package me.waliedyassen.rscp.symbol

/**
 * The base class for all the symbols, it holds the shared information between
 * all the symbols such as [name] and [id].
 */
abstract class Symbol {

    /**
     * The name of the symbol stripped.
     */
    abstract val name: String

    /**
     * The generated ID of the symbol.
     */
    abstract val id: Int
}

/**
 * A bare minimum implementation of [Symbol].
 */
data class BasicSymbol(
    override val name: String,
    override val id: Int,
) : Symbol()

/**
 * A [Symbol] implementation that stores an additional [SymbolType].
 */
data class TypedSymbol(
    override val name: String,
    override val id: Int,
    val type: SymbolType<*>
) : Symbol()

/**
 * A [Symbol] implementation for constants, which store the value as is, in string form.
 */
data class ConstantSymbol(
    override val name: String,
    override val id: Int,
    val value: String,
) : Symbol()

/**
 * A [Symbol] implementation for constants, which store the value as is, in string form.
 */
data class ClientScriptSymbol(
    override val name: String,
    override val id: Int,
    val arguments: List<SymbolType<*>>,
) : Symbol()