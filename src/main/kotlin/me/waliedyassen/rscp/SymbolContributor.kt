package me.waliedyassen.rscp

import me.waliedyassen.rscp.symbol.*

/**
 * An interface that contains all the common stuff necessary for contributing a [Symbol] to the symbol table.
 */
interface SymbolContributor<T : Symbol> {

    /**
     * The type of the symbol.
     */
    val symbolType: SymbolType<out T>

    /**
     * The name of the symbol.
     */
    val debugName: String

    /**
     * Create a [T] object that we can store in the symbol table.
     */
    fun createSymbol(id: Int): T

    /**
     * Contribute a list of symbols
     */
    fun contributeSymbols(sym: SymbolTable) {
        val type = symbolType
        val name = debugName
        val old = sym.lookupSymbol(type, name)
        val new = createSymbol(-1)
        if (old != new) {
            val list = sym.lookupList(type)
            if (old != null) {
                list.remove(old)
            }
            list.add(new)
        }
    }
}


/**
 * An interface for generating symbols that are associated with an ID.
 */
interface SymbolWithIdContributor<out T> : SymbolContributor<@UnsafeVariance T> where T : SymbolWithId {

    override fun contributeSymbols(sym: SymbolTable) {
        val type = symbolType
        val name = debugName
        val old = sym.lookupSymbol(type, name)
        val id = old?.id ?: sym.generateId(type)
        val new = createSymbol(id)
        if (old != new) {
            val list = sym.lookupList(type)
            if (old != null) {
                list.remove(old)
            }
            list.add(new)
        }
    }
}