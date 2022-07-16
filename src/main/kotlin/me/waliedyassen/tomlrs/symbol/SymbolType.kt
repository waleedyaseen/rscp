package me.waliedyassen.tomlrs.symbol

import me.waliedyassen.tomlrs.config.*

/**
 * Each instance holds information about single configuration type in the system.
 *
 * @author Walied K. Yassen
 */
enum class SymbolType(
    val char: Char,
    val literal: String,
    val supplier: (String) -> Config = { error("Cannot construct type") }
) {
    INT('i', "int"),
    BOOLEAN('1', "boolean"),
    STRING('s', "string"),
    ENUM('g', "enum", ::EnumConfig),
    VAR_PLAYER('\u0000', "varp", ::VarpConfig),
    INV('\u0000', "inv", ::InvConfig),
    STRUCT('J', "struct", ::StructConfig),
    VAR_BIT('\u0000', "varbit", ::VarbitConfig),
    PARAM('\u0000', "param", ::ParamConfig),
    VARC('\u0000', "varc", ::VarcConfig),
    ;

    companion object {

        /**
         * A look-up map for [SymbolType] by their [SymbolType.literal].
         */
        private val lookupByLiteral = values().associateBy { it.literal }

        /**
         * Looks-up for a [SymbolType] with the specified [literal].
         */
        fun lookupOrNull(literal: String) = lookupByLiteral[literal]

        /**
         * Looks-up for a [SymbolType] with the specified [literal].
         */
        fun lookup(literal: String) = lookupOrNull(literal) ?: error("Could not match $literal to a symbol type")
    }
}