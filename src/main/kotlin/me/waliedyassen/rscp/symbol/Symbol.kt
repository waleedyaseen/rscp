package me.waliedyassen.rscp.symbol

import me.waliedyassen.rscp.format.dbtable.DbColumnProp

/**
 * The base class for all the symbols, it holds the shared information between
 * all the symbols such as [name] and [id].
 */
abstract class Symbol : Comparable<Symbol> {

    /**
     * The name of the symbol stripped.
     */
    abstract val name: String

    override fun compareTo(other: Symbol): Int {
        return name.compareTo(other.name)
    }
}

/**
 * A [Symbol] that is associated with a unique ID for each symbol.
 */
abstract class SymbolWithId : Symbol() {

    /**
     * The generated ID of the symbol.
     */
    abstract val id: Int

    override fun compareTo(other: Symbol): Int {
        if (other is SymbolWithId) {
            return id.compareTo(other.id)
        }
        return super.compareTo(other)
    }
}

/**
 * A bare minimum implementation of [Symbol].
 */
data class BasicSymbol(
    override val name: String,
    override val id: Int,
) : SymbolWithId()

/**
 * A [Symbol] implementation that stores an additional [SymbolType].
 */
data class TypedSymbol(
    override val name: String,
    override val id: Int,
    val type: SymbolType<*>
) : SymbolWithId()

/**
 * A [Symbol] implementation that stores an additional [SymbolType] and transmit
 * boolean property.
 */
data class ConfigSymbol(
    override val name: String,
    override val id: Int,
    val type: SymbolType<*>,
    val transmit: Boolean
) : SymbolWithId()

/**
 * A [Symbol] implementation for constants, which store the value as is, in string form.
 */
data class ConstantSymbol(
    override val name: String,
    val value: String,
) : Symbol()

/**
 * A [Symbol] implementation for constants, which store the value as is, in string form.
 */
data class ClientScriptSymbol(
    override val name: String,
    override val id: Int,
    val arguments: List<SymbolType<*>>,
) : SymbolWithId()

/**
 * A [Symbol] implementation for "dbcolumns".
 */
data class DbColumnSymbol(
    override val name: String,
    override val id: Int,
    val types: List<SymbolType<*>>,
    val props: Set<DbColumnProp>
) : SymbolWithId()