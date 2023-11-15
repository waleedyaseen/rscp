package me.waliedyassen.rscp.format.config

import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.Side
import me.waliedyassen.rscp.binary.BinaryEncoder
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.symbol.SymbolTable
import me.waliedyassen.rscp.symbol.SymbolType
import me.waliedyassen.rscp.symbol.TypedSymbol
import me.waliedyassen.rscp.util.LiteralEnum

@Suppress("unused")
enum class VarLifetime(val id: Int, override val literal: String) : LiteralEnum {
    TEMPORARY(0, "temp"),
    PERMANENT(1, "perm"),
    SERVERPERMANENT(2, "serverperm");
}

@Suppress("unused")
enum class VarTransmitLevel(val id: Int, override val literal: String) : LiteralEnum {
    No(0, "no"),
    Yes(1, "yes");
}

/**
 * Implementation for 'varp' type configuration.
 *
 * @author Walied K. Yassen
 */
class VarpConfig(override val debugName: String) : Config(SymbolType.VarPlayer) {

    /**
     * The `type` attribute of the varp.
     */
    private var dataType: SymbolType<*> = SymbolType.Undefined

    /**
     * The 'clientcode' attribute of the varp.
     */
    private var clientCode = 0

    /**
     * The `lifetime` attribute of the varp.
     */
    private var lifetime = VarLifetime.TEMPORARY

    /**
     * The `transmit` attribute of the varp.
     */
    private var clientTransmitLevel = VarTransmitLevel.No

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "type" -> dataType = parser.parseType() ?: return parser.skipProperty()
            "clientcode" -> clientCode = parser.parseInteger() ?: return parser.skipProperty()
            "scope" -> lifetime = parser.parseEnumLiteral() ?: return parser.skipProperty()
            "transmit" -> clientTransmitLevel = parser.parseEnumLiteral() ?: return parser.skipProperty()
            else -> parser.unknownProperty()
        }
    }

    override fun verifyProperties(parser: Parser) {
        if (dataType == SymbolType.Undefined) {
            parser.reportUnitError("type property must be specified")
        }
    }

    override fun resolveReferences(compiler: Compiler) {
        // Do nothing.
    }

    override fun createSymbol(id: Int) = TypedSymbol(debugName, id, dataType)

    override fun encode(side: Side, sym: SymbolTable): ByteArray {
        val expectedSize = 1 + (if (clientCode != 0) 3 else 0) + if (lifetime != VarLifetime.TEMPORARY) 2 else 0
        val packet = BinaryEncoder(expectedSize)

        if (side == Side.Server) {
            packet.code(1)
            packet.write1or2(dataType.id)

            if (lifetime != VarLifetime.TEMPORARY) {
                packet.code(2)
                packet.write1(lifetime.id)
            }
            if (clientTransmitLevel != VarTransmitLevel.No) {
                packet.code(3)
                packet.write1(clientTransmitLevel.id)
            }
        }
        if (clientCode != 0) {
            packet.code(5)
            packet.write2(clientCode)
        }
        packet.terminateCode()
        return packet.toByteArray()
    }
}