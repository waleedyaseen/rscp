package me.waliedyassen.rscp.format.config

import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.Side
import me.waliedyassen.rscp.binary.BinaryEncoder
import me.waliedyassen.rscp.binary.codeParams
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.parser.Reference
import me.waliedyassen.rscp.parser.parseParam
import me.waliedyassen.rscp.symbol.SymbolTable
import me.waliedyassen.rscp.symbol.SymbolType

data class LocModel(var shape: Any, var model: Any)

class LocConfig(override val debugName: String) : Config(SymbolType.Loc) {

    var models = mutableListOf<LocModel>()
    var lowModels = mutableListOf<LocModel>()
    var locName: String? = null
    var recolSrc = arrayOfNulls<Int>(6)
    var recolDst = arrayOfNulls<Int>(6)
    var retexSrc = arrayOfNulls<Any>(6)
    var retexDst = arrayOfNulls<Any>(6)
    var width: Int? = null
    var length: Int? = null
    var blockWalk: Boolean? = null
    var blockRange: Boolean? = null
    var active: Boolean? = null
    var hillskew = false
    var hillChange: Int? = null
    var category: Any? = 0
    var sharelight = false
    var occlude = false
    var anim: Any? = null
    var wallOffset: Int? = null
    var ambient: Int? = null
    var contrast: Int? = null
    var options = arrayOfNulls<String>(5)
    var mapFunction = -1
    var mapScene = -1
    var mirror = false
    var softShadows = true
    var resizeX: Int? = null
    var resizeY: Int? = null
    var resizeZ: Int? = null
    var blockSides = 0
    var xof: Int? = null
    var yof: Int? = null
    var zof: Int? = null
    var forceDecor = false
    var placeObjs: Boolean? = null
    var breakRouteFinding = false
    var multiVar = 0
    var multiVarbit = 0
    var bgSoundId: Any? = null
    var bgSoundDelayMin: Int? = null
    var bgSundDelayMax: Int? = null
    var bgSoundRange = 0
    var randseq = false
    var params = LinkedHashMap<Int, Any>()

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "model" -> {
                val shape = parser.parseDynamic(SymbolType.LocShape) ?: return parser.skipProperty()
                parser.parseComma() ?: return parser.skipProperty()
                val model = parser.parseDynamic(SymbolType.Model) ?: return parser.skipProperty()
                models.add(LocModel(shape, model))
            }

            "lowmodel" -> {
                val shape = parser.parseDynamic(SymbolType.LocShape) ?: return parser.skipProperty()
                parser.parseComma() ?: return parser.skipProperty()
                val model = parser.parseDynamic(SymbolType.Model) ?: return parser.skipProperty()
                lowModels.add(LocModel(shape, model))
            }

            "name" -> locName = parser.parseString()
            "width" -> width = parser.parseInteger() ?: return parser.skipProperty()
            "length" -> length = parser.parseInteger() ?: return parser.skipProperty()
            "blockwalk" -> blockWalk = parser.parseBoolean()
            "blockrange" -> blockRange = parser.parseBoolean()
            "active" -> active = parser.parseBoolean()
            "hillskew" -> hillskew = parser.parseBoolean()
            "sharelight" -> sharelight = parser.parseBoolean()
            "occlude" -> occlude = parser.parseBoolean()
            "anim" -> anim = parser.parseDynamic(SymbolType.Seq) ?: return parser.skipProperty()
            "walloff" -> wallOffset = parser.parseInteger() ?: return parser.skipProperty()
            "ambient" -> ambient = parser.parseInteger() ?: return parser.skipProperty()
            "contrast" -> contrast = parser.parseInteger() ?: return parser.skipProperty()
            "op1" -> options[0] = parser.parseString()
            "op2" -> options[1] = parser.parseString()
            "op3" -> options[2] = parser.parseString()
            "op4" -> options[3] = parser.parseString()
            "op5" -> options[4] = parser.parseString()
            "recol1s" -> recolSrc[0] = parser.parseInteger() ?: return parser.skipProperty()
            "recol1d" -> recolDst[0] = parser.parseInteger() ?: return parser.skipProperty()
            "recol2s" -> recolSrc[1] = parser.parseInteger() ?: return parser.skipProperty()
            "recol2d" -> recolDst[1] = parser.parseInteger() ?: return parser.skipProperty()
            "recol3s" -> recolSrc[2] = parser.parseInteger() ?: return parser.skipProperty()
            "recol3d" -> recolDst[2] = parser.parseInteger() ?: return parser.skipProperty()
            "recol4s" -> recolSrc[3] = parser.parseInteger() ?: return parser.skipProperty()
            "recol4d" -> recolDst[3] = parser.parseInteger() ?: return parser.skipProperty()
            "recol5s" -> recolSrc[4] = parser.parseInteger() ?: return parser.skipProperty()
            "recol5d" -> recolDst[4] = parser.parseInteger() ?: return parser.skipProperty()
            "recol6s" -> recolSrc[5] = parser.parseInteger() ?: return parser.skipProperty()
            "recol6d" -> recolDst[5] = parser.parseInteger() ?: return parser.skipProperty()
            "retex1s" -> retexSrc[0] = parser.parseDynamic(SymbolType.Texture) ?: return parser.skipProperty()
            "retex1d" -> retexDst[0] = parser.parseDynamic(SymbolType.Texture) ?: return parser.skipProperty()
            "retex2s" -> retexSrc[1] = parser.parseDynamic(SymbolType.Texture) ?: return parser.skipProperty()
            "retex2d" -> retexDst[1] = parser.parseDynamic(SymbolType.Texture) ?: return parser.skipProperty()
            "retex3s" -> retexSrc[2] = parser.parseDynamic(SymbolType.Texture) ?: return parser.skipProperty()
            "retex3d" -> retexDst[2] = parser.parseDynamic(SymbolType.Texture) ?: return parser.skipProperty()
            "retex4s" -> retexSrc[3] = parser.parseDynamic(SymbolType.Texture) ?: return parser.skipProperty()
            "retex4d" -> retexDst[3] = parser.parseDynamic(SymbolType.Texture) ?: return parser.skipProperty()
            "retex5s" -> retexSrc[4] = parser.parseDynamic(SymbolType.Texture) ?: return parser.skipProperty()
            "retex5d" -> retexDst[4] = parser.parseDynamic(SymbolType.Texture) ?: return parser.skipProperty()
            "retex6s" -> retexSrc[5] = parser.parseDynamic(SymbolType.Texture) ?: return parser.skipProperty()
            "retex6d" -> retexDst[5] = parser.parseDynamic(SymbolType.Texture) ?: return parser.skipProperty()
            "category" -> category = parser.parseDynamic(SymbolType.Category) ?: return parser.skipProperty()
            "mirror" -> mirror = parser.parseBoolean()
            "softshadows" -> softShadows = parser.parseBoolean()
            "resizex" -> resizeX = parser.parseInteger() ?: return parser.skipProperty()
            "resizey" -> resizeY = parser.parseInteger() ?: return parser.skipProperty()
            "resizez" -> resizeZ = parser.parseInteger() ?: return parser.skipProperty()
            "mapscene" -> mapScene = parser.parseInteger() ?: return parser.skipProperty()
            "blocknorth" -> blockSides = blockSides or (if (parser.parseBoolean()) 0x1 else 0x0)
            "blockeast" -> blockSides = blockSides or (if (parser.parseBoolean()) 0x2 else 0x0)
            "blocksouth" -> blockSides = blockSides or (if (parser.parseBoolean()) 0x4 else 0x0)
            "blockwest" -> blockSides = blockSides or (if (parser.parseBoolean()) 0x8 else 0x0)
            "xof" -> xof = parser.parseInteger() ?: return parser.skipProperty()
            "yof" -> yof = parser.parseInteger() ?: return parser.skipProperty()
            "zof" -> zof = parser.parseInteger() ?: return parser.skipProperty()
            // TODO: Multi loc
            "forcedecor" -> forceDecor = parser.parseBoolean()
            "breakroutefinding" -> breakRouteFinding = parser.parseBoolean()
            "placeobjs" -> placeObjs = parser.parseBoolean()
            "mapfunction" -> mapFunction = parser.parseInteger() ?: return parser.skipProperty()
            "hillchange" -> hillChange = parser.parseInteger() ?: return parser.skipProperty()
            "randseq" -> randseq = parser.parseBoolean()
            "bgsound" -> bgSoundId = parser.parseDynamic(SymbolType.Synth) ?: return parser.skipProperty()
            "bgsoundrange" -> bgSoundRange = parser.parseInteger() ?: return parser.skipProperty()
            "param" -> parser.parseParam(params)
        }
    }

    override fun verifyProperties(parser: Parser) {
    }

    override fun resolveReferences(compiler: Compiler) {
        models.forEach {
            compiler.resolveReference(it::model)
            compiler.resolveReference(it::shape)
        }
        lowModels.forEach {
            compiler.resolveReference(it::model)
            compiler.resolveReference(it::shape)
        }
        if (anim != null) {
            compiler.resolveReference(::anim)
        }
        retexSrc.forEachIndexed { index, ref ->
            if (ref is Reference) {
                retexSrc[index] = compiler.resolveReference(ref, false)
            }
        }
        retexDst.forEachIndexed { index, ref ->
            if (ref is Reference) {
                retexDst[index] = compiler.resolveReference(ref, false)
            }
        }
        if (category != null) {
            compiler.resolveReference(::category)
        }
        if (bgSoundId != null) {
            compiler.resolveReference(::bgSoundId)
        }
    }

    override fun encode(side: Side, sym: SymbolTable): ByteArray {
        val packet = BinaryEncoder(6)
        if (side != Side.Server && !transmit) {
            packet.terminateCode()
            return packet.toByteArray()
        }
        if (locName != null) {
            packet.code(2)
            packet.writeString(locName!!)
        }
        if (width != null) {
            packet.code(14)
            packet.write1(width!!)
        }
        if (length != null) {
            packet.code(15)
            packet.write1(length!!)
        }
        if (blockWalk == false && blockRange == false) {
            packet.code(17)
        } else if (blockRange == false) {
            packet.code(18)
        }
        if (active != null) {
            packet.code(19)
            packet.write1(if (active == true) 1 else 0)
        }
        if (hillskew) {
            packet.code(21)
        }
        if (sharelight) {
            packet.code(22)
        }
        if (occlude) {
            packet.code(23)
        }
        if (anim != null) {
            packet.code(24)
            packet.write2(anim as Int)
        }
        if (blockWalk == true) {
            packet.code(27)
        }
        if (wallOffset != null) {
            packet.code(28)
            packet.code(wallOffset!!)
        }
        if (ambient != null) {
            packet.code(29)
            packet.write1(ambient!!.toInt())
        }
        if (contrast != null) {
            packet.code(39)
            packet.write1(contrast!!.toInt())
        }
        options.forEachIndexed { index, string ->
            if (string == null) return@forEachIndexed
            packet.code(30 + index)
            packet.writeString(string)
        }
        val recolorCount = recolSrc.count { it != null }
        if (recolorCount > 0) {
            packet.code(40)
            packet.write1(recolorCount)
            for (i in recolSrc.indices) {
                val src = recolSrc[i]
                val dst = recolDst[i]
                if (src == null || dst == null) {
                    check(src == null && dst == null)
                    continue
                }
                packet.write2(src)
                packet.write2(dst)
            }
        }
        val retextureCount = retexSrc.count { it != null }
        if (retextureCount > 0) {
            packet.code(40)
            packet.write1(retextureCount)
            for (i in retexSrc.indices) {
                val src = retexSrc[i]
                val dst = retexDst[i]
                if (src == null || dst == null) {
                    check(src == null && dst == null)
                    continue
                }
                packet.write2(src as Int)
                packet.write2(dst as Int)
            }
        }
        if (category != null) {
            packet.code(61)
            packet.write2(category as Int)
        }
        if (mirror) {
            packet.code(62)
        }
        if (!softShadows) {
            packet.code(64)
        }
        if (resizeX != null) {
            packet.code(65)
            packet.write2(resizeX!!)
        }
        if (resizeY != null) {
            packet.code(66)
            packet.write2(resizeY!!)
        }
        if (resizeZ != null) {
            packet.code(67)
            packet.write2(resizeZ!!)
        }
        if (mapScene != -1) {
            packet.code(68)
            packet.write2(mapScene)
        }
        if (blockSides != 0) {
            packet.code(69)
            packet.write2(blockSides)
        }
        if (xof != null) {
            packet.code(70)
            packet.write2(xof!!)
        }
        if (yof != null) {
            packet.code(71)
            packet.write2(yof!!)
        }
        if (zof != null) {
            packet.code(72)
            packet.write2(zof!!)
        }
        if (forceDecor) {
            packet.code(73)
        }
        if (breakRouteFinding) {
            packet.code(74)
        }
        if (placeObjs != null) {
            packet.code(75)
            packet.write1(if (placeObjs == true) 1 else 0)
        }
        if (bgSoundId != null) {
            packet.code(78)
            packet.write2(bgSoundId as Int)
            packet.write1(bgSoundRange)
        }
        // TODO: 77, 79
        if (hillChange != null) {
            packet.code(81)
            packet.write2(hillChange!!)
        }
        if (mapFunction != -1) {
            packet.code(82)
            packet.write1(mapFunction)
        }
        if (!randseq) {
            packet.code(89)
        }
        if (params.isNotEmpty()) {
            packet.codeParams(side, sym, params)
        }
        packet.terminateCode()
        return packet.toByteArray()
    }
}