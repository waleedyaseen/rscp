package me.waliedyassen.rscp.format.config.varconfig

import me.waliedyassen.rscp.Side
import me.waliedyassen.rscp.binary.BinaryEncoder
import me.waliedyassen.rscp.format.config.varconfig.constants.VarDomainType
import me.waliedyassen.rscp.format.config.varconfig.constants.VarLifetime
import me.waliedyassen.rscp.symbol.SymbolTable

class VarcConfig(override val debugName: String) : VarConfig(VarDomainType.Client) {

    override fun encode(side: Side, sym: SymbolTable): ByteArray {
        val packet = BinaryEncoder(6)
        if (side == Side.Server) {
            packet.code(1)
            packet.write1(dataType.legacyChar.code)
        }
        if (lifetime == VarLifetime.Permanent) {
            packet.code(2)
        }
        packet.terminateCode()
        return packet.toByteArray()
    }

    override fun encodeVar(packet: BinaryEncoder, side: Side, sym: SymbolTable) {
        // Do nothing.
    }
}