package me.waliedyassen.rscp.format.config.varconfig.constants;

import me.waliedyassen.rscp.util.LiteralEnum

@Suppress("unused")
enum class VarTransmitLevel(val id: Int, override val literal: String) : LiteralEnum {
    No(0, "no"),
    Yes(1, "yes"),

    Never(0, "never"),
    Different(1, "different"),
    Always(2, "always")
    ;
}
