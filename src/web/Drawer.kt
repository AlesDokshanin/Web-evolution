package web

import java.awt.*
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D

private fun pointToPixel(point: PolarPoint, scale: Double): Point2D {
    val cartesian = point.toCartesian()

    val shiftX = Math.round(WIDTH / 2).toInt()
    val shiftY = Math.round(HEIGHT / 2).toInt()
    cartesian.translate(shiftX, shiftY)

    val x = (cartesian.x * scale).toInt()
    val y = (cartesian.y * scale).toInt()
    val scaled = Point(x, y)
    return scaled
}

private fun polygonFromPoints(points: Iterable<PolarPoint>, scale: Double): Polygon {
    val cartesianPoints = points.map { pt -> pointToPixel(pt, scale) }
    val xCoordinates = cartesianPoints.map { p -> p.x.toInt() }.toIntArray()
    val yCoordinates = cartesianPoints.map { p -> p.y.toInt() }.toIntArray()
    val polygon = Polygon(xCoordinates, yCoordinates, xCoordinates.size)
    return polygon
}


internal abstract class BaseDrawer(protected val graphics: Graphics2D) {
    abstract internal fun draw(web: Web, scale: Double)
}

internal class WebDrawer(graphics: Graphics2D) {
    private val skeletonDrawer = SkeletonDrawer(graphics)
    private val trappingNetDrawer = TrappingNetDrawer(graphics)
    private val fliesDrawer = FliesDrawer(graphics)


    internal fun draw(web: Web, drawFlies: Boolean, panelBounds: Rectangle2D) {
        val scale = calculateScale(panelBounds)

        skeletonDrawer.draw(web, scale)
        trappingNetDrawer.draw(web, scale)

        if (drawFlies) {
            fliesDrawer.draw(web, scale)
        }
    }

    companion object {
        private fun calculateScale(panelBounds: Rectangle2D): Double {
            val widthScale = panelBounds.width / WIDTH
            val heightScale = panelBounds.height / HEIGHT

            val scale = Math.min(widthScale, heightScale)
            return scale
        }
    }
}

private class SkeletonDrawer(graphics: Graphics2D) : BaseDrawer(graphics) {
    private val COLOR = Color(128, 128, 128)
    private val STROKE = BasicStroke(2f)

    override fun draw(web: Web, scale: Double) {
        val skeleton = web.skeleton

        graphics.stroke = STROKE
        graphics.color = COLOR

        val polygon = polygonFromPoints(skeleton.points, scale)
        graphics.drawPolygon(polygon)

        val centerPixel = pointToPixel(PolarPoint(0.0, 0.0), scale)
        for (i in 0..polygon.npoints - 1)
            graphics.drawLine(polygon.xpoints[i], polygon.ypoints[i], centerPixel.x.toInt(), centerPixel.y.toInt())
    }
}

private class TrappingNetDrawer(graphics: Graphics2D) : BaseDrawer(graphics) {
    private val COLOR = Color(255, 0, 0)
    private val STROKE = BasicStroke(2f)

    override fun draw(web: Web, scale: Double) {
        val trappingNet = web.trappingNet

        graphics.color = COLOR
        graphics.stroke = STROKE
        trappingNet.circles
                .map { it.points }
                .forEach {
                    (0..it.lastIndex).forEach { i ->
                        val p1 = pointToPixel(it[i], scale)
                        val p2 = pointToPixel(it[(i + 1) % it.size], scale)
                        graphics.drawLine(p1.x.toInt(), p1.y.toInt(), p2.x.toInt(), p2.y.toInt())
                    }
                }
    }
}

private class FliesDrawer(graphics: Graphics2D) : BaseDrawer(graphics) {
    private val CAUGHT_FLY_COLOR = Color(94, 104, 205, 128)
    private val UNCAUGHT_FLY_COLOR = Color(37, 205, 7, 128)


    override fun draw(web: Web, scale: Double) {
        web.flies.forEach { fly ->
            graphics.color = if (fly.isCaught == true) CAUGHT_FLY_COLOR else UNCAUGHT_FLY_COLOR
            graphics.background = Color.BLACK

            val rect = buildRect(fly, scale)
            graphics.fill(rect)
            graphics.draw(rect)
        }
    }

    companion object {
        private fun buildRect(fly: Fly, scale: Double): Rectangle2D {
            val center = pointToPixel(fly.center, scale)
            val flySizePixels = scale * FLY_SIZE
            val r = Rectangle2D.Double(center.x - flySizePixels / 2, center.y - flySizePixels / 2,
                    flySizePixels, flySizePixels)
            return r
        }
    }
}

