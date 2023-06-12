package me.waliedyassen.rscp.format.dbtable

import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.Side
import me.waliedyassen.rscp.binary.BinaryEncoder
import me.waliedyassen.rscp.binary.writeDbColumn
import me.waliedyassen.rscp.format.config.Config
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.parser.Reference
import me.waliedyassen.rscp.parser.Token
import me.waliedyassen.rscp.symbol.SymbolTable
import me.waliedyassen.rscp.symbol.SymbolType

class DbRowConfig(override val debugName: String) : Config(SymbolType.DbRow) {

    private var tableReference: Reference? = null
    var table: Int? = null
    var columns = mutableMapOf<String, MutableList<Any>>()

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "table" -> {
                tableReference = parser.parseReference(SymbolType.DbTable) ?: return parser.skipProperty()
                if (tableReference == null) {
                    return parser.reportPropertyError("Duplicate 'table' property")
                }
                table = parser.compiler.resolveReference(tableReference, false)

            }

            "data" -> {
                if (table == -1) {
                    return
                }
                val tableName = tableReference?.name?.lowercase() ?: return parser.skipProperty()
                val literal = parser.parseIdentifier() ?: return parser.skipProperty()
                parser.storeSemInfo(literal.span, "reference")
                val columnName = (literal as Token.Identifier).text.lowercase()
                val columnSymbol = parser.compiler.sym.lookupSymbol(SymbolType.DbColumn, "${tableName}:${columnName}")
                if (columnSymbol == null) {
                    parser.reportPropertyError("Could not find '$columnName' in $tableName")
                    parser.skipProperty()
                    return
                }
                var list = columns[columnName]
                val repeated = list != null
                if (list == null) {
                    list = mutableListOf()
                    columns[columnName] = list
                }
                for (type in columnSymbol.types) {
                    parser.parseComma() ?: return parser.skipProperty()
                    list.add(parser.parseDynamic(type) ?: return parser.skipProperty())
                }
                if (repeated && !columnSymbol.props.contains(DbColumnProp.List)) {
                    parser.reportPropertyError("The column '${columnName}' does not allow listing")
                }
            }
        }
    }

    override fun verifyProperties(parser: Parser) {
        if (table == -1) {
            return
        }
        val tableConfig = parser.compiler.sym.lookupList(SymbolType.DbColumn)
        val tableSymbols = tableConfig.symbols.filter { it.id shr 12 == table }
        tableSymbols.forEach { sym ->
            val required = sym.props.contains(DbColumnProp.Required)
            if (!required) {
                return@forEach
            }
            // TODO(Walied): Maybe we could avoid the split in here
            val columnName = sym.name.split(":")[1]
            if (!columns.containsKey(columnName)) {
                parser.reportUnitError("Table ${tableReference!!.name} requires column '${columnName}' to be defined")
            }
        }
    }

    override fun resolveReferences(compiler: Compiler) {
        columns.values.forEach { list ->
            list.forEachIndexed { index, any ->
                if (any is Reference) {
                    list[index] = compiler.resolveReference(any)
                }
            }
        }
    }

    override fun encode(side: Side, sym: SymbolTable): ByteArray {
        val encoder = BinaryEncoder(64)
        encoder.code(4) {
            encoder.writeVarInt(table!!)
        }
        if (columns.isNotEmpty()) {
            encoder.code(3) {
                val highestColumnIndex = sym.lookupList(SymbolType.DbColumn)
                    .symbols
                    .filter { it.id ushr 12 == table }
                    .maxOf { it.id shr 4 and 0xff }
                encoder.write1(highestColumnIndex + 1)
                columns.forEach { (name, values) ->
                    val column = sym.lookupSymbol(SymbolType.DbColumn, "${tableReference!!.name}:$name")!!
                    encoder.write1(column.id shr 4 and 0xff)
                    encoder.write1(column.types.size)
                    column.types.forEach { type -> encoder.write1or2(type.id) }
                    encoder.writeDbColumn(column.types, values)
                }
                encoder.write1(255)
            }
        }
        encoder.code(0)
        return encoder.toByteArray()
    }
}