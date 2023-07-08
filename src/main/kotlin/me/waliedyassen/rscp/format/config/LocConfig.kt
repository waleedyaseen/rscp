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
import me.waliedyassen.rscp.util.LiteralEnum

data class LocModel(var shape: Any?, var model: Any)

enum class ForceApproachDir(override val literal: String) : LiteralEnum {
    North("north"),
    East("east"),
    South("south"),
    West("west"),
}

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
    var forceApproach: ForceApproachDir? = null
    var xof: Int? = null
    var yof: Int? = null
    var zof: Int? = null
    var forceDecor = false
    var raiseObject: Boolean? = null
    var breakRouteFinding = false
    var multiVarRef: Reference? = null
    var multiVar: Int = -1
    var multiVarbit: Int = -1
    var multiLoc: MutableMap<Int, Any>? = null
    var multiDefault: Any? = null
    var bgSoundId: Any? = null
    var bgSoundRange = 0
    var randomSoundDelayMin = 0
    var randomSoundDelayMax = 0
    var randomSounds: MutableList<Any>? = null
    var randseq = false
    var params = LinkedHashMap<Int, Any>()

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            in MODELX_NAMES -> {
                val model = parser.parseDynamic(SymbolType.Model) ?: return parser.skipProperty()
                val shape = if (parser.isComma()) {
                    parser.parseComma() ?: return parser.skipProperty()
                    parser.parseDynamic(SymbolType.LocShape) ?: return parser.skipProperty()
                } else {
                    null
                }
                models.add(LocModel(shape, model))
            }

            in LOWMODELX_NAMES -> {
                val model = parser.parseDynamic(SymbolType.Model) ?: return parser.skipProperty()
                val shape = if (parser.isComma()) {
                    parser.parseComma() ?: return parser.skipProperty()
                    parser.parseDynamic(SymbolType.LocShape) ?: return parser.skipProperty()
                } else {
                    null
                }
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
            "shadow" -> softShadows = parser.parseBoolean()
            "resizex" -> resizeX = parser.parseInteger() ?: return parser.skipProperty()
            "resizey" -> resizeY = parser.parseInteger() ?: return parser.skipProperty()
            "resizez" -> resizeZ = parser.parseInteger() ?: return parser.skipProperty()
            "mapscene" -> mapScene = parser.parseInteger() ?: return parser.skipProperty()
            "forceapproach" -> forceApproach = parser.parseEnumLiteral() ?: return parser.skipProperty()
            "xof" -> xof = parser.parseInteger() ?: return parser.skipProperty()
            "yof" -> yof = parser.parseInteger() ?: return parser.skipProperty()
            "zof" -> zof = parser.parseInteger() ?: return parser.skipProperty()
            "multivar" -> multiVarRef = parser.parseReference(SymbolType.VarOrVarbit) ?: return parser.skipProperty()
            "multiloc" -> parsePropertyMultiConfig(parser, ::multiDefault, ::multiLoc, SymbolType.Loc)
            "forcedecor" -> forceDecor = parser.parseBoolean()
            "breakroutefinding" -> breakRouteFinding = parser.parseBoolean()
            "raiseobject" -> raiseObject = parser.parseBoolean()
            "mapfunction" -> mapFunction = parser.parseInteger() ?: return parser.skipProperty()
            "hillchange" -> hillChange = parser.parseInteger() ?: return parser.skipProperty()
            "randseq" -> randseq = parser.parseBoolean()
            "bgsound" -> {
                bgSoundId = parser.parseDynamic(SymbolType.Synth) ?: return parser.skipProperty()
                parser.parseComma() ?: return parser.skipProperty()
                bgSoundRange = parser.parseInteger() ?: return parser.skipProperty()
            }

            "randomsound" -> {
                randomSoundDelayMin = parser.parseInteger() ?: return parser.skipProperty()
                parser.parseComma() ?: return parser.skipProperty()
                randomSoundDelayMax = parser.parseInteger() ?: return parser.skipProperty()
                parser.parseComma() ?: return parser.skipProperty()
                bgSoundRange = parser.parseInteger() ?: return parser.skipProperty()
                val synths = mutableListOf<Any>()
                randomSounds = synths
                do {
                    parser.parseComma() ?: return parser.skipProperty()
                    synths.add(parser.parseDynamic(SymbolType.Synth) ?: return parser.skipProperty())
                } while (parser.isComma())
            }

            "param" -> parser.parseParam(params)
            "transmit" -> parser.parseBoolean()
            else -> parser.unknownProperty()
        }
    }

    override fun verifyProperties(parser: Parser) {
        if (!models.all { it.shape != null } && !models.all { it.shape == null }) {
            parser.reportUnitError("'model' property must define shapes for all models or none.")
        }
        if (!lowModels.all { it.shape != null } && !lowModels.all { it.shape == null }) {
            parser.reportUnitError("'lowmodel' property must define shapes for all models or none.")
        }
        if ((bgSoundId != null || randomSounds != null) && bgSoundRange !in 0..255) {
            parser.reportUnitError("bgsound/randomsound range must be between [0-255]")
        }
        if (randomSounds != null) {
            if (randomSoundDelayMin !in 0..65535) {
                parser.reportUnitError("randomsound delay min must be between [0-65535]")
            }
            if (randomSoundDelayMax !in 0..65535) {
                parser.reportUnitError("randomsound delay max must be between [0-65535]")
            }
            if (randomSoundDelayMin > randomSoundDelayMax) {
                parser.reportUnitError("randomsound delay max must greater or equals to delay min")
            }
        }
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
                retexSrc[index] = compiler.resolveReferenceId(ref, false)
            }
        }
        retexDst.forEachIndexed { index, ref ->
            if (ref is Reference) {
                retexDst[index] = compiler.resolveReferenceId(ref, true)
            }
        }
        if (category != null) {
            compiler.resolveReference(::category)
        }
        if (bgSoundId != null) {
            compiler.resolveReference(::bgSoundId)
        }
        val randomSounds = randomSounds
        randomSounds?.forEachIndexed { index, ref ->
            if (ref is Reference) {
                randomSounds[index] = compiler.resolveReferenceId(ref)
            }
        }
        resolveReferencesMultiConfig(compiler, ::multiLoc, ::multiDefault, ::multiVarRef, ::multiVar, ::multiVarbit)
    }

    override fun encode(side: Side, sym: SymbolTable): ByteArray {
        val packet = BinaryEncoder(6)
        if (side != Side.Server && !transmit) {
            packet.terminateCode()
            return packet.toByteArray()
        }
        var version = 0
        if (models.any { it.model as Int >= 65535 } || lowModels.any { it.model as Int >= 65535 }) {
            version = 1
        }
        if (version < 1 && anim != null && anim as Int >= 65535) {
            version = 1
        }
        if (version > 0) {
            packet.write1(-1)
            packet.write1(version)
        }
        packet.codeModels(models, version)
        packet.codeModels(lowModels, version)
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
            if (version > 0) {
                packet.write2or4(anim as Int)
            } else {
                packet.write2(anim as Int)
            }
        }
        if (blockWalk == true) {
            packet.code(27)
        }
        if (wallOffset != null) {
            packet.code(28)
            packet.write1(wallOffset!!)
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
            packet.code(41)
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
        forceApproach?.let {
            packet.code(69)
            packet.write1((1 shl it.ordinal).inv() and 0xf)
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
        if (raiseObject != null) {
            packet.code(75)
            packet.write1(if (raiseObject == true) 1 else 0)
        }
        if (bgSoundId != null) {
            packet.code(78)
            packet.write2(bgSoundId as Int)
            packet.write1(bgSoundRange)
        }
        val randomSounds = randomSounds
        if (randomSounds != null) {
            packet.code(79)
            packet.write2(randomSoundDelayMin)
            packet.write2(randomSoundDelayMax)
            packet.write1(bgSoundRange)
            packet.write1(randomSounds.size)
            randomSounds.forEach { packet.write2(it as Int) }
        }
        val multiLoc = multiLoc
        if (multiLoc != null) {
            packet.code(if (multiDefault != null) 92 else 77)
            packet.write2(multiVarbit)
            packet.write2(multiVar)
            if (multiDefault != null) {
                if (version > 0) {
                    packet.write2or4(multiDefault as Int)
                } else {
                    packet.write2(multiDefault as Int)
                }
            }
            val maxState = multiLoc.maxOf { (state, _) -> state }
            packet.write1(maxState)
            for (state in 0..maxState) {
                val loc = multiLoc[state] as Int? ?: -1
                if (version > 0) {
                    packet.write2or4(loc)
                } else {
                    packet.write2(loc)
                }
            }
        }
        if (hillChange != null) {
            packet.code(81)
            packet.write1(hillChange!!)
        }
        if (mapFunction != -1) {
            packet.code(82)
            packet.write2(mapFunction)
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

    companion object {
        private val MODELX_NAMES = (1..12).map { "model$it" }.toHashSet()
        private val LOWMODELX_NAMES = (1..12).map { "lowmodel$it" }.toHashSet()
    }

    private fun BinaryEncoder.codeModels(models: List<LocModel>, version: Int) {
        if (models.isEmpty()) {
            return // TODO(Walied): Is this more valid than encoding with 0 size
        }
        val haveShapes = models[0].shape == null
        if (haveShapes) {
            code(1)
            write1(models.size)
            models.forEach {
                if (version > 0) {
                    write2or4(it.model as Int)
                } else {
                    write2(it.model as Int)
                }
                write1(it.shape as Int)
            }
        } else {
            code(5)
            write1(models.size)
            models.forEach {
                if (version > 0) {
                    write2or4(it.model as Int)
                } else {
                    write2(it.model as Int)
                }
            }
        }
    }
}
