package me.waliedyassen.tomlrs.config

import com.fasterxml.jackson.databind.JsonNode
import me.waliedyassen.tomlrs.CompilationContext
import me.waliedyassen.tomlrs.binary.BinaryEncoder
import me.waliedyassen.tomlrs.parser.Parser
import me.waliedyassen.tomlrs.symbol.SymbolType
import me.waliedyassen.tomlrs.util.asReference

/**
 * Implementation for 'varbit' type configuration.
 *
 * @author Walied K. Yassen
 */
class VarbitConfig(name: String) : Config(name, SymbolType.VAR_BIT) {

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

    override fun parseToml(node: JsonNode, context: CompilationContext) {
        startBit = node["startbit"].asInt()
        endBit = node["endbit"].asInt()
        baseVar = node["basevar"].asReference(SymbolType.VAR_PLAYER, context)
    }

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "startbit" -> startBit = parser.parseInteger()
            "endbit" -> endBit = parser.parseInteger()
            "basevar" -> baseVar = parser.parseReference(SymbolType.VAR_PLAYER)
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