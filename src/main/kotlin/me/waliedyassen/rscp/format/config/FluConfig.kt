package me.waliedyassen.rscp.format.config

import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.Side
import me.waliedyassen.rscp.binary.BinaryEncoder
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.symbol.SymbolTable
import me.waliedyassen.rscp.symbol.SymbolType

class FluConfig(override val debugName: String) : Config(SymbolType.Flu) {

    private var colour: Int? = null

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "colour" -> colour = parser.parseInteger() ?: return parser.skipProperty()
            else -> parser.unknownProperty()
        }
    }

    override fun verifyProperties(parser: Parser) {
        if (colour == null) {
            parser.reportUnitError("Floor underlay must define a 'colour' property")
        }
    }

    override fun resolveReferences(compiler: Compiler) {
    }

    override fun encode(side: Side, sym: SymbolTable): ByteArray {
        val encoder = BinaryEncoder(5)
        colour?.let {
            encoder.code(1)
            encoder.write3(it)
        }
        encoder.terminateCode()
        return encoder.toByteArray()
    }
}