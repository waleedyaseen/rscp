package me.waliedyassen.rscp.format.config.varconfig.constants;

import me.waliedyassen.rscp.util.LiteralEnum

@Suppress("unused")
enum class VarLifetime(val id: Int, override val literal: String) : LiteralEnum {
    Temporary(0, "temp"),
    Permanent(1, "perm"),
    ServerPermanent(2, "serverperm");
}