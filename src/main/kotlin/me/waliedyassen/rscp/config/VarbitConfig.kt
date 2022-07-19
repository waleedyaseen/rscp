package me.waliedyassen.rscp.config

import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.binary.BinaryEncoder
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.parser.Reference
import me.waliedyassen.rscp.symbol.SymbolType

/**
 * Implementation for 'varbit' type configuration.
 *
 * @author Walied K. Yassen
 */
class VarbitConfig(name: String) : Config(name, SymbolType.VarBit) {

    /**
     * The 'startbit' attribute of the enum type.
     */
    private var startBit = 0

    /**
     * The 'endbit' attribute of the enum type.
     */
    private var endBit = 0

    /**
     * The 'basevar' attribute of the enum type.
     */
    private var baseVar: Any = -1

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "startbit" -> startBit = parser.parseInteger() ?: return parser.skipProperty()
            "endbit" -> endBit = parser.parseInteger() ?: return parser.skipProperty()
            "basevar" -> baseVar = parser.parseReference(SymbolType.VarPlayer) ?: return parser.skipProperty()
            else -> parser.unknownProperty()
        }
    }

    override fun verifyProperties(parser: Parser) {
        // Do nothing.
    }

    override fun resolveReferences(compiler: Compiler) {
        if (baseVar is Reference) {
            baseVar = compiler.resolveReference(baseVar as Reference)
        }
    }

    override fun encode(): ByteArray {
        val packet = BinaryEncoder(6)
        packet.code(1) {
            write2(baseVar as Int)
            write1(startBit)
            write1(endBit)
        }
        packet.terminateCode()
        return packet.toByteArray()
    }
}