package me.waliedyassen.rscp.format.config.varconfig

import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.Side
import me.waliedyassen.rscp.binary.BinaryEncoder
import me.waliedyassen.rscp.format.config.Config
import me.waliedyassen.rscp.format.config.varconfig.constants.VarDomainType
import me.waliedyassen.rscp.format.config.varconfig.constants.VarLifetime
import me.waliedyassen.rscp.format.config.varconfig.constants.VarTransmitLevel
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.symbol.SymbolTable
import me.waliedyassen.rscp.symbol.SymbolType
import me.waliedyassen.rscp.symbol.TypedSymbol

abstract class VarConfig(private val domain: VarDomainType) : Config(domain.type) {

    protected var dataType: SymbolType<*> = SymbolType.Undefined
    protected var lifetime = VarLifetime.Temporary
    protected var clientTransmitLevel = VarTransmitLevel.No

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "type" -> dataType = parser.parseType() ?: return parser.skipProperty()
            "scope" -> lifetime = parser.parseEnumLiteral() ?: return parser.skipProperty()
            "transmit" -> {
                clientTransmitLevel = parser.parseEnumLiteral() ?: return parser.skipProperty()
                if (!domain.permitClientTransmitLevel) {
                    parser.reportPropertyError("transmit property is not allowed for this var type")
                }
            }

            else -> parser.unknownProperty()
        }
    }

    override fun verifyProperties(parser: Parser) {
        if (dataType == SymbolType.Undefined) {
            parser.reportUnitError("type property must be specified for var types")
        }
    }

    override fun resolveReferences(compiler: Compiler) {
        // Do nothing.
    }

    override fun createSymbol(id: Int) = TypedSymbol(debugName, id, dataType)

    override fun encode(side: Side, sym: SymbolTable): ByteArray {
        val packet = BinaryEncoder(32)

        if (side == Side.Server) {
            packet.code(1)
            packet.write1or2(dataType.id)

            if (lifetime != VarLifetime.Temporary) {
                packet.code(2)
                packet.write1(lifetime.id)
            }
            if (clientTransmitLevel != VarTransmitLevel.No) {
                packet.code(3)
                packet.write1(clientTransmitLevel.id)
            }
        }
        encodeVar(packet, side, sym)
        packet.terminateCode()
        return packet.toByteArray()
    }

    protected abstract fun encodeVar(packet: BinaryEncoder, side: Side, sym: SymbolTable)
}