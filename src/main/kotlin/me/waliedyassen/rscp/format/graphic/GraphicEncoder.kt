package me.waliedyassen.rscp.format.graphic

import me.waliedyassen.rscp.binary.BinaryEncoder
import java.awt.image.BufferedImage

object GraphicEncoder {

    data class Margins(val left: Int, val top: Int, val right: Int, val bottom: Int)

    fun encode(images: List<BufferedImage>, format: Format): ByteArray {
        val cellWidth = images[0].width
        val cellHeight = images[0].height
        check(images.all { it.width == cellWidth && it.height == cellHeight })
        val packet: BinaryEncoder
        when (format) {
            Format.Palette -> {
                val pixelSize = cellWidth * cellHeight * images.size + 1
                packet = BinaryEncoder(pixelSize)
                val palette = mutableListOf<Int>()
                packet.write1(0x1)
                val margins = images.map { image ->
                    val margin = calculateMargins(image)
                    val imageWidth = cellWidth - (margin.left + margin.right)
                    val imageHeight = cellHeight - (margin.top + margin.bottom)
                    for (x in 0 until imageWidth) {
                        for (y in 0 until imageHeight) {
                            val argb = image.getRGB(x + margin.left, y + margin.top)
                            val alpha = (argb shr 24) and 0xff
                            val rgb = argb and 0xffffff
                            check(alpha == 0 || alpha == 255) { "Images with alpha channel is currently not supported" }
                            if (rgb !in palette) {
                                check(palette.size < 256) { "You can only use up to 256 colours in an atlas" }
                                palette += rgb
                            }
                            if (alpha == 0 || rgb == 0xff00ff) {
                                packet.write1(0)
                            } else {
                                packet.write1(palette.indexOf(rgb) + 1)
                            }
                        }
                    }
                    margin
                }
                for (rgb in palette) {
                    packet.write3(rgb)
                }
                packet.write2(cellWidth)
                packet.write2(cellHeight)
                packet.write1(palette.size)
                margins.forEach { packet.write2(it.left) }
                margins.forEach { packet.write2(it.top) }
                margins.forEach { packet.write2(cellWidth - (it.left + it.right)) }
                margins.forEach { packet.write2(cellHeight - (it.top + it.bottom)) }
            }

            Format.TrueColour -> {
                packet = BinaryEncoder(3)
                val encodingAlpha = images.any { it.colorModel.hasAlpha() }
                packet.write1(0)
                packet.write1(if (encodingAlpha) 1 else 0)// has alpha
                packet.write2(cellWidth)
                packet.write2(cellHeight)
                for (image in images) {
                    for (y in 0 until image.height) {
                        for (x in 0 until image.width) {
                            packet.write3(image.getRGB(x, y) and 0xffffff)
                        }
                    }
                    if (encodingAlpha) {
                        val hasAlpha = image.colorModel.hasAlpha()
                        for (y in 0 until image.height) {
                            for (x in 0 until image.width) {
                                val alpha = if (hasAlpha) (image.getRGB(x, y) shr 24 and 0xff) else 255
                                packet.write1(alpha)
                            }
                        }
                    }
                }

            }
        }
        packet.write2((if (format == Format.TrueColour) 1 else 0) shl 15 or (images.size and 0x7fff))
        return packet.toByteArray()
    }

    private fun calculateMargins(image: BufferedImage): Margins {
        var lowestX = image.width
        var lowestY = image.height
        var highestX = 0
        var highestY = 0
        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                val argb = image.getRGB(x, y)
                val alpha = (argb shr 24) and 0xff
                var rgb = argb and 0xffffff
                if (rgb == 0) {
                    rgb = 1
                }
                if (alpha == 0 || rgb == 0xff00ff) {
                    continue
                }
                if (x < lowestX) lowestX = x
                if (y < lowestY) lowestY = y
                if (x > highestX) highestX = x
                if (y > highestY) highestY = y
            }
        }
        return Margins(lowestX, lowestY, image.width - highestX - 1, image.height - highestY - 1)
    }
}