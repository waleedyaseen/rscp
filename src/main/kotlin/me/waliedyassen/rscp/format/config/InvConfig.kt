package me.waliedyassen.rscp.format.config

import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.Side
import me.waliedyassen.rscp.binary.BinaryEncoder
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.symbol.SymbolTable
import me.waliedyassen.rscp.symbol.SymbolType
import me.waliedyassen.rscp.util.LiteralEnum

enum class InvScope(val id: Int, override val literal: String) : LiteralEnum {
    TEMPORARY(0, "temp"),
    PERMANENT(1, "perm"),
}

/**
 * Implementation for 'inv' type configuration.
 *
 * @author Walied K. Yassen
 */
class InvConfig(name: String) : Config(name, SymbolType.Inv) {

    var size = 0
    var scope = InvScope.TEMPORARY

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "size" -> size = parser.parseInteger() ?: return parser.skipProperty()
            "scope" -> scope = parser.parseEnumLiteral() ?: return parser.skipProperty()
            "transmit" -> transmit = parser.parseBoolean()
            else -> parser.unknownProperty()
        }
    }

    override fun verifyProperties(parser: Parser) {
        // Do nothing.
    }

    override fun resolveReferences(compiler: Compiler) {
        // Do nothing.
    }

    override fun encode(side: Side, sym: SymbolTable): ByteArray {
        val packet = BinaryEncoder(1 + if (size != 0) 3 else 0)
        if (side == Side.Server || transmit) {
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
        }
        packet.terminateCode()
        return packet.toByteArray()
    }
}