package me.waliedyassen.rscp

import me.waliedyassen.rscp.symbol.Symbol
import me.waliedyassen.rscp.symbol.SymbolList
import me.waliedyassen.rscp.symbol.SymbolTable
import me.waliedyassen.rscp.symbol.SymbolType

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
    val debugName: String

    /**
     * Create a [Symbol] object that we can store in the symbol table.
     */
    fun createSymbol(id: Int): Symbol

    /**
     * Contribute a list of symbols
     */
    fun contributeSymbols(sym: SymbolTable) {
        val type = symbolType
        val name = debugName
        val old = sym.lookupSymbol(type, name)
        val id = old?.id ?: sym.generateId(type)
        val new = createSymbol(id)
        if (old != new) {
            @Suppress("UNCHECKED_CAST")
            val list = sym.lookupList(type) as SymbolList<Symbol>
            if (old != null) {
                list.remove(old)
            }
            list.add(new)
        }
    }
}