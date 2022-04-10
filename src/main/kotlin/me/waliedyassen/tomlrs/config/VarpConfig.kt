package me.waliedyassen.tomlrs.config

import com.fasterxml.jackson.databind.JsonNode
import me.waliedyassen.tomlrs.CompilationContext
import me.waliedyassen.tomlrs.binary.BinaryEncoder
import me.waliedyassen.tomlrs.symbol.SymbolType

/**
 * Implementation for 'varp' type configuration.
 *
 * @author Walied K. Yassen
 */
class VarpConfig : Config(SymbolType.VAR_PLAYER) {

    /**
     * The 'clientcode' attribute of the enum type.
     */
    private var clientCode = 0

    override fun parseToml(node: JsonNode, context: CompilationContext) {
        clientCode = node["clientcode"]?.asInt(-1) ?: 0
    }

    override fun encode(): ByteArray {
        val packet = BinaryEncoder(1 + if (clientCode != 0) 3 else 0)
        if (clientCode != 0) {
            packet.code(5) {
                write2(clientCode)
            }
        }
        packet.terminateCode()
        return packet.toByteArray()
    }
}