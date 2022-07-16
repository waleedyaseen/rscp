package me.waliedyassen.tomlrs.config

import com.fasterxml.jackson.databind.JsonNode
import me.waliedyassen.tomlrs.CompilationContext
import me.waliedyassen.tomlrs.parser.Parser
import me.waliedyassen.tomlrs.symbol.SymbolType

/**
 * The base class for all the configuration in the system.
 *
 * @author Walied K. Yassen
 */
abstract class Config(val name: String, val symbolType: SymbolType) {

    /**
     * Parses the attributes of the configuration from a toml configuration node.
     */
    abstract fun parseToml(node: JsonNode, context: CompilationContext)

    open fun parseProperty(name: String, parser: Parser) {
        TODO("Unimplemented type: $this")
    }

    open fun verifyProperties(parser: Parser) {
        TODO("Unimplemented type: $this")
    }

    /**
     * Serializes the attributes of this configuration to binary format.
     */
    abstract fun encode(): ByteArray
}