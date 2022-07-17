package me.waliedyassen.rsconfig.config

import me.waliedyassen.rsconfig.binary.BinaryEncoder
import me.waliedyassen.rsconfig.parser.Parser
import me.waliedyassen.rsconfig.symbol.SymbolType

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
    private var baseVar = -1

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "startbit" -> startBit = parser.parseInteger()
            "endbit" -> endBit = parser.parseInteger()
            "basevar" -> baseVar = parser.parseReference(SymbolType.VarPlayer)
            else -> parser.unknownProperty()
        }
    }

    override fun verifyProperties(parser: Parser) {
        // Do nothing.
    }

    override fun encode(): ByteArray {
        val packet = BinaryEncoder(6)
        packet.code(1) {
            write2(baseVar)
            write1(startBit)
            write1(endBit)
        }
        packet.terminateCode()
        return packet.toByteArray()
    }
}