package me.waliedyassen.rscp.config.value

import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.config.Config
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.symbol.SymbolType

/**
 * Implementation of [Config] for constant reference.
 */
// TODO(Walied): Remove the config implementation and abstract out the functionality
class ConstantValue(name: String) : Config(name, SymbolType.Constant) {

    override fun parseProperty(name: String, parser: Parser) {
        // Do nothing.
    }

    override fun verifyProperties(parser: Parser) {
        // Do nothing.
    }

    override fun resolveReferences(compiler: Compiler) {
        // Do nothing.
    }

    override fun encode() = error("Unsupported operation")
}