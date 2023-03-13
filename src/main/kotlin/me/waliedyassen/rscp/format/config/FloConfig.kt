package me.waliedyassen.rscp.format.config

import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.Side
import me.waliedyassen.rscp.binary.BinaryEncoder
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.symbol.SymbolTable
import me.waliedyassen.rscp.symbol.SymbolType

class FloConfig(override val name: String) : Config(name, SymbolType.Flo) {

    private var colour: Int? = null
    private var mapColour: Int? = null
    private var texture: Any? = null
    private var occlude: Boolean? = null

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "colour" -> colour = parser.parseInteger() ?: return parser.skipProperty()
            "mapcolour" -> colour = parser.parseInteger() ?: return parser.skipProperty()
            "texture" -> texture = parser.parseReference(SymbolType.Texture)
            "occlude" -> occlude = parser.parseBoolean()
            else -> parser.unknownProperty()
        }
    }

    override fun verifyProperties(parser: Parser) {
        if (colour == null && texture == null && mapColour == null) {
            parser.reportUnitError("Floor overlay must define either a 'texture' or a 'colour' or a 'mapcolour' property")
        }
    }

    override fun resolveReferences(compiler: Compiler) {
        compiler.resolveReference(this::texture)
    }

    override fun encode(side: Side, sym: SymbolTable): ByteArray {
        val encoder = BinaryEncoder(12)
        colour?.let {
            encoder.code(1)
            encoder.write3(it)
        }
        texture?.let {
            encoder.code(2)
            encoder.write1(texture as Int)
        }
        occlude?.let {
            if (it) {
                return@let
            }
            encoder.write1(5)
        }
        mapColour?.let {
            encoder.code(7)
            encoder.write3(it)
        }
        encoder.terminateCode()
        return encoder.toByteArray()
    }
}