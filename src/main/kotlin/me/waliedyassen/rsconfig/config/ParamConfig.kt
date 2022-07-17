package me.waliedyassen.rsconfig.config

import me.waliedyassen.rsconfig.binary.BinaryEncoder
import me.waliedyassen.rsconfig.parser.Parser
import me.waliedyassen.rsconfig.symbol.SymbolType
import me.waliedyassen.rsconfig.symbol.TypedSymbol

class ParamConfig(name: String) : Config(name, SymbolType.Param) {

    var type: SymbolType<*>? = null
    private var defaultInt: Int? = null
    private var defaultStr: String? = null
    private var autoDisable: Boolean = true

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "type" -> type = parser.parseType() ?: return
            "default" -> {
                if (type == null) {
                    parser.reportError("type must be specified before default")
                    return
                }
                val value = parser.parseDynamic(type!!)
                if (type == SymbolType.String) {
                    defaultStr = value as String
                } else {
                    defaultInt = value as Int
                }
            }

            "autodisable" -> autoDisable = parser.parseBoolean()
        }
    }

    override fun verifyProperties(parser: Parser) {
        if (type == null) {
            parser.reportError("type property must be specified")
            return
        }
    }

    override fun createSymbol(id: Int) = TypedSymbol(name, id, type!!)

    override fun encode(): ByteArray {
        val packet = BinaryEncoder(7)
        packet.code(1) { write1(type!!.legacyChar.code) }
        if (defaultInt != null) {
            packet.code(2) { write4(defaultInt!!) }
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