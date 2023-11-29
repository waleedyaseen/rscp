package me.waliedyassen.rscp.format.config.varconfig.constants

import me.waliedyassen.rscp.symbol.SymbolType
import me.waliedyassen.rscp.symbol.SymbolWithId

enum class VarDomainType(val type: SymbolType<out SymbolWithId>, val permitClientTransmitLevel: Boolean) {
    Player(SymbolType.VarPlayer, true),
    Client(SymbolType.VarClient, true),
    ;
}