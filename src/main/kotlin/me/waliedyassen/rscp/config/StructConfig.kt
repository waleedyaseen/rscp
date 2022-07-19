package me.waliedyassen.rscp.config

import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.binary.BinaryEncoder
import me.waliedyassen.rscp.binary.codeParams
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.parser.Reference
import me.waliedyassen.rscp.parser.parseParam
import me.waliedyassen.rscp.symbol.SymbolType

/**
 * Implementation for 'struct' type configuration.
 *
 * @author Walied K. Yassen
 */
class StructConfig(name: String) : Config(name, SymbolType.Struct) {

    /**
     * The 'params' attribute of the struct type.
     */
    private var params = LinkedHashMap<Int, Any>()

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "param" -> parser.parseParam(params)
            else -> parser.unknownProperty()
        }
    }

    override fun verifyProperties(parser: Parser) {
        // Do nothing.
    }

    override fun resolveReferences(compiler: Compiler) {
        val newParams = params.map { (key, value) ->
            val transformedValue = if (value is Reference) compiler.resolveReference(value) else value
            key to transformedValue
        }.toMap()
        params.clear()
        params += newParams
    }

    override fun encode(): ByteArray {
        val packet = BinaryEncoder(32)
        packet.codeParams(params)
        packet.terminateCode()
        return packet.toByteArray()
    }
}