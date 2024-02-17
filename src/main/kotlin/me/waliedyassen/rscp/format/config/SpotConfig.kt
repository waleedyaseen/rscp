package me.waliedyassen.rscp.format.config

import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.Side
import me.waliedyassen.rscp.binary.BinaryEncoder
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.symbol.SymbolTable
import me.waliedyassen.rscp.symbol.SymbolType

class SpotConfig(override val debugName: String) : Config(SymbolType.Spotanim) {

    private var model: Any? = null
    private var seq: Any? = null
    private var resizeh: Int? = null
    private var resizev: Int? = null
    private var ambient: Int? = null
    private var contrast: Int? = null
    private var angle: Int? = null
    private var hillRotate: Boolean? = null
    private var hillRotateWidth: Int? = null
    private var hillRotateLength: Int? = null
    private var hillRotateMaxAngleX: Int? = null
    private var hillRotateMaxAngleZ: Int? = null
    private var recolSrc = arrayOfNulls<Int>(6)
    private var recolDst = arrayOfNulls<Int>(6)
    private var retexSrc = arrayOfNulls<Any>(6)
    private var retexDst = arrayOfNulls<Any>(6)

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "model" -> model = parser.parseDynamic(SymbolType.Model) ?: return parser.skipProperty()
            "seq" -> seq = parser.parseDynamic(SymbolType.Seq) ?: return parser.skipProperty()
            "resizeh" -> resizeh = parser.parseInteger() ?: return parser.skipProperty()
            "resizev" -> resizev = parser.parseInteger() ?: return parser.skipProperty()
            "angle" -> {
                val angle = parser.parseInteger() ?: return parser.skipProperty()
                this.angle = angle
                if (angle != 0 && angle != 90 && angle != 180 && angle != 270) {
                    parser.reportPropertyError("'angle' must be 0, 90, 180 or 270")
                }
            }

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
            "ambient" -> {
                val ambient = parser.parseInteger() ?: return parser.skipProperty()
                if (ambient !in 0..255) {
                    parser.reportPropertyError("'ambient' property must be in the range [0-255]")
                }
                this.ambient = ambient
            }
            "contrast" -> {
                val contrast = parser.parseInteger() ?: return parser.skipProperty()
                if (contrast !in 0..255) {
                    parser.reportPropertyError("'contrast' property must be in the range [0-255]")
                }
                this.contrast = contrast
            }
            "hillrotate" -> {
                if (parser.isBoolean()) {
                    hillRotate = parser.parseBoolean()
                } else {
                    val hillRotateWidth = parser.parseInteger() ?: return parser.skipProperty()
                    parser.parseComma() ?: return parser.skipProperty()
                    val hillRotateLength = parser.parseInteger() ?: return parser.skipProperty()
                    if (hillRotateWidth !in 0..255) {
                        parser.reportPropertyError("'hillrotate' property width component must be in the range [0-255]")
                    }
                    if (hillRotateLength !in 0..255) {
                        parser.reportPropertyError("'hillrotate' property length component must be in the range [0-255]")
                    }
                    this.hillRotateWidth = hillRotateWidth
                    this.hillRotateLength = hillRotateLength
                    if (parser.isComma()) {
                        parser.parseComma()
                        val hillRotateMaxAngleX = parser.parseInteger() ?: return parser.skipProperty()
                        if (hillRotateMaxAngleX !in 0..255) {
                            parser.reportPropertyError("'hillrotate' property max x-angle component must be in the range [0-255]")
                        }
                        parser.parseComma() ?: return parser.skipProperty()
                        val hillRotateMaxAngleZ = parser.parseInteger() ?: return parser.skipProperty()
                        if (hillRotateMaxAngleZ !in 0..255) {
                            parser.reportPropertyError("'hillrotate' property max z-angle component must be in the range [0-255]")
                        }
                        this.hillRotateMaxAngleX = hillRotateMaxAngleX
                        this.hillRotateMaxAngleZ = hillRotateMaxAngleZ
                    }
                }
            }

            else -> parser.unknownProperty()
        }
    }

    override fun verifyProperties(parser: Parser) {
    }

    override fun resolveReferences(compiler: Compiler) {
        compiler.resolveReference(::model)
        compiler.resolveReference(::seq)
    }

    override fun encode(side: Side, sym: SymbolTable): ByteArray {
        val encoder = BinaryEncoder(64)
        val model = model
        if (model is Int) {
            encoder.code(1)
            encoder.write2(model)
        }
        val seq = seq
        if (seq is Int) {
            encoder.code(2)
            encoder.write2(seq)
        }
        val resizeh = resizeh
        if (resizeh != null) {
            encoder.code(4)
            encoder.write2(resizeh)
        }
        val resizev = resizev
        if (resizev != null) {
            encoder.code(5)
            encoder.write2(resizev)
        }
        val angle = angle
        if (angle != null) {
            encoder.code(6)
            encoder.write2(angle)
        }
        val ambient = ambient
        if (ambient != null) {
            encoder.code(7)
            encoder.write1(ambient)
        }
        val contrast = contrast
        if (contrast != null) {
            encoder.code(8)
            encoder.write1(contrast)
        }
        val hillRotate = hillRotate
        if (hillRotate != null) {
            if (hillRotate) {
                encoder.code(9)
            }
        } else {
            val hillRotateWidth = hillRotateWidth
            val hillRotateLength = hillRotateLength
            val hillRotateMaxAngleX = hillRotateMaxAngleX
            val hillRotateMaxAngleZ = hillRotateMaxAngleZ
            if (hillRotateWidth != null && hillRotateLength != null) {
                var packedValue = 0
                packedValue = packedValue or (hillRotateWidth)
                packedValue = packedValue or (hillRotateLength shl 8)
                if (hillRotateMaxAngleX != null && hillRotateMaxAngleZ != null) {
                    packedValue = packedValue or (hillRotateMaxAngleX shl 16)
                    packedValue = packedValue or (hillRotateMaxAngleZ shl 24)
                    encoder.code(16)
                    encoder.write4(packedValue)
                } else {
                    encoder.code(15)
                    encoder.write2(packedValue)
                }
            }
        }
        val recolorCount = recolSrc.count { it != null }
        if (recolorCount > 0) {
            encoder.code(40)
            encoder.write1(recolorCount)
            for (i in recolSrc.indices) {
                val src = recolSrc[i]
                val dst = recolDst[i]
                if (src == null || dst == null) {
                    check(src == null && dst == null)
                    continue
                }
                encoder.write2(src)
                encoder.write2(dst)
            }
        }
        val retextureCount = retexSrc.count { it != null }
        if (retextureCount > 0) {
            encoder.code(41)
            encoder.write1(retextureCount)
            for (i in retexSrc.indices) {
                val src = retexSrc[i]
                val dst = retexDst[i]
                if (src == null || dst == null) {
                    check(src == null && dst == null)
                    continue
                }
                encoder.write2(src as Int)
                encoder.write2(dst as Int)
            }
        }
        encoder.terminateCode()
        return encoder.toByteArray()
    }
}