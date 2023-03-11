package me.waliedyassen.rscp.format.graphic

import me.waliedyassen.rscp.CodeGenerator
import me.waliedyassen.rscp.Compiler
import me.waliedyassen.rscp.Side
import me.waliedyassen.rscp.format.config.Config
import me.waliedyassen.rscp.parser.Parser
import me.waliedyassen.rscp.symbol.BasicSymbol
import me.waliedyassen.rscp.symbol.SymbolTable
import me.waliedyassen.rscp.symbol.SymbolType
import me.waliedyassen.rscp.util.LiteralEnum
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

@Suppress("unused")
enum class AtlasMode(override val literal: String) : LiteralEnum {
    None("none"),
    File("file"),
    Group("group")
}
@Suppress("unused")
enum class Format(override val literal: String) : LiteralEnum {
    Palette("palette"),
    TrueColour("truecolour")
}

class GraphicConfig(override val name: String) : Config(name, SymbolType.Graphic) {

    private var atlasMode = AtlasMode.None
    private var format = Format.Palette
    var atlasCellWidth = 0
    var atlasCellHeight = 0
    var path: String? = null
    private var atlases: Map<String, List<BufferedImage>>? = null
    private var imageFile: File? = null

    override val symbolType = SymbolType.Graphic

    override fun parseProperty(name: String, parser: Parser) {
        when (name) {
            "format" -> format = parser.parseEnumLiteral() ?: return parser.skipProperty()
            "atlasmode" -> atlasMode = parser.parseEnumLiteral() ?: return parser.skipProperty()
            "atlascell" -> {
                val width = parser.parseInteger() ?: return parser.skipProperty()
                parser.parseComma() ?: return parser.skipProperty()
                val height = parser.parseInteger() ?: return parser.skipProperty()
                atlasCellWidth = width
                atlasCellHeight = height
            }

            "path" -> path = parser.parseString() ?: return parser.skipProperty()
            else -> return parser.unknownProperty()
        }
    }

    override fun verifyProperties(parser: Parser) {
        if (atlasMode != AtlasMode.None) {
            if (atlasCellWidth < 1 || atlasCellHeight < 1) {
                parser.reportUnitError("Atlas cell size must be a positive number")
            }
        }
        if (path == null) {
            parser.reportUnitError("path property must be specified")
        } else {
            imageFile = parser.compiler.graphicsDirectory.resolve(path!!)
            if (!imageFile!!.exists()) {
                parser.reportUnitError("Could not find $path in the file system")
            }
        }
    }

    override fun resolveReferences(compiler: Compiler) {
        // Do nothing.
    }

    override fun contributeSymbols(sym: SymbolTable) {
        val imageFile = imageFile ?: return
        if (!imageFile.exists()) {
            return
        }
        val spritesList = generateSprites(imageFile)
        check(atlasMode != AtlasMode.None || spritesList.size == 1)
        var index = 0
        val spritesMap = spritesList.associateWith {
            if (atlasMode == AtlasMode.Group) {
                "$name,${index++}"
            } else {
                name
            }
        }
        atlases = spritesMap.asSequence()
            .groupBy { it.value }
            .mapValues { it.value.map { entry -> entry.key } }
        atlases!!.keys.forEach { name ->
            val old = sym.lookupSymbol(SymbolType.Graphic, name)
            val id = old?.id ?: sym.generateId(SymbolType.Graphic)
            val new = BasicSymbol(name, id)
            if (old != new) {
                val list = sym.lookupList(SymbolType.Graphic)
                if (old != null) {
                    list.remove(old)
                }
                list.add(new)
            }
        }
    }

    private fun generateSprites(file: File): List<BufferedImage> {
        val atlasImage = ImageIO.read(file)
        if (atlasMode == AtlasMode.None) {
            return listOf(atlasImage)
        }
        val columns = atlasImage.width / atlasCellWidth
        val rows = atlasImage.height / atlasCellHeight
        val sprites = mutableListOf<BufferedImage>()
        for (column in 0 until columns) {
            for (row in 0 until rows) {
                val image = atlasImage.getSubimage(
                    column * atlasCellWidth,
                    row * atlasCellHeight,
                    atlasCellWidth,
                    atlasCellHeight
                )
                sprites += image
            }
        }
        return sprites
    }

    override fun generateCode(allUnits: List<CodeGenerator>, outputFolder: File, sym: SymbolTable, side: Side) {
        val atlases = this.atlases ?: error("No atlases were generated")
        val graphicsDirectory = outputFolder.resolve("graphic")
        check(graphicsDirectory.exists() || graphicsDirectory.mkdirs())
        atlases.forEach { (name, images) ->
            val id = sym.lookupSymbol(SymbolType.Graphic, name)?.id ?: error("No symbol was generated for atlas: $name")
            val file = graphicsDirectory.resolve(id.toString())
            file.writeBytes(GraphicEncoder.encode(images, format))
        }
    }


    override fun encode(side: Side, sym: SymbolTable): ByteArray {
        return ByteArray(0)
    }
}