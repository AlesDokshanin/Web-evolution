package web

import java.awt.*
import java.awt.geom.Rectangle2D

private fun translatePolygon(polygon: Polygon) {
    polygon.translate(WIDTH / 2, HEIGHT / 2)
}

private fun translatePoint(point: Point) {
    point.translate(WIDTH / 2, HEIGHT / 2)
}

internal abstract class BaseDrawer(protected val graphics: Graphics2D)

internal class WebDrawer(graphics: Graphics2D) : BaseDrawer(graphics) {
    private val skeletonDrawer = SkeletonDrawer(graphics)
    private val trappingNetDrawer = TrappingNetDrawer(graphics)
    private val flyDrawer = FlyDrawer(graphics)

    fun draw(web: Web, drawFlies: Boolean) {
        skeletonDrawer.draw(web.skeleton)
        trappingNetDrawer.draw(web.trappingNet)

        if (drawFlies) {
            web.flies.forEach { fly ->
                flyDrawer.draw(fly)
            }
        }
    }
}

private class SkeletonDrawer(graphics: Graphics2D) : BaseDrawer(graphics) {
    private val COLOR = Color(128, 128, 128)
    private val STROKE = BasicStroke(2f)

    companion object {
        private fun generatePolygon(skeleton: Skeleton): Polygon {
            val polygon = buildPolygonFromPolarPoints(skeleton.points)
            return polygon
        }
    }

    internal fun draw(skeleton: Skeleton) {
        graphics.stroke = STROKE
        graphics.color = COLOR

        val polygon = generatePolygon(skeleton)
        translatePolygon(polygon)
        graphics.drawPolygon(polygon)

        for (i in 0..polygon.npoints - 1)
            graphics.drawLine(polygon.xpoints[i], polygon.ypoints[i], CENTER.x, CENTER.y)
    }
}

private class TrappingNetDrawer(graphics: Graphics2D) : BaseDrawer(graphics) {
    private val COLOR = Color(255, 0, 0)
    private val STROKE = BasicStroke(2f)

    internal fun draw(trappingNet: TrappingNet) {
        graphics.color = COLOR
        graphics.stroke = STROKE
        trappingNet.circles
                .map { it.points }
                .forEach {
                    (0..it.lastIndex).forEach { i ->
                        val p1 = it[i].toCartesian()
                        translatePoint(p1)
                        val p2 = it[(i + 1) % it.size].toCartesian()
                        translatePoint(p2)
                        graphics.drawLine(p1.x, p1.y, p2.x, p2.y)
                    }
                }
    }
}

private class FlyDrawer(graphics: Graphics2D) : BaseDrawer(graphics) {
    private val CAUGHT_FLY_COLOR = Color(94, 104, 205, 128)
    private val UNCAUGHT_FLY_COLOR = Color(37, 205, 7, 128)

    internal fun draw(fly: Fly) {
        graphics.color = if (fly.isCaught == true) CAUGHT_FLY_COLOR else UNCAUGHT_FLY_COLOR
        graphics.background = Color.BLACK
        val rect = buildRect(fly)
        graphics.fillRect((CENTER.x + rect.x).toInt(), (CENTER.y + rect.y).toInt(), FLY_SIZE, FLY_SIZE)
        graphics.drawRect((CENTER.x + rect.x).toInt(), (CENTER.y + rect.y).toInt(), FLY_SIZE, FLY_SIZE)
    }

    companion object {

        private fun buildRect(fly: Fly): Rectangle2D {
            val pt = fly.center.toCartesian()
            val r = Rectangle2D.Float(pt.x.toFloat() - FLY_SIZE / 2,
                    pt.y.toFloat() - FLY_SIZE / 2,
                    FLY_SIZE.toFloat(), FLY_SIZE.toFloat())
            return r
        }
    }
}

