package me.waliedyassen.rsconfig.config

import com.fasterxml.jackson.databind.JsonNode
import me.waliedyassen.rsconfig.CompilationContext
import me.waliedyassen.rsconfig.binary.BinaryEncoder
import me.waliedyassen.rsconfig.binary.codeParams
import me.waliedyassen.rsconfig.parser.Parser
import me.waliedyassen.rsconfig.parser.Span
import me.waliedyassen.rsconfig.parser.parseParam
import me.waliedyassen.rsconfig.symbol.SymbolType
import me.waliedyassen.rsconfig.util.asValue

/**
 * Implementation for 'struct' type configuration.
 *
 * @author Walied K. Yassen
 */
class StructConfig(name: String) : Config(name, SymbolType.STRUCT) {

    /**
     * The 'params' attribute of the struct type.
     */
    private var params = LinkedHashMap<Int, Any>()

    override fun parseToml(node: JsonNode, context: CompilationContext) {
        node.fields().forEach { (key, value) ->
            val param = context.sym.lookupOrNull(SymbolType.PARAM, key)
            if (param == null) {
                context.reportError(Span.empty(), "Unresolved param reference to '${key}'")
                return@forEach
            }
            params[param.id] = value.asValue(param.content!!, context)
        }
    }

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "param" -> parser.parseParam(params)
            else -> parser.unknownProperty()
        }
    }

    override fun verifyProperties(parser: Parser) {
        // Do nothing.
    }

    override fun encode(): ByteArray {
        val packet = BinaryEncoder(32)
        packet.codeParams(params)
        packet.terminateCode()
        return packet.toByteArray()
    }
}