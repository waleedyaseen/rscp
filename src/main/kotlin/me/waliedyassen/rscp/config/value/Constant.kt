package me.waliedyassen.rscp.config.value

import me.waliedyassen.rscp.symbol.ConstantSymbol
import me.waliedyassen.rscp.symbol.SymbolContributor
import me.waliedyassen.rscp.symbol.SymbolType

/**
 * Holds information about single parsed constant entity.
 */
data class Constant(override val name: String, val value: String) : SymbolContributor {

    override val symbolType = SymbolType.Constant

    override fun createSymbol(id: Int) = ConstantSymbol(name, id, value)
}