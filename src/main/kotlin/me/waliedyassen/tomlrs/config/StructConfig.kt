package me.waliedyassen.tomlrs.config

import com.fasterxml.jackson.databind.JsonNode
import me.waliedyassen.tomlrs.CompilationContext
import me.waliedyassen.tomlrs.binary.BinaryEncoder
import me.waliedyassen.tomlrs.symbol.SymbolType
import me.waliedyassen.tomlrs.util.asValue

/**
 * Implementation for 'struct' type configuration.
 *
 * @author Walied K. Yassen
 */
class StructConfig : Config(SymbolType.STRUCT) {

    /**
     * The 'params' attribute of the struct type.
     */
    private var params = LinkedHashMap<Int, Any>()

    override fun parseToml(node: JsonNode, context: CompilationContext) {
        node.fields().forEach { (key, value) ->
            val param = context.sym.lookupOrNull(SymbolType.PARAM, key)
            if (param == null) {
                context.reportError("Unresolved param reference to '${key}'")
                return@forEach
            }
            params[param.id] = value.asValue(param.content!!, context)
        }
    }

    override fun encode(): ByteArray {
        val packet = BinaryEncoder(32)
        packet.code(249) {
            write1(params.size)
            params.forEach { (key, value) ->
                val stringValue = value is String
                write1(if (stringValue) 1 else 0)
                write3(key)
                if (stringValue) {
                    writeString(value as String)
                } else {
                    write4(value as Int)
                }
            }
        }
        packet.terminateCode()
        return packet.toByteArray()
    }
}