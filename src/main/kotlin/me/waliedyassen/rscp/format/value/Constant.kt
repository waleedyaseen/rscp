package me.waliedyassen.rscp.format.value

import me.waliedyassen.rscp.SymbolContributor
import me.waliedyassen.rscp.symbol.ConstantSymbol
import me.waliedyassen.rscp.symbol.SymbolType

/**
 * Holds information about single parsed constant entity.
 */
data class Constant(override val debugName: String, val value: String) : SymbolContributor {

    override val symbolType = SymbolType.Constant

    override fun createSymbol(id: Int) = ConstantSymbol(debugName, id, value)
}