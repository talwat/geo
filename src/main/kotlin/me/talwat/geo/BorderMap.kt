package me.talwat.geo

import org.bukkit.Location
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Point
import java.awt.geom.Area
import java.awt.geom.Path2D
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import kotlin.math.floor

internal class Tracer(private val image: BufferedImage) {
    fun trace(start: Point, target: Color): Path2D.Double {
        val path = Path2D.Double()
        val current = start.clone() as Point

        path.moveTo(start.x.toDouble(), start.y.toDouble())
        var orientation = 0

        var first = true
        do {
            var foundNext = false

            for (i in 0..3) {
                val next = getNextPoint(current, orientation)
                if (isBlack(this.image, next, target)) {
                    current.location = next
                    path.lineTo(current.getX(), current.getY())
                    orientation = turnLeft(orientation) // Turn left after finding the target
                    foundNext = true
                    break
                }

                orientation = turnRight(orientation) // Turn right if no target found
            }

            if (!foundNext) {
                break
            }

            first = false;
        } while (first || current != start)

        return path
    }

    companion object {
        // Directions in clockwise order: up, right, down, left
        private val DIRECTIONS = arrayOf(intArrayOf(0, -1), intArrayOf(1, 0), intArrayOf(0, 1), intArrayOf(-1, 0))

        private fun turnLeft(orientation: Int): Int {
            return (orientation + 3) % 4
        }

        private fun turnRight(orientation: Int): Int {
            return (orientation + 1) % 4
        }

        private fun isWithinBounds(p: Point, image: BufferedImage): Boolean {
            return p.x >= 0 && p.x < image.width && p.y >= 0 && p.y < image.height
        }

        private fun getNextPoint(p: Point, orientation: Int): Point {
            return Point(p.x + DIRECTIONS[orientation][0], p.y + DIRECTIONS[orientation][1])
        }

        private fun isBlack(image: BufferedImage, p: Point, target: Color): Boolean {
            return isWithinBounds(p, image) && Color(image.getRGB(p.x, p.y)) == target
        }

        fun isGrayScale(color: Color): Boolean {
            return (color.red == color.green && color.green == color.blue)
        }
    }
}

class BorderMap(private val image: BufferedImage) {
    fun traceShapes(): HashMap<Color, Area> {
        val tracer = Tracer(this.image)

        val shapes = HashMap<Color, Area>()
        for (i in 0 until image.width) {
            for (j in image.height - 1 downTo 1) {
                val pixel = Color(image.getRGB(i, j))

                // Grayscale is treated as a "comment"
                if (pixel.alpha != 255 || shapes.containsKey(pixel) || (Tracer.isGrayScale(pixel))) {
                    continue
                }

                val shape = Area(tracer.trace(Point(i, j), pixel))
                val stroke = BasicStroke(0.9f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER)
                val expandedArea = Area(stroke.createStrokedShape(shape))
                shape.add(expandedArea)
                shapes[pixel] = shape
            }
        }

        return shapes
    }

    fun mapToImage(pos: Location): Point {
        val x = Math.floorDiv(pos.blockX, 16)
        val z = Math.floorDiv(pos.blockZ, 16)

        val height = image.height
        val width = image.width

        val imgX = x + width / 2
        val imgY = z + height / 2

        return Point(imgX, imgY)
    }

    fun imageToMap(pos: Point2D.Double): xyz.jpenilla.squaremap.api.Point {
        val height = image.height
        val width = image.width

        val x = (pos.x - width.toDouble() / 2) * 16.0
        val z = (pos.y - height.toDouble() / 2) * 16.0

        return xyz.jpenilla.squaremap.api.Point.of(x, z)
    }

    fun getColor(pos: Location): Color? {
        val mapToImage = mapToImage(pos.toBlockLocation())

        if (mapToImage.x < 0 || mapToImage.x >= image.width) {
            return null
        }

        if (mapToImage.y < 0 || mapToImage.y >= image.height) {
            return null
        }

        return convertToColor(
            image.getRGB(
                mapToImage.x, mapToImage.y
            )
        )
    }

    companion object {
        private fun convertToColor(rgb: Int): Color {
            val a = (rgb shr 24) and 0xFF
            val r = (rgb shr 16) and 0xFF
            val g = (rgb shr 8) and 0xFF
            val b = (rgb) and 0xFF

            return Color(r, g, b, a)
        }
    }
}
