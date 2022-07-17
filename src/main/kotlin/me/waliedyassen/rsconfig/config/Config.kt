package me.waliedyassen.rsconfig.config

import com.fasterxml.jackson.databind.JsonNode
import me.waliedyassen.rsconfig.CompilationContext
import me.waliedyassen.rsconfig.parser.Parser
import me.waliedyassen.rsconfig.symbol.BasicSymbol
import me.waliedyassen.rsconfig.symbol.Symbol
import me.waliedyassen.rsconfig.symbol.SymbolType

/**
 * The base class for all the configuration in the system.
 *
 * @author Walied K. Yassen
 */
abstract class Config(val name: String, val symbolType: SymbolType<*>) {

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

    open fun createSymbol(id: Int): Symbol = BasicSymbol(name, id)

    /**
     * Serializes the attributes of this configuration to binary format.
     */
    abstract fun encode(): ByteArray
}