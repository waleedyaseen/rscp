package me.waliedyassen.rsconfig.config

import me.waliedyassen.rsconfig.Compiler
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
     * Parse a property with the specified [name] using the specified [Parser].
     */
    abstract fun parseProperty(name: String, parser: Parser)

    /**
     * Verify that all the parsed properties are valid and check for any
     * missing property.
     */
    abstract fun verifyProperties(parser: Parser)

    /**
     * Resolve all of the [me.waliedyassen.rsconfig.parser.Reference] objects
     * in this [Config] instance
     */
    abstract fun resolveReferences(compiler: Compiler)

    /**
     * Create a [Symbol] object that we can store in the symbol table.
     */
    open fun createSymbol(id: Int): Symbol = BasicSymbol(name, id)

    /**
     * Serializes the attributes of this configuration to binary format.
     */
    abstract fun encode(): ByteArray
}