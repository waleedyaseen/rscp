package me.waliedyassen.rscp.format.config

import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.binary.BinaryEncoder
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.symbol.SymbolType
import me.waliedyassen.rscp.symbol.TypedSymbol

class ParamConfig(name: String) : Config(name, SymbolType.Param) {

    var type: SymbolType<*> = SymbolType.Undefined
    private var defaultInt: Any? = null
    private var defaultStr: String? = null
    private var autoDisable: Boolean = true

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "type" -> type = parser.parseType() ?: return
            "default" -> {
                if (type == SymbolType.Undefined) {
                    parser.reportPropertyError("type must be specified before default")
                    return
                }
                val value = parser.parseDynamic(type)
                if (type == SymbolType.String) {
                    defaultStr = value as String
                } else {
                    defaultInt = value
                }
            }

            "autodisable" -> autoDisable = parser.parseBoolean()
        }
    }

    override fun verifyProperties(parser: Parser) {
        if (type == SymbolType.Undefined) {
            parser.reportUnitError("type property must be specified")
            return
        }
    }

    override fun resolveReferences(compiler: Compiler) {
        compiler.resolveReference(::defaultInt)
    }
    override fun createSymbol(id: Int) = TypedSymbol(name, id, type)

    override fun encode(): ByteArray {
        val packet = BinaryEncoder(7)
        packet.code(1) { write1(type.legacyChar.code) }
        if (defaultInt != null) {
            packet.code(2) { write4(defaultInt as Int) }
        }
        if (!autoDisable) {
            packet.code(4)
        }
        if (defaultStr != null) {
            packet.code(5) { writeString(defaultStr!!) }
        }
        packet.terminateCode()
        return packet.toByteArray()
    }

}