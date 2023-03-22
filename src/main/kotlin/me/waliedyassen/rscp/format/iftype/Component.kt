package me.waliedyassen.rscp.format.iftype

import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.Side
import me.waliedyassen.rscp.binary.BinaryEncoder
import me.waliedyassen.rscp.format.config.Config
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.parser.Reference
import me.waliedyassen.rscp.parser.Token
import me.waliedyassen.rscp.symbol.SymbolTable
import me.waliedyassen.rscp.symbol.SymbolType
import kotlin.reflect.KMutableProperty0

data class Hook(var script: Any, val arguments: Array<Any>, val transmitList: Array<Any>) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Hook

        if (script != other.script) return false
        if (!arguments.contentEquals(other.arguments)) return false
        if (!transmitList.contentEquals(other.transmitList)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = script.hashCode()
        result = 31 * result + arguments.contentHashCode()
        result = 31 * result + transmitList.contentHashCode()
        return result
    }
}

class Component(override val debugName: String) : Config(SymbolType.Component) {

    private var type = -1
    private var contenttype = 0
    private var x = 0
    private var y = 0
    private var width = 0
    private var height = 0
    private var hPosMode = 0
    private var vPosMode = 0
    private var hSizeMode = 0
    private var vSizeMode = 0
    private var hide = false
    private var layer: Any? = null
    private var scrollWidth = 0
    private var scrollHeight = 0
    private var noClickThrough = false
    private var colour = 0
    private var fill = false
    private var trans = 0
    private var text = ""
    private var textFont: Any? = null
    private var textHAlign = 0
    private var textValign = 0
    private var textParaHeight = 0
    private var textShadow = false
    private var graphic: Any? = null
    private var graphicAngle = 0
    private var graphicTiling = false
    private var graphicOutline = 0
    private var graphicShadow = 0
    private var graphicFlipV = false
    private var graphicFlipH = false
    private var model: Any? = null
    private var modelOriginX = 0
    private var modelOriginY = 0
    private var modelAngleX = 0
    private var modelAngleY = 0
    private var modelAngleZ = 0
    private var modelZoom = 0
    private var modelSeq: Any? = null
    private var modelOrtho = false
    private var modelViewportWidth = 0
    private var modelViewportHeight = 0
    private var lineWidth = 1
    private var lineLeading = false
    private var pauseButton = false
    private var tgtObj = false
    private var tgtNpc = false
    private var tgtLoc = false
    private var tgtPlayer = false
    private var tgtInv = false
    private var tgtCom = false
    private var dragDepth = 0
    private var dragSource = false
    private var dragTarget = false
    private var draggable = false
    private var dragDrop = false
    private var optionBase: String = ""
    private var options = arrayOfNulls<String>(10)
    private var targetVerb: String = ""
    private var dragDeadZone = 0
    private var dragDeadTime = 0
    private var dragRenderBehaviour = 0
    private var onLoadHook: Hook? = null
    private var onMouseOverHook: Hook? = null
    private var onMouseLeaveHook: Hook? = null
    private var onTargetLeaveHook: Hook? = null
    private var onTargetEnterHook: Hook? = null
    private var onVarTransmitHook: Hook? = null
    private var onInvTransmitHook: Hook? = null
    private var onStatTransmitHook: Hook? = null
    private var onTimerHook: Hook? = null
    private var onOpHook: Hook? = null
    private var onMouseRepeatHook: Hook? = null
    private var onClickHook: Hook? = null
    private var onClickRepeatHook: Hook? = null
    private var onReleaseHook: Hook? = null
    private var onHoldHook: Hook? = null
    private var onDragHook: Hook? = null
    private var onDragCompleteHook: Hook? = null
    private var onScrollWheelHook: Hook? = null

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {

            // Basic
            "type" -> type = parser.parseInteger() ?: return parser.skipProperty()
            "contenttype" -> contenttype = parser.parseInteger() ?: return parser.skipProperty()
            "x" -> x = parser.parseInteger() ?: return parser.skipProperty()
            "y" -> y = parser.parseInteger() ?: return parser.skipProperty()
            "width" -> width = parser.parseInteger() ?: return parser.skipProperty()
            "height" -> height = parser.parseInteger() ?: return parser.skipProperty()
            "hposmode" -> hPosMode = parser.parseInteger() ?: return parser.skipProperty()
            "vposmode" -> vPosMode = parser.parseInteger() ?: return parser.skipProperty()
            "hsizemode" -> hSizeMode = parser.parseInteger() ?: return parser.skipProperty()
            "vsizemode" -> vSizeMode = parser.parseInteger() ?: return parser.skipProperty()
            "hide" -> hide = parser.parseBoolean()
            "layer" -> layer = parser.parseReference(SymbolType.Component) ?: return parser.skipProperty()

            // Layer
            "scrollwidth" -> scrollWidth = parser.parseInteger() ?: return parser.skipProperty()
            "scrollheight" -> scrollHeight = parser.parseInteger() ?: return parser.skipProperty()
            "noclickthrough" -> noClickThrough = parser.parseBoolean()

            // Rectangle
            "colour" -> colour = parser.parseInteger() ?: return parser.skipProperty()
            "fill" -> fill = parser.parseBoolean()
            "trans" -> trans = parser.parseInteger() ?: return parser.skipProperty()
            "text" -> text = parser.parseString()

            // Text
            "font" -> textFont = parser.parseDynamic(SymbolType.FontMetrics)
            "halign" -> textHAlign = parser.parseInteger() ?: return parser.skipProperty()
            "valign" -> textValign = parser.parseInteger() ?: return parser.skipProperty()
            "paraheight" -> textParaHeight = parser.parseInteger() ?: return parser.skipProperty()
            "shadowed" -> textShadow = parser.parseBoolean()

            // Graphic
            "graphic" -> graphic = parser.parseDynamic(SymbolType.Graphic) ?: return parser.skipProperty()
            "angle" -> graphicAngle = parser.parseInteger() ?: return parser.skipProperty()
            "tiling" -> graphicTiling = parser.parseBoolean()
            "outline" -> graphicOutline = parser.parseInteger() ?: return parser.skipProperty()
            "shadow" -> graphicShadow = parser.parseInteger() ?: return parser.skipProperty()
            "flipv" -> graphicFlipV = parser.parseBoolean()
            "fliph" -> graphicFlipH = parser.parseBoolean()

            // Model
            "model" -> model = parser.parseDynamic(SymbolType.Model) ?: return parser.skipProperty()
            "originx" -> modelOriginX = parser.parseInteger() ?: return parser.skipProperty()
            "originy" -> modelOriginY = parser.parseInteger() ?: return parser.skipProperty()
            "xan" -> modelAngleX = parser.parseInteger() ?: return parser.skipProperty()
            "yan" -> modelAngleY = parser.parseInteger() ?: return parser.skipProperty()
            "zan" -> modelAngleZ = parser.parseInteger() ?: return parser.skipProperty()
            "zoom" -> modelZoom = parser.parseInteger() ?: return parser.skipProperty()
            "anim" -> modelSeq = parser.parseDynamic(SymbolType.Seq) ?: return parser.skipProperty()
            "orthog" -> modelOrtho = parser.parseBoolean()
            "viewportwid" -> modelViewportWidth = parser.parseInteger() ?: return parser.skipProperty()
            "viewporthei" -> modelViewportHeight = parser.parseInteger() ?: return parser.skipProperty()

            // Line
            "linewid" -> lineWidth = parser.parseInteger() ?: return parser.skipProperty()
            "lineleading" -> lineLeading = parser.parseBoolean()

            // Options
            "pausebutton" -> pauseButton = parser.parseBoolean()
            "tgtobj" -> tgtObj = parser.parseBoolean()
            "tgtnpc" -> tgtNpc = parser.parseBoolean()
            "tgtloc" -> tgtLoc = parser.parseBoolean()
            "tgtplayer" -> tgtPlayer = parser.parseBoolean()
            "tgtinv" -> tgtInv = parser.parseBoolean()
            "tgtcom" -> tgtCom = parser.parseBoolean()
            "dragdepth" -> {
                dragDepth = parser.parseInteger() ?: return parser.skipProperty()
                if (dragDepth < 0 || dragDepth > 7) {
                    parser.reportPropertyError("Drag depth must be in range [0-7]")
                }
            }

            "dragsource" -> dragSource = parser.parseBoolean()
            "dragtarget" -> dragTarget = parser.parseBoolean()
            "draggable" -> draggable = parser.parseBoolean()
            "dragdrop" -> dragDrop = parser.parseBoolean()
            "opbase" -> optionBase = parser.parseString()
            "op1" -> options[0] = parser.parseString()
            "op2" -> options[1] = parser.parseString()
            "op3" -> options[2] = parser.parseString()
            "op4" -> options[3] = parser.parseString()
            "op5" -> options[4] = parser.parseString()
            "op6" -> options[5] = parser.parseString()
            "op7" -> options[6] = parser.parseString()
            "op8" -> options[7] = parser.parseString()
            "op9" -> options[8] = parser.parseString()
            "op10" -> options[9] = parser.parseString()
            "targetverb" -> targetVerb = parser.parseString()

            // Drag
            "dragdeadzone" -> dragDeadZone = parser.parseInteger() ?: return parser.skipProperty()
            "dragdeadtime" -> dragDeadTime = parser.parseInteger() ?: return parser.skipProperty()
            "dragrenderbehaviour" -> dragRenderBehaviour = parser.parseInteger() ?: return parser.skipProperty()

            // Hooks
            "onload" -> parseHook(parser, ::onLoadHook)
            "onmouseover" -> parseHook(parser, ::onMouseOverHook)
            "onmouseleave" -> parseHook(parser, ::onMouseLeaveHook)
            "ontargetleave" -> parseHook(parser, ::onTargetLeaveHook)
            "ontargetenter" -> parseHook(parser, ::onTargetEnterHook)
            "onvartransmit" -> parseHook(parser, ::onVarTransmitHook, SymbolType.VarPlayer)
            "oninvtransmit" -> parseHook(parser, ::onInvTransmitHook, SymbolType.Inv)
            "onstattransmit" -> parseHook(parser, ::onStatTransmitHook, SymbolType.Stat)
            "ontimer" -> parseHook(parser, ::onTimerHook)
            "onop" -> parseHook(parser, ::onOpHook)
            "onmouserepeat" -> parseHook(parser, ::onMouseRepeatHook)
            "onclick" -> parseHook(parser, ::onClickHook)
            "onclickrepeat" -> parseHook(parser, ::onClickRepeatHook)
            "onrelease" -> parseHook(parser, ::onReleaseHook)
            "onhold" -> parseHook(parser, ::onHoldHook)
            "ondrag" -> parseHook(parser, ::onDragHook)
            "ondragcomplete" -> parseHook(parser, ::onDragCompleteHook)
            "onscrollwheel" -> parseHook(parser, ::onScrollWheelHook)
            else -> parser.unknownProperty()
        }
    }

    private fun parseHook(parser: Parser, hook: KMutableProperty0<Hook?>, transmitType: SymbolType<*>? = null) {
        val reference = parser.parseReference(SymbolType.ClientScript) ?: return parser.skipProperty()
        val symbol = parser.compiler.sym.lookupSymbol(SymbolType.ClientScript, reference.name)
        if (symbol == null) {
            parser.reportError(reference.span, "Could not resolve '${reference.name}' to a valid clientscript")
            return parser.skipProperty()
        }
        // Parse to argument list which looks like: (expression (, expression)+)
        parser.parseLParen() ?: return parser.skipProperty()
        val arguments = mutableListOf<Any>()
        for (argument in symbol.arguments) {
            if (arguments.isNotEmpty()) {
                parser.parseComma() ?: return parser.skipProperty()
            }
            val identifier = parser.peekIdentifier()
            if (identifier != null) {
                val (value, type) = when ((identifier as Token.Identifier).text) {
                    "event_opbase" -> "event_opbase" to SymbolType.String
                    "event_mousex" -> 0x80000001.toInt() to SymbolType.Int
                    "event_mousey" -> 0x80000002.toInt() to SymbolType.Int
                    "event_com" -> 0x80000003.toInt() to SymbolType.Component
                    "event_opindex" -> 0x80000004.toInt() to SymbolType.Int
                    "event_comsubid" -> 0x80000005.toInt() to SymbolType.Int
                    "event_dragtarget" -> 0x80000006.toInt() to SymbolType.Component
                    "event_dragtargetid" -> 0x80000007.toInt() to SymbolType.Int
                    "event_key" -> 0x80000008.toInt() to SymbolType.Int
                    "event_keychar" -> 0x80000009.toInt() to SymbolType.Char
                    else -> null to null
                }
                if (value != null && type != null) {
                    parser.parseIdentifier()
                    if (type != argument) {
                        parser.reportError(
                            identifier.span,
                            "Runtime constant evaluates to type '${type.literal}' but expected '${argument.literal}'"
                        )
                        parser.skipProperty()
                        return
                    }
                    arguments += value
                    continue
                }
            }
            arguments += parser.parseDynamic(argument) ?: return parser.skipProperty()
        }
        parser.parseRParen() ?: return parser.skipProperty()
        // Parse to transmit list which looks like: (expression (, expression)+)
        val transmits = mutableListOf<Any>()
        if (transmitType != null && parser.isLBrace()) {
            parser.parseLBrace() ?: return parser.skipProperty()
            while (true) {
                transmits += parser.parseDynamic(transmitType) ?: return parser.skipProperty()
                if (!parser.isComma()) {
                    break
                }
                parser.parseComma() ?: return parser.skipProperty()
            }
            parser.parseRBrace() ?: return parser.skipProperty()
        }
        hook.set(Hook(reference, arguments.toTypedArray(), transmits.toTypedArray()))
    }

    override fun verifyProperties(parser: Parser) {

    }

    override fun resolveReferences(compiler: Compiler) {
        compiler.resolveReference(::layer)
        compiler.resolveReference(::model)
        compiler.resolveReference(::modelSeq)
        compiler.resolveReference(::graphic)
        compiler.resolveReference(::textFont)
        resolveHookReferences(compiler, onLoadHook)
        resolveHookReferences(compiler, onMouseOverHook)
        resolveHookReferences(compiler, onMouseLeaveHook)
        resolveHookReferences(compiler, onTargetLeaveHook)
        resolveHookReferences(compiler, onTargetEnterHook)
        resolveHookReferences(compiler, onVarTransmitHook)
        resolveHookReferences(compiler, onInvTransmitHook)
        resolveHookReferences(compiler, onStatTransmitHook)
        resolveHookReferences(compiler, onTimerHook)
        resolveHookReferences(compiler, onOpHook)
        resolveHookReferences(compiler, onMouseRepeatHook)
        resolveHookReferences(compiler, onClickHook)
        resolveHookReferences(compiler, onClickRepeatHook)
        resolveHookReferences(compiler, onReleaseHook)
        resolveHookReferences(compiler, onHoldHook)
        resolveHookReferences(compiler, onDragHook)
        resolveHookReferences(compiler, onDragCompleteHook)
        resolveHookReferences(compiler, onScrollWheelHook)
    }

    private fun resolveHookReferences(compiler: Compiler, hook: Hook?) {
        if (hook == null) {
            return
        }
        compiler.resolveReference(hook::script)
        hook.arguments.forEachIndexed { index, argument ->
            if (argument is Reference) {
                hook.arguments[index] = compiler.resolveReference(argument)
            }
        }
        hook.transmitList.forEachIndexed { index, argument ->
            if (argument is Reference) {
                hook.transmitList[index] = compiler.resolveReference(argument)
            }
        }
    }

    override fun encode(side: Side, sym: SymbolTable): ByteArray {
        val encoder = BinaryEncoder(6)
        var version = 0
        if (version < 1) {
            if (textFont != null && (textFont as Int) >= 65535) {
                version = 1
            } else if (model != null && (model as Int) >= 65535) {
                version = 1
            } else if (modelSeq != null && (modelSeq as Int) >= 65535) {
                version = 1
            }
        }
        encoder.write1(-1)
        if (version != 0) {
            encoder.write1(type or 0x80)
            encoder.write1(version)
        } else {
            encoder.write1(type)
        }
        encoder.write2(contenttype)
        encoder.write2(x)
        encoder.write2(y)
        encoder.write2(width)
        encoder.write2(height)
        encoder.write1(hSizeMode)
        encoder.write1(vSizeMode)
        encoder.write1(hPosMode)
        encoder.write1(vPosMode)
        encoder.write2(if (layer == null) -1 else (layer as Int) and 0xffff)
        encoder.writeBoolean(hide)
        when (type) {
            0 -> {
                encoder.write2(scrollWidth)
                encoder.write2(scrollHeight)
                encoder.writeBoolean(noClickThrough)
            }

            5 -> {
                encoder.write4(if (graphic == null) -1 else graphic as Int)
                encoder.write2(graphicAngle)
                encoder.writeBoolean(graphicTiling)
                encoder.write1(trans)
                encoder.write1(graphicOutline)
                encoder.write4(graphicShadow)
                encoder.writeBoolean(graphicFlipV)
                encoder.writeBoolean(graphicFlipH)
            }

            6 -> {
                val modelId = if (model == null) -1 else model as Int
                if (version >= 1) {
                    encoder.write2or4(modelId)
                } else {
                    encoder.write2(modelId)
                }
                encoder.write2(modelOriginX)
                encoder.write2(modelOriginY)
                encoder.write2(modelAngleX)
                encoder.write2(modelAngleY)
                encoder.write2(modelAngleZ)
                encoder.write2(modelZoom)
                val modelSeqId = if (modelSeq == null) -1 else modelSeq as Int
                if (version >= 1) {
                    encoder.write2or4(modelSeqId)
                } else {
                    encoder.write2(modelSeqId)
                }
                encoder.writeBoolean(modelOrtho)
                encoder.write2(0)
                if (hSizeMode != 0) {
                    encoder.write2(modelViewportWidth)
                }
                if (vSizeMode != 0) {
                    encoder.write2(modelViewportHeight)
                }
            }

            4 -> {
                val textFontId = if (textFont == null) -1 else textFont as Int
                if (version >= 1) {
                    encoder.write2or4(textFontId)
                } else {
                    encoder.write2(textFontId)
                }
                encoder.writeString(text)
                encoder.write1(textParaHeight)
                encoder.write1(textHAlign)
                encoder.write1(textValign)
                encoder.writeBoolean(textShadow)
                encoder.write4(colour)
            }

            3 -> {
                encoder.write4(colour)
                encoder.writeBoolean(fill)
                encoder.write1(trans)
            }

            9 -> {
                encoder.write1(lineWidth)
                encoder.write4(colour)
                encoder.writeBoolean(lineLeading)
            }
        }
        var events = 0
        if (pauseButton) events = events or (1 shl 0)
        options.forEachIndexed { index, option ->
            if (option == null) return@forEachIndexed
            events = events or (2 shl index)
        }
        if (tgtObj) events = events or (1 shl 11)
        if (tgtNpc) events = events or (1 shl 12)
        if (tgtLoc) events = events or (1 shl 13)
        if (tgtPlayer) events = events or (1 shl 14)
        if (tgtInv) events = events or (1 shl 15)
        if (tgtCom) events = events or (1 shl 16)
        if (dragDepth != 0) events = events or (dragDepth shl 17)
        if (dragSource) events = events or (1 shl 20)
        if (dragTarget) events = events or (1 shl 21)
        if (draggable) events = events or (1 shl 29)
        if (dragDrop) events = events or (1 shl 31)
        encoder.write3(events)
        encoder.writeString(optionBase)
        val opCount = options.indexOfLast { it != null } + 1
        encoder.write1(opCount)
        repeat(opCount) { encoder.writeString(if (options[it] != null) options[it]!! else "") }
        encoder.write1(dragDeadZone)
        encoder.write1(dragDeadTime)
        encoder.write1(dragRenderBehaviour)
        encoder.writeString(targetVerb)
        encodeHook(encoder, onLoadHook)
        encodeHook(encoder, onMouseOverHook)
        encodeHook(encoder, onMouseLeaveHook)
        encodeHook(encoder, onTargetLeaveHook)
        encodeHook(encoder, onTargetEnterHook)
        encodeHook(encoder, onVarTransmitHook)
        encodeHook(encoder, onInvTransmitHook)
        encodeHook(encoder, onStatTransmitHook)
        encodeHook(encoder, onTimerHook)
        encodeHook(encoder, onOpHook)
        encodeHook(encoder, onMouseRepeatHook)
        encodeHook(encoder, onClickHook)
        encodeHook(encoder, onClickRepeatHook)
        encodeHook(encoder, onReleaseHook)
        encodeHook(encoder, onHoldHook)
        encodeHook(encoder, onDragHook)
        encodeHook(encoder, onDragCompleteHook)
        encodeHook(encoder, onScrollWheelHook)
        encodeTransmitList(encoder, onVarTransmitHook)
        encodeTransmitList(encoder, onInvTransmitHook)
        encodeTransmitList(encoder, onStatTransmitHook)
        return encoder.toByteArray()
    }

    private fun encodeHook(encoder: BinaryEncoder, hook: Hook?) {
        if (hook == null) {
            encoder.write1(0)
            return
        }
        encoder.write1(1 + hook.arguments.size)
        // Write the id, which is always first argument.
        encoder.write1(0)
        encoder.write4(hook.script as Int)
        repeat(hook.arguments.size) {
            when (val value = hook.arguments[it]) {
                is Int -> {
                    encoder.write1(0)
                    encoder.write4(value)
                }

                is Boolean -> {
                    encoder.write1(0)
                    encoder.write4(if (value) 1 else 0)
                }

                is String -> {
                    encoder.write1(1)
                    encoder.writeString(value)
                }

                else -> error("Unrecognized hook argument: $value (${value::class})")
            }
        }
    }

    private fun encodeTransmitList(encoder: BinaryEncoder, hook: Hook?) {
        if (hook == null) {
            encoder.write1(0)
            return
        }
        val transmits = hook.transmitList
        encoder.write1(transmits.size)
        repeat(transmits.size) { encoder.write4(transmits[it] as Int) }
    }
}