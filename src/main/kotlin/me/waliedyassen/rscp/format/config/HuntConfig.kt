package me.waliedyassen.rscp.format.config

import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.Side
import me.waliedyassen.rscp.binary.BinaryEncoder
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.symbol.SymbolTable
import me.waliedyassen.rscp.symbol.SymbolType
import me.waliedyassen.rscp.util.LiteralEnum

enum class HuntTargetType(val id: Int, override val literal: String) : LiteralEnum {
    Player(0, "player"),
    Npc(1, "npc"),
    Scenery(2, "scenery"),
    Obj(3, "obj")
}

enum class HuntCheckVis(val id: Int, override val literal: String) : LiteralEnum {
    Off(0, "off"),
    LineOfSight(1, "lineofsight"),
    LineOfWalk(2, "lineofwalk")
}

enum class HuntCheckAfk(val id: Int, override val literal: String) : LiteralEnum {
    Off(0, "off"),
    On(1, "on")
}

enum class HuntCheckNotTooStrong(val id: Int, override val literal: String) : LiteralEnum {
    Off(0, "off"),
    On(1, "on"),
    OutsideWilderness(2, "outside_wilderness")
}

enum class HuntCheckNotBusy(val id: Int, override val literal: String) : LiteralEnum {
    Off(0, "off"),
    On(1, "on"),
}

enum class HuntFindKeepHunting(val id: Int, override val literal: String) : LiteralEnum {
    Off(0, "off"),
    On(1, "on"),
}

enum class HuntNobodyNear(val id: Int, override val literal: String) : LiteralEnum {
    KeepHunting(0, "keephunting"),
    PauseHunt(1, "pausehunt"),
}

class HuntConfig(override val debugName: String) : Config(SymbolType.Hunt) {

    var rate: Int? = null
    var targetType: HuntTargetType? = null
    var findNewMode: Any? = null
    var findKeepHunting: HuntFindKeepHunting? = null
    var nobodyNear: HuntNobodyNear? = null
    var checkVis: HuntCheckVis? = null
    var checkAfk: HuntCheckAfk? = null
    var checkNotTooStrong: HuntCheckNotTooStrong? = null
    var checkNotBusy: HuntCheckNotBusy? = null
    var checkNotCombat: Any? = null
    var checkNotCombatSelf: Any? = null

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "rate" -> rate = parser.parseInteger() ?: return parser.skipProperty()
            "type" -> targetType = parser.parseEnumLiteral() ?: return parser.skipProperty()
            "find_newmode" -> findNewMode = parser.parseDynamic(SymbolType.NpcMode) ?: return parser.skipProperty()
            "find_keephunting" -> findKeepHunting = parser.parseEnumLiteral() ?: return parser.skipProperty()
            "nobodynear " -> nobodyNear = parser.parseEnumLiteral() ?: return parser.skipProperty()
            "check_vis" -> checkVis = parser.parseEnumLiteral() ?: return parser.skipProperty()
            "check_afk" -> checkAfk = parser.parseEnumLiteral() ?: return parser.skipProperty()
            "check_nottoostrong" -> checkNotTooStrong = parser.parseEnumLiteral() ?: return parser.skipProperty()
            "check_notbusy" -> checkNotBusy = parser.parseEnumLiteral() ?: return parser.skipProperty()
            "check_notcombat" -> {
                when (val type = targetType) {
                    HuntTargetType.Player -> checkNotCombat = parser.parseVar(SymbolType.VarPlayer)
                    HuntTargetType.Npc -> checkNotCombat = parser.parseVar(SymbolType.VarNpc)
                    null -> parser.reportPropertyError("You need to define 'type' property first.")
                    else -> parser.reportPropertyError("'check_notcombat' property is not allowed for target of type '${type.literal}'.")
                }
            }

            "check_notcombat_self" -> checkNotCombatSelf = parser.parseVar(SymbolType.VarNpc)
            else -> parser.unknownProperty()
        }
    }

    override fun verifyProperties(parser: Parser) {
        if (targetType == null) {
            parser.reportUnitError("Hunt configuration must define 'type' property")
        }
        if (findNewMode == null) {
            parser.reportUnitError("Hunt configuration must define 'find_newmode' property")
        }
    }

    override fun resolveReferences(compiler: Compiler) {
        compiler.resolveReference(::findNewMode)
        compiler.resolveReference(::checkNotCombat)
        compiler.resolveReference(::checkNotCombatSelf)
    }

    override fun encode(side: Side, sym: SymbolTable): ByteArray {
        val encoder = BinaryEncoder(1)
        if (side == Side.Server) {
            if (rate != null) {
                encoder.code(1)
                encoder.write1(rate as Int)
            }
            if (targetType != null) {
                encoder.code(2)
                encoder.write1((targetType as HuntTargetType).id)
            }
            if (findNewMode != null) {
                encoder.code(3)
                encoder.write1(findNewMode as Int)
            }
            if (findKeepHunting != null) {
                encoder.code(4)
                encoder.write1((findKeepHunting as HuntFindKeepHunting).id)
            }
            if (nobodyNear != null) {
                encoder.code(5)
                encoder.write1((nobodyNear as HuntNobodyNear).id)
            }
            if (checkVis != null) {
                encoder.code(6)
                encoder.write1((checkVis as HuntCheckVis).id)
            }
            if (checkAfk != null) {
                encoder.code(7)
                encoder.write1((checkAfk as HuntCheckAfk).id)
            }
            if (checkNotTooStrong != null) {
                encoder.code(8)
                encoder.write1((checkNotTooStrong as HuntCheckNotTooStrong).id)
            }
            if (checkNotBusy != null) {
                encoder.code(9)
                encoder.write1((checkNotBusy as HuntCheckNotBusy).id)
            }
            if (checkNotCombat != null) {
                encoder.code(10)
                encoder.write2(checkNotCombat as Int)
            }
            if (checkNotCombatSelf != null) {
                encoder.code(11)
                encoder.write2(checkNotCombatSelf as Int)
            }
        }
        encoder.terminateCode()
        return encoder.toByteArray()
    }
}