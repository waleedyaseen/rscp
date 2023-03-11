package me.waliedyassen.rscp.format.dbtable

import me.waliedyassen.rscp.CodeGenerator
import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.Side
import me.waliedyassen.rscp.binary.BinaryEncoder
import me.waliedyassen.rscp.binary.writeDbColumn
import me.waliedyassen.rscp.format.config.Config
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.parser.Reference
import me.waliedyassen.rscp.parser.Token
import me.waliedyassen.rscp.symbol.DbColumnSymbol
import me.waliedyassen.rscp.symbol.SymbolTable
import me.waliedyassen.rscp.symbol.SymbolType
import me.waliedyassen.rscp.util.LiteralEnum
import java.io.File
import java.util.*

data class DbColumn(
    val id: Int,
    val name: String,
    val type: List<SymbolType<*>>,
    val props: Set<DbColumnProp>,
    var defaults: MutableList<Any>? = null
) {

    fun createSymbol(tableName: String, tableId: Int): DbColumnSymbol {
        return DbColumnSymbol("$tableName:$name", tableId shl 12 or (id shl 4), type, props)
    }
}

enum class DbColumnProp(override val literal: String) : LiteralEnum {
    Required("REQUIRED"),
    Indexed("INDEXED"),
    ClientSide("CLIENTSIDE"),
    List("LIST")
}

class DbTableConfig(override val name: String) : Config(name, SymbolType.DbTable) {

    private val columns = mutableListOf<DbColumn>()

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "column" -> {
                val columnNameToken = parser.parseIdentifier() ?: return parser.skipProperty()
                parser.storeSemInfo(columnNameToken.span, "reference")
                columnNameToken as Token.Identifier
                val columnName = columnNameToken.text.lowercase()
                val types = mutableListOf<SymbolType<*>>()
                var props: List<DbColumnProp>? = null
                while (parser.isComma()) {
                    parser.parseComma() ?: return parser.skipProperty()
                    if (parser.isEnumLiteral<DbColumnProp>()) {
                        props = parser.parseEnumLiteralList() ?: return parser.skipProperty()
                    } else {
                        val type = parser.parseType() ?: return parser.skipProperty()
                        if (type.id == -1) {
                            parser.reportPropertyError("Cannot use type '${type.literal}' here")
                            return parser.skipProperty()
                        }
                        types.add(type)
                    }
                }
                if (columns.any { it.name == columnName }) {
                    return parser.reportPropertyError("Duplicate column '${columnName}'")
                }
                val column = DbColumn(
                    columns.size,
                    columnName,
                    types,
                    props?.toSet() ?: emptySet()
                )
                columns.add(column)
            }

            "default" -> {
                val columnNameToken = parser.parseIdentifier() ?: return parser.skipProperty()
                parser.storeSemInfo(columnNameToken.span, "reference")
                columnNameToken as Token.Identifier
                val column = columns.find { it.name == columnNameToken.text.lowercase() }
                    ?: return parser.reportPropertyError("Column '${columnNameToken.text.lowercase()}' must be defined first")
                val defaults = mutableListOf<Any>()
                for (type in column.type) {
                    parser.parseComma() ?: return parser.skipProperty()
                    val default = parser.parseDynamic(type) ?: return parser.skipProperty()
                    defaults += default
                }
                // If the field is required, we should not allow for it to have a default value
                if (column.props.contains(DbColumnProp.Required)) {
                    parser.reportPropertyError("Required columns are not allowed to have defaults")
                    return
                }
                var defaultsList = column.defaults
                if (defaultsList == null) {
                    defaultsList = mutableListOf()
                    column.defaults = defaultsList
                }
                defaultsList.addAll(defaults)
            }

            else -> parser.unknownProperty()
        }
    }

    override fun verifyProperties(parser: Parser) {
    }

    override fun generateCode(allUnits: List<CodeGenerator>, outputFolder: File, sym: SymbolTable, side: Side) {
        // TODO(Walied): This is very slow when number of configs is high. Find a better solution around this.
        super.generateCode(allUnits, outputFolder, sym, side)
        val id = sym.lookupSymbol(symbolType, name)!!.id
        val indexDirectory = outputFolder.resolve("dbtableindex/$id")
        indexDirectory.deleteRecursively()
        check(indexDirectory.mkdirs()) { "Failed to create the output directory '$indexDirectory'" }
        val dbRowConfigs = allUnits.filter { it is DbRowConfig && it.table == id }
        @Suppress("UNCHECKED_CAST")
        dbRowConfigs as List<DbRowConfig>
        val masterIndex = generateMasterIndex(sym, dbRowConfigs)
        masterIndex.writeTo(indexDirectory.resolve("0"))
        columns.forEach {
            if (it.props.contains(DbColumnProp.Indexed)) {
                val columnId = it.id shr 4 and 0xff
                val columnIndex = generateColumnIndex(sym, dbRowConfigs, it.name, it.type)
                columnIndex.writeTo(indexDirectory.resolve((columnId + 1).toString()))
            }
        }
    }


    private fun generateMasterIndex(sym: SymbolTable, dbRowConfigs: List<DbRowConfig>): DbTableIndex {
        val index = DbTableIndex()
        dbRowConfigs.forEach { dbRowConfig ->
            val id = sym.lookupSymbol(SymbolType.DbRow, dbRowConfig.name)!!.id
            index.add(0, 0, id)
        }
        return index
    }

    private fun generateColumnIndex(
        sym: SymbolTable,
        dbRowConfigs: List<DbRowConfig>,
        name: String,
        tupleTypes: List <SymbolType<*>>
    ): DbTableIndex {
        val tupleSize = tupleTypes.size
        val indexingDbRowConfigs = dbRowConfigs.filter { it.columns.contains(name) }
        val dbIndex = DbTableIndex(tupleTypes.size, tupleTypes.map { if (it is SymbolType.String) 2 else 0 }.toIntArray())
        for (indexingDbRowConfig in indexingDbRowConfigs) {
            val id = sym.lookupSymbol(SymbolType.DbRow, indexingDbRowConfig.name)!!.id
            val values = indexingDbRowConfig.columns[name]!!
            for ((index, value) in values.withIndex()) {
                val tupleIndex = index % tupleSize
                dbIndex.add(tupleIndex, value, id)
            }
        }
        return dbIndex
    }

    override fun contributeSymbols(sym: SymbolTable) {
        super.contributeSymbols(sym)
        val id = sym.lookupSymbol(SymbolType.DbTable, name)!!.id
        val columnList = sym.lookupList(SymbolType.DbColumn)
        val oldSym = columnList.symbols.filter { it.id shr 12 == id }.toSet()
        val newSym = columns.map { column -> column.createSymbol(name, id) }.toSet()
        val deleted = oldSym - newSym
        val additions = newSym - oldSym
        deleted.forEach { columnList.remove(it) }
        additions.forEach { columnList.add(it) }
    }

    override fun resolveReferences(compiler: Compiler) {
        columns.forEach { column ->
            val defaults = column.defaults ?: return@forEach
            defaults.forEachIndexed { index, default ->
                if (default is Reference) {
                    defaults[index] = compiler.resolveReference(default, true)
                }
            }
        }
    }

    override fun encode(side: Side, sym: SymbolTable): ByteArray {
        val encoder = BinaryEncoder(256)
        if (columns.isNotEmpty()) {
            encoder.code(1) {
                encoder.write1(columns.size)
                columns.forEachIndexed { index, column ->
                    val defaults = column.defaults
                    encoder.write1(index or if (defaults != null) 0x80 else 0x00)
                    encoder.write1(column.type.size)
                    column.type.forEach { type -> encoder.write1or2(type.id) }
                    if (defaults != null) {
                        encoder.writeDbColumn(column.type, defaults)
                    }
                }
                encoder.write1(255)
            }
        }
        encoder.code(0)
        return encoder.toByteArray()
    }
}