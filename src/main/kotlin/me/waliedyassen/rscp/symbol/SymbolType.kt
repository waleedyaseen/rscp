package me.waliedyassen.rscp.symbol

import me.waliedyassen.rscp.format.config.Config
import me.waliedyassen.rscp.format.config.EnumConfig
import me.waliedyassen.rscp.format.config.InvConfig
import me.waliedyassen.rscp.format.config.ParamConfig
import me.waliedyassen.rscp.format.config.StructConfig
import me.waliedyassen.rscp.format.config.VarbitConfig
import me.waliedyassen.rscp.format.config.VarcConfig
import me.waliedyassen.rscp.format.config.VarpConfig
import me.waliedyassen.rscp.format.graphic.GraphicConfig
import me.waliedyassen.rscp.format.iftype.Component

/**
 * A primitive symbol type that cannot be stored in a symbol table.
 */
open class PrimitiveSymbolType(legacyChar: kotlin.Char, literal: kotlin.String) :
    SymbolType<BasicSymbol>(legacyChar, literal, serializer = BasicSymbolSerializer)

/**
 * The base symbol type.
 */
open class SymbolType<T : Symbol>(
    val legacyChar: kotlin.Char,
    val literal: kotlin.String,
    val constructor: ((kotlin.String) -> Config)? = null,
    val serializer: SymbolSerializer<T>,
    val extension: kotlin.String? = literal
) {
    /**
     * Checks whether this symbol type can be referenced.
     */
    fun isReference() = when (this) {
        ClientScript,
        Seq,
        LocShape,
        Component,
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
    object ClientScript :
        SymbolType<ClientScriptSymbol>(0.toChar(), "clientscript", null, ClientScriptSymbolSerializer)

    object Int : PrimitiveSymbolType('i', "int")
    object Boolean : PrimitiveSymbolType('1', "boolean")
    object Seq : PrimitiveSymbolType('A', "seq")
    object LocShape : PrimitiveSymbolType('H', "locshape")
    object Component : SymbolType<BasicSymbol>('I', "component", ::Component, BasicSymbolSerializer, extension = null)
    object NamedObj : PrimitiveSymbolType('O', "namedobj")
    object Synth : PrimitiveSymbolType('P', "synth")
    object Area : PrimitiveSymbolType('R', "area")
    object Stat : PrimitiveSymbolType('S', "stat")
    object NpcStat : PrimitiveSymbolType('T', "npc_stat")
    object MapArea : PrimitiveSymbolType('`', "wma")
    object CoordGrid : PrimitiveSymbolType('c', "coord")
    object Graphic : SymbolType<BasicSymbol>('d', "graphic", ::GraphicConfig, BasicSymbolSerializer)
    object FontMetrics : PrimitiveSymbolType('f', "fontmetrics")
    object Enum : SymbolType<TypedSymbol>('g', "enum", ::EnumConfig, TypedSymbolSerializer)
    object Loc : PrimitiveSymbolType('l', "loc")
    object Model : PrimitiveSymbolType('m', "model")
    object Npc : PrimitiveSymbolType('n', "npc")
    object Obj : PrimitiveSymbolType('o', "obj")
    object String : PrimitiveSymbolType('s', "string")
    object Spotanim : PrimitiveSymbolType('t', "spotanim")
    object NpcUid : PrimitiveSymbolType('u', "npc_uid")
    object Inv : SymbolType<BasicSymbol>('v', "inv", ::InvConfig, BasicSymbolSerializer)
    object Category : PrimitiveSymbolType('y', "category")
    object Char : PrimitiveSymbolType('z', "char")
    object MapElement : PrimitiveSymbolType('µ', "mapelement")
    object Interface : PrimitiveSymbolType('a', "interface")
    object TopLevelInterface : PrimitiveSymbolType('F', "toplevelinterface")
    object OverlayInterface : PrimitiveSymbolType('L', "overlayinterface")
    object ClientInterface : PrimitiveSymbolType('©', "clientinterface")
    object NewVar : PrimitiveSymbolType('-', "newvar")
    object VarPlayer : SymbolType<TypedSymbol>('\u0000', "varp", ::VarpConfig, TypedSymbolSerializer)
    object VarClient : SymbolType<TypedSymbol>('\u0000', "varc", ::VarcConfig, TypedSymbolSerializer)
    object VarBit : SymbolType<BasicSymbol>('\u0000', "varbit", ::VarbitConfig, BasicSymbolSerializer)
    object Param : SymbolType<ConfigSymbol>('\u0000', "param", ::ParamConfig, ConfigSymbolSerializer)
    object Struct : SymbolType<BasicSymbol>('J', "struct", ::StructConfig, BasicSymbolSerializer)
    object DbRow : PrimitiveSymbolType('Ð', "dbrow")
    object Constant : SymbolType<ConstantSymbol>('^', "constant", null, ConstantSymbolSerializer)


    companion object {

        /**
         * A list of all the existing [SymbolType] instances.
         */
        val values = listOf(
            ClientScript,
            Int,
            Boolean,
            Seq,
            LocShape,
            Component,
            NamedObj,
            Synth,
            Area,
            Stat,
            NpcStat,
            MapArea,
            CoordGrid,
            Graphic,
            FontMetrics,
            Enum,
            Loc,
            Model,
            Npc,
            Obj,
            String,
            Spotanim,
            NpcUid,
            Inv,
            Category,
            Char,
            MapElement,
            Interface,
            TopLevelInterface,
            OverlayInterface,
            ClientInterface,
            NewVar,
            VarPlayer,
            VarClient,
            VarBit,
            Param,
            Struct,
            DbRow,
            Constant,
        )

        /**
         * A look-up by literal map for [SymbolType].
         */
        private val lookupByLiteral = values
            .filter { it.literal.isNotBlank() }
            .associateBy { it.literal }
        /**
         * A look-up by literal map for [SymbolType].
         */
        private val lookupByExtension = values
            .filter { it.extension != null }
            .associateBy { it.extension!! }

        /**
         * Looks-up for a [SymbolType] with the specified [extension].
         */
        fun lookupByExtensionOrNull(extension: kotlin.String): SymbolType<*>? = lookupByExtension[extension]

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