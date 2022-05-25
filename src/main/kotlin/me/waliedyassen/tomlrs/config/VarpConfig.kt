package me.waliedyassen.tomlrs.config

import com.fasterxml.jackson.databind.JsonNode
import me.waliedyassen.tomlrs.CompilationContext
import me.waliedyassen.tomlrs.binary.BinaryEncoder
import me.waliedyassen.tomlrs.symbol.SymbolType
import me.waliedyassen.tomlrs.util.LiteralEnum
import me.waliedyassen.tomlrs.util.asEnumLiteral

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
class VarpConfig : Config(SymbolType.VAR_PLAYER) {

    /**
     * The 'clientcode' attribute of the enum type.
     */
    private var clientCode = 0

    /**
     * The `lifetime` attribute of the enum type.
     */
    private var lifetime = VarLifetime.TEMPORARY

    override fun parseToml(node: JsonNode, context: CompilationContext) {
        clientCode = node["clientcode"]?.asInt(-1) ?: 0
        if (node.has("scope"))
            lifetime = node["scope"].asEnumLiteral()
    }

    override fun encode(): ByteArray {
        val packet = BinaryEncoder(1 + (if (clientCode != 0) 3 else 0) + if (lifetime != VarLifetime.TEMPORARY) 2 else 0)
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