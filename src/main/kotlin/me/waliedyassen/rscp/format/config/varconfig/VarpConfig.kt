package me.waliedyassen.rscp.format.config.varconfig

import me.waliedyassen.rscp.Side
import me.waliedyassen.rscp.binary.BinaryEncoder
import me.waliedyassen.rscp.format.config.varconfig.constants.VarDomainType
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.symbol.SymbolTable

/**
 * Implementation for 'varp' type configuration.
 *
 * @author Walied K. Yassen
 */
class VarpConfig(override val debugName: String) : VarConfig(VarDomainType.Player) {

    private var clientCode = 0

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "clientcode" -> clientCode = parser.parseInteger() ?: return parser.skipProperty()
            else -> super.parseProperty(name, parser)
        }
    }

    override fun encodeVar(packet: BinaryEncoder, side: Side, sym: SymbolTable) {
        if (clientCode != 0) {
            packet.code(5)
            packet.write2(clientCode)
        }
    }
}