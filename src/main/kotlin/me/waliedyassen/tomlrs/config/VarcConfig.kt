package me.waliedyassen.tomlrs.config

import com.fasterxml.jackson.databind.JsonNode
import me.waliedyassen.tomlrs.CompilationContext
import me.waliedyassen.tomlrs.binary.BinaryEncoder
import me.waliedyassen.tomlrs.parser.Parser
import me.waliedyassen.tomlrs.symbol.SymbolType
import me.waliedyassen.tomlrs.util.asEnumLiteral
import me.waliedyassen.tomlrs.util.asSymbolType

class VarcConfig(name: String) : Config(name, SymbolType.VARC) {

    lateinit var type: SymbolType
    var scope = VarLifetime.TEMPORARY

    override fun parseToml(node: JsonNode, context: CompilationContext) {
        type = node["type"].asSymbolType()
        if (node.has("scope"))
            scope = node["scope"].asEnumLiteral()
    }

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "type" -> type = parser.parseType() ?: return
            "scope" -> scope = parser.parseEnumLiteral(VarLifetime.TEMPORARY)
            else -> parser.unknownProperty()
        }
    }

    override fun verifyProperties(parser: Parser) {
        if (!this::type.isInitialized) {
            parser.reportError("type property must be specified")
        }
    }

    override fun encode(): ByteArray {
        val packet = BinaryEncoder(6)
        packet.code(1) {
            write1(type.char.code)
        }
        if (scope == VarLifetime.PERMANENT) {
            packet.code(2) {}
        }
        packet.terminateCode()
        return packet.toByteArray()
    }

}