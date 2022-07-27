package me.waliedyassen.rscp.symbol

/**
 * An interface that contains all the common stuff necessary for contributing a [Symbol] to the symbol table.
 */
interface SymbolContributor {

    /**
     * The type of the symbol.
     */
    val symbolType: SymbolType<*>

    /**
     * The name of the symbol.
     */
    val name: String

    /**
     * Create a [Symbol] object that we can store in the symbol table.
     */
    fun createSymbol(id: Int): Symbol
}