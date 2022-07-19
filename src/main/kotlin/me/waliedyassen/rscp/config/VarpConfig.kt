package me.waliedyassen.rscp.config

import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.binary.BinaryEncoder
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.symbol.SymbolType
import me.waliedyassen.rscp.symbol.TypedSymbol
import me.waliedyassen.rscp.util.LiteralEnum

enum class VarLifetime(val id: Int, override val literal: String) : LiteralEnum {
    TEMPORARY(0, "temp"),
    PERMANENT(1, "perm"),
    SERVERPERMANENT(2, "serverperm");
}

/**
 * Implementation for 'varp' type configuration.
 *
 * @author Walied K. Yassen
 */
class VarpConfig(name: String) : Config(name, SymbolType.VarPlayer) {

    /**
     * The `type` attribute of the varp.
     */
    private var type: SymbolType<*> = SymbolType.Undefined

    /**
     * The 'clientcode' attribute of the varp.
     */
    private var clientCode = 0

    /**
     * The `lifetime` attribute of the varp.
     */
    private var lifetime = VarLifetime.TEMPORARY

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "type" -> type = parser.parseType() ?: return parser.skipProperty()
            "clientcode" -> clientCode = parser.parseInteger() ?: return parser.skipProperty()
            "scope" -> lifetime = parser.parseEnumLiteral() ?: return parser.skipProperty()
            else -> parser.unknownProperty()
        }
    }

    override fun verifyProperties(parser: Parser) {
        // Do nothing.
    }

    override fun resolveReferences(compiler: Compiler) {
        // Do nothing.
    }

    override fun createSymbol(id: Int) = TypedSymbol(name, id, type)

    override fun encode(): ByteArray {
        val expectedSize = 1 + (if (clientCode != 0) 3 else 0) + if (lifetime != VarLifetime.TEMPORARY) 2 else 0
        val packet = BinaryEncoder(expectedSize)
        if (lifetime != VarLifetime.TEMPORARY) {
            packet.code(4) {
                write1(lifetime.id)
            }
        }
        if (clientCode != 0) {
            packet.code(5) {
                write2(clientCode)
            }
        }
        packet.terminateCode()
        return packet.toByteArray()
    }
}