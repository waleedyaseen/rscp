package me.waliedyassen.rsconfig.config

import com.fasterxml.jackson.databind.JsonNode
import me.waliedyassen.rsconfig.CompilationContext
import me.waliedyassen.rsconfig.binary.BinaryEncoder
import me.waliedyassen.rsconfig.parser.Parser
import me.waliedyassen.rsconfig.symbol.SymbolType
import me.waliedyassen.rsconfig.util.asSymbolType
import me.waliedyassen.rsconfig.util.asValue

class ParamConfig(name: String) : Config(name, SymbolType.PARAM) {

    var type: SymbolType? = null
    private var defaultInt: Int? = null
    private var defaultStr: String? = null
    private var autoDisable: Boolean = true

    override fun parseToml(node: JsonNode, context: CompilationContext) {
        type = node["type"].asSymbolType()
        if (node.has("default")) {
            val value = node["default"].asValue(type!!, context)
            if (type == SymbolType.STRING) {
                defaultStr = value as String
            } else {
                defaultInt = value as Int
            }
        }
        if (node.has("autodisable")) {
            autoDisable = node["autodisable"].asBoolean()
        }
    }

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "type" -> type = parser.parseType() ?: return
            "default" -> {
                if (type == null) {
                    parser.reportError("type must be specified before default")
                    return
                }
                val value = parser.parseDynamic(type!!)
                if (type == SymbolType.STRING) {
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

    override fun encode(): ByteArray {
        val packet = BinaryEncoder(7)
        packet.code(1) { write1(type!!.char.code) }
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