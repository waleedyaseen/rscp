package me.waliedyassen.tomlrs.symbol

/**
 * Holds information about a single symbol in the system, a symbol represents information about
 * a single configuration entry.
 */
data class Symbol(val name: String, val id: Int, var content: SymbolType? = null)