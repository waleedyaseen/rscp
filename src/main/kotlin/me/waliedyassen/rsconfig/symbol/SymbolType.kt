package me.waliedyassen.rsconfig.symbol

import me.waliedyassen.rsconfig.config.*

/**
 * A primitive symbol type that cannot be stored in a symbol table.
 */
open class PrimitiveSymbolType(legacyChar: Char, literal: kotlin.String) :
    SymbolType<BasicSymbol>(legacyChar, literal, serializer = BasicSymbolSerializer)

/**
 * The base symbol type.
 */
open class SymbolType<T : Symbol>(
    val legacyChar: Char,
    val literal: kotlin.String,
    val constructor: (kotlin.String) -> Config = { error("Cannot construct symbol of type: $this") },
    val serializer: SymbolSerializer<T>
) {
    /**
     * Checks whether this symbol type can be referenced.
     */
    fun isReference() = when (this) {
        Enum -> true
        Inv -> true
        Struct -> true
        VarPlayer -> true
        VarClient -> true
        VarBit -> true
        Param -> true
        Obj -> true
        else -> false
    }

    object Int : PrimitiveSymbolType('i', "int")
    object Boolean : PrimitiveSymbolType('1', "boolean")
    object String : PrimitiveSymbolType('s', "string")
    object Obj : PrimitiveSymbolType('\u0000', "obj")
    object Enum : SymbolType<TypedSymbol>('g', "enum", ::EnumConfig, TypedSymbolSerializer)
    object VarPlayer : SymbolType<TypedSymbol>('\u0000', "varp", ::VarpConfig, TypedSymbolSerializer)
    object VarClient : SymbolType<TypedSymbol>('\u0000', "varc", ::VarcConfig, TypedSymbolSerializer)
    object VarBit : SymbolType<BasicSymbol>('\u0000', "varbit", ::VarbitConfig, BasicSymbolSerializer)
    object Param : SymbolType<TypedSymbol>('\u0000', "param", ::ParamConfig, TypedSymbolSerializer)
    object Inv : SymbolType<BasicSymbol>('\u0000', "inv", ::InvConfig, BasicSymbolSerializer)
    object Struct : SymbolType<BasicSymbol>('j', "struct", ::StructConfig, BasicSymbolSerializer)


    companion object {

        /**
         * A list of all the existing [SymbolType] instances.
         */
        val values = listOf(
            Int,
            Boolean,
            String,
            Obj,
            Enum,
            VarPlayer,
            VarClient,
            VarBit,
            Param,
            Inv,
            Struct
        )

        /**
         * A look-up by literal map for [SymbolType].
         */
        private val lookupByLiteral = values.associateBy { it.literal }

        /**
         * Looks-up for a [SymbolType] with the specified [literal].
         */
        fun lookupOrNull(literal: kotlin.String): SymbolType<*>? = lookupByLiteral[literal]

        /**
         * Looks-up for a [SymbolType] with the specified [literal].
         */
        fun lookup(literal: kotlin.String) = lookupOrNull(literal) ?: error("Could not match $literal to a symbol type")
    }
}