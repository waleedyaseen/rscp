package me.waliedyassen.rscp.symbol

import me.waliedyassen.rscp.config.Config
import me.waliedyassen.rscp.config.EnumConfig
import me.waliedyassen.rscp.config.InvConfig
import me.waliedyassen.rscp.config.ParamConfig
import me.waliedyassen.rscp.config.StructConfig
import me.waliedyassen.rscp.config.VarbitConfig
import me.waliedyassen.rscp.config.VarcConfig
import me.waliedyassen.rscp.config.VarpConfig

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
    val serializer: SymbolSerializer<T>,
) {
    /**
     * Checks whether this symbol type can be referenced.
     */
    fun isReference() = when (this) {
        Seq,
        LocShape,
        Stat,
        NpcStat,
        Obj,
        FontMetrics,
        Model,
        Enum,
        VarPlayer,
        VarClient,
        VarBit,
        Param,
        Inv,
        Struct -> true
        else -> false
    }

    object Undefined : PrimitiveSymbolType(0.toChar(), "")
    object ClientScript : PrimitiveSymbolType(0.toChar(), "")
    object Int : PrimitiveSymbolType('i', "int")
    object Boolean : PrimitiveSymbolType('1', "boolean")
    object Seq : PrimitiveSymbolType('A', "seq")
    object LocShape : PrimitiveSymbolType('H', "loc_shape")
    object Stat : PrimitiveSymbolType('S', "stat")
    object NpcStat : PrimitiveSymbolType('T', "npc_stat")
    object Obj : PrimitiveSymbolType('o', "obj")
    object String : PrimitiveSymbolType('s', "string")
    object Graphic : PrimitiveSymbolType('d', "graphic")
    object FontMetrics : PrimitiveSymbolType('f', "fontmetrics")
    object Model : PrimitiveSymbolType('m', "model")
    object Enum : SymbolType<TypedSymbol>('g', "enum", ::EnumConfig, TypedSymbolSerializer)
    object VarPlayer : SymbolType<TypedSymbol>('\u0000', "varp", ::VarpConfig, TypedSymbolSerializer)
    object VarClient : SymbolType<TypedSymbol>('\u0000', "varc", ::VarcConfig, TypedSymbolSerializer)
    object VarBit : SymbolType<BasicSymbol>('\u0000', "varbit", ::VarbitConfig, BasicSymbolSerializer)
    object Param : SymbolType<TypedSymbol>('\u0000', "param", ::ParamConfig, TypedSymbolSerializer)
    object Inv : SymbolType<BasicSymbol>('v', "inv", ::InvConfig, BasicSymbolSerializer)
    object Struct : SymbolType<BasicSymbol>('J', "struct", ::StructConfig, BasicSymbolSerializer)
    object Constant : SymbolType<ConstantSymbol>('^', "constant", { error("") }, ConstantSymbolSerializer)


    companion object {

        /**
         * A list of all the existing [SymbolType] instances.
         */
        val values = listOf(
            Int,
            Boolean,
            Seq,
            LocShape,
            Stat,
            NpcStat,
            Obj,
            String,
            Graphic,
            FontMetrics,
            Model,
            Enum,
            VarPlayer,
            VarClient,
            VarBit,
            Param,
            Inv,
            Struct,
            Constant,
        )

        /**
         * A look-up by literal map for [SymbolType].
         */
        private val lookupByLiteral = values
            .filter { it.literal.isNotBlank() }
            .associateBy { it.literal }

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