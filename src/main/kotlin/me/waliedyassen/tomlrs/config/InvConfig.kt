package me.waliedyassen.tomlrs.config

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ser.std.ToEmptyObjectSerializer
import me.waliedyassen.tomlrs.CompilationContext
import me.waliedyassen.tomlrs.binary.BinaryEncoder
import me.waliedyassen.tomlrs.parser.Parser
import me.waliedyassen.tomlrs.symbol.SymbolType
import me.waliedyassen.tomlrs.util.LiteralEnum
import me.waliedyassen.tomlrs.util.asEnumLiteral
import java.awt.Color

enum class InvScope(val id: Int, override val literal: String) : LiteralEnum {
    TEMPORARY(0, "temp"),
    PERMANENT(1, "perm"),
}

/**
 * Implementation for 'inv' type configuration.
 *
 * @author Walied K. Yassen
 */
class InvConfig : Config(SymbolType.INV) {

    var size = 0
    var scope = InvScope.TEMPORARY

    override fun parseToml(node: JsonNode, context: CompilationContext) {
        size = node["size"]?.asInt(0) ?: 0
        if (node.has("scope"))
            scope = node["scope"].asEnumLiteral()
    }

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "size" -> size = parser.parseInteger()
            "scope" -> scope = parser.parseEnumLiteral(InvScope.TEMPORARY)
            else -> parser.unknownProperty()
        }
    }

    override fun verifyProperties(parser: Parser) {
        // Do nothing.
    }

    override fun encode(): ByteArray {
        val packet = BinaryEncoder(1 + if (size != 0) 3 else 0)
        if (scope != InvScope.TEMPORARY) {
            packet.code(1) {
                write1(scope.id)
            }
        }
        if (size != 0) {
            packet.code(2) {
                write2(size)
            }
        }
        packet.terminateCode()
        return packet.toByteArray()
    }
}