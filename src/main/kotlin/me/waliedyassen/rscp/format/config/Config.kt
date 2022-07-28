package me.waliedyassen.rscp.format.config

import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.symbol.BasicSymbol
import me.waliedyassen.rscp.symbol.Symbol
import me.waliedyassen.rscp.symbol.SymbolContributor
import me.waliedyassen.rscp.symbol.SymbolType

/**
 * The base class for all the configuration in the system.
 *
 * @author Walied K. Yassen
 */
abstract class Config(override val name: String, override val symbolType: SymbolType<*>) : SymbolContributor {

    /**
     * Parse a property with the specified [name] using the specified [Parser].
     */
    abstract fun parseProperty(name: String, parser: Parser)

    /**
     * Verify that all the parsed properties are valid and check for any
     * missing property.
     */
    abstract fun verifyProperties(parser: Parser)

    /**
     * Resolve all of the [me.waliedyassen.rscp.parser.Reference] objects
     * in this [Config] instance
     */
    abstract fun resolveReferences(compiler: Compiler)

    /**
     * Serializes the attributes of this configuration to binary format.
     */
    abstract fun encode(): ByteArray

    override fun createSymbol(id: Int): Symbol = BasicSymbol(name, id)
}