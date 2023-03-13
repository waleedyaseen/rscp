package me.waliedyassen.rscp.symbol

import me.waliedyassen.rscp.format.config.*
import me.waliedyassen.rscp.format.dbtable.DbRowConfig
import me.waliedyassen.rscp.format.dbtable.DbTableConfig
import me.waliedyassen.rscp.format.graphic.GraphicConfig
import me.waliedyassen.rscp.format.iftype.Component

/**
 * A primitive symbol type that cannot be stored in a symbol table.
 */
open class PrimitiveSymbolType(id: kotlin.Int, legacyChar: kotlin.Char, literal: kotlin.String) :
    SymbolType<BasicSymbol>(id, legacyChar, literal, serializer = BasicSymbolSerializer)

/**
 * The base symbol type.
 */
open class SymbolType<T : Symbol>(
    val id: kotlin.Int,
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
        Texture,
        Synth,
        Struct,
        Interface,
        OverlayInterface,
        TopLevelInterface,
        DbRow,
        DbTable,
        Flo -> true
        else -> false
    }

    object Undefined : PrimitiveSymbolType(-1, 0.toChar(), "")
    object ClientScript :
        SymbolType<ClientScriptSymbol>(-1, 0.toChar(), "clientscript", null, ClientScriptSymbolSerializer)

    object Int : PrimitiveSymbolType(0, 'i', "int")
    object Boolean : PrimitiveSymbolType(1, '1', "boolean")
    object Seq : PrimitiveSymbolType(6, 'A', "seq")
    object LocShape : PrimitiveSymbolType(8, 'H', "locshape")
    object Component :
        SymbolType<BasicSymbol>(9, 'I', "component", ::Component, BasicSymbolSerializer, extension = null)

    object NamedObj : PrimitiveSymbolType(13, 'O', "namedobj")
    object Synth : PrimitiveSymbolType(14, 'P', "synth")
    object Area : PrimitiveSymbolType(16, 'R', "area")
    object Stat : PrimitiveSymbolType(17, 'S', "stat")
    object NpcStat : PrimitiveSymbolType(18, 'T', "npc_stat")
    object MapArea : PrimitiveSymbolType(21, '`', "wma")
    object CoordGrid : PrimitiveSymbolType(22, 'c', "coord")
    object Graphic : SymbolType<BasicSymbol>(23, 'd', "graphic", ::GraphicConfig, BasicSymbolSerializer)
    object FontMetrics : PrimitiveSymbolType(25, 'f', "fontmetrics")
    object Enum : SymbolType<TypedSymbol>(26, 'g', "enum", ::EnumConfig, TypedSymbolSerializer)
    object Loc : SymbolType<BasicSymbol>(30, 'l', "loc", ::LocConfig, BasicSymbolSerializer)
    object Model : PrimitiveSymbolType(31, 'm', "model")
    object Npc : PrimitiveSymbolType(32, 'n', "npc")
    object Obj : PrimitiveSymbolType(33, 'o', "obj")
    object String : PrimitiveSymbolType(36, 's', "string")
    object Spotanim : PrimitiveSymbolType(37, 't', "spotanim")
    object NpcUid : PrimitiveSymbolType(38, 'u', "npc_uid")
    object Inv : SymbolType<BasicSymbol>(39, 'v', "inv", ::InvConfig, BasicSymbolSerializer)
    object Texture : PrimitiveSymbolType(40, 'x', "texture")
    object Category : PrimitiveSymbolType(41, 'y', "category")
    object Char : PrimitiveSymbolType(42, 'z', "char")
    object MapElement : PrimitiveSymbolType(59, 'µ', "mapelement")
    object Interface : PrimitiveSymbolType(97, 'a', "interface")
    object TopLevelInterface : PrimitiveSymbolType(98, 'F', "toplevelinterface")
    object OverlayInterface : PrimitiveSymbolType(99, 'L', "overlayinterface")
    object ClientInterface : PrimitiveSymbolType(100, '©', "clientinterface")
    object NewVar : PrimitiveSymbolType(-1, '-', "newvar")
    object VarPlayer : SymbolType<TypedSymbol>(-1, '\u0000', "varp", ::VarpConfig, TypedSymbolSerializer)
    object VarClient : SymbolType<TypedSymbol>(-1, '\u0000', "varc", ::VarcConfig, TypedSymbolSerializer)
    object VarBit : SymbolType<BasicSymbol>(-1, '\u0000', "varbit", ::VarbitConfig, BasicSymbolSerializer)
    object Param : SymbolType<ConfigSymbol>(-1, '\u0000', "param", ::ParamConfig, ConfigSymbolSerializer)
    object Struct : SymbolType<BasicSymbol>(73, 'J', "struct", ::StructConfig, BasicSymbolSerializer)
    object DbRow : SymbolType<BasicSymbol>(74, 'Ð', "dbrow", ::DbRowConfig, BasicSymbolSerializer)
    object Constant : SymbolType<ConstantSymbol>(-1, '\u0000', "constant", null, ConstantSymbolSerializer)
    object AutoInt : PrimitiveSymbolType(-1, '\u0000', "autoint")
    object DbTable : SymbolType<BasicSymbol>(-1, '\u0000', "dbtable", ::DbTableConfig, BasicSymbolSerializer)
    object DbColumn : SymbolType<DbColumnSymbol>(-1, '\u0000', "dbcolumn", null, DbColumnSymbolSerializer)
    object Flo : SymbolType<BasicSymbol>(-1, '\u0000', "flo", ::FloConfig, BasicSymbolSerializer)

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
            Texture,
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
            DbTable,
            DbColumn,
            Constant,
            Flo,
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