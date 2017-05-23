package web

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D


abstract internal class BaseDrawer(protected val graphics: Graphics2D) {
    abstract internal fun draw(instance: Any)
}

internal class WebDrawer(graphics: Graphics2D){
    private val skeletonDrawer = SkeletonDrawer(graphics)
    private val trappingNetDrawer = TrappingNetDrawer(graphics)
    private val flyDrawer = FlyDrawer(graphics)

    fun draw(web: Web, drawFlies: Boolean) {
        skeletonDrawer.draw(web.skeleton)
        trappingNetDrawer.draw(web.trappingNet)

        if(drawFlies) {
            web.flies!!.forEach { fly ->
                flyDrawer.draw(fly)
            }
        }
    }
}

private class SkeletonDrawer(graphics: Graphics2D) : BaseDrawer(graphics) {
    internal val COLOR = Color(128, 128, 128)
    private val STROKE = BasicStroke(2f)

    override fun draw(instance: Any) {
        val skeleton = instance as Skeleton

        graphics.stroke = STROKE
        graphics.color = COLOR

        val polygon = skeleton.generatePolygon()
        polygon.translate(WIDTH / 2, HEIGHT / 2)
        graphics.drawPolygon(polygon)

        for (i in 0..polygon.npoints - 1)
            graphics.drawLine(polygon.xpoints[i], polygon.ypoints[i], CENTER.x, CENTER.y)
    }
}

private class TrappingNetDrawer(g: Graphics2D) : BaseDrawer(g) {
    private val COLOR = Color(255, 0, 0)
    private val STROKE = BasicStroke(2f)

    override fun draw(instance: Any) {
        val trappingNet = instance as TrappingNet

        graphics.color = COLOR
        graphics.stroke = STROKE
        for (circle in trappingNet.circles) {
            val points = circle.points

            (0..points.lastIndex).forEach { i ->
                val p1 = points[i].toCartesian()
                p1.translate(WIDTH / 2, HEIGHT / 2)
                val p2 = points[(i + 1) % points.size].toCartesian()
                p2.translate(WIDTH / 2, HEIGHT / 2)
                graphics.drawLine(p1.x, p1.y, p2.x, p2.y)
            }
        }
    }
}

private class FlyDrawer(g: Graphics2D): BaseDrawer(g) {
    private val CAUGHT_FLY_COLOR = Color(94, 104, 205, 128)
    private val UNCAUGHT_FLY_COLOR = Color(37, 205, 7, 128)

    override fun draw(instance: Any) {
        val fly = instance as Fly
        graphics.color = if (fly.isCaught == true) CAUGHT_FLY_COLOR else UNCAUGHT_FLY_COLOR
        graphics.background = Color.BLACK
        graphics.fillRect((CENTER.x + fly.rect.x).toInt(), (CENTER.y + fly.rect.y).toInt(), FLY_SIZE, FLY_SIZE)
        graphics.drawRect((CENTER.x + fly.rect.x).toInt(), (CENTER.y + fly.rect.y).toInt(), FLY_SIZE, FLY_SIZE)
    }
}