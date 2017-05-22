package web

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Line2D

internal class TrappingNetCircle {
    internal val points = mutableListOf<PolarPoint>()
    internal var length = 0
        private set

    constructor(web: Web) {
        generate(web.skeleton.points)
    }

    constructor(circle: TrappingNetCircle) {
        circle.points.forEach { pt -> points.add(PolarPoint(pt)) }
        save()
    }

    internal fun calculateLength() {
        length = (0..points.lastIndex).map { i ->
            val p1 = points[i]
            val p2 = points[(i + 1) % points.size]
            p1.distanceTo(p2)
        }.sum().toInt()
    }

    internal fun save() {
        calculateLength()
    }

    internal fun generate(skeletonPoints: List<PolarPoint>) {
        for (i in 0..WebConfig.sidesCount - 1) {
            val lowerBound = 0
            val upperBound = skeletonPoints[i].distance
            val angle = skeletonPoints[i].angle
            val distance = (lowerBound + random.nextDouble() * (upperBound - lowerBound)).toInt()
            points.add(PolarPoint(angle, distance))
        }
        save()
    }

    internal fun catchesFly(fly: Fly): Boolean {
        for (i in 0..points.lastIndex) {
            val a = points[i].toCartesian()
            val b = points[(i + 1) % points.size].toCartesian()

            val line = Line2D.Float(a, b)
            if (fly.rect.intersectsLine(line)) {
                fly.isCaught = true
                return true
            }
        }
        return false
    }

    internal fun draw(g: Graphics2D) {
        g.color = Color(255, 0, 0)
        g.stroke = BasicStroke(2f)

        (0..points.lastIndex).forEach { i ->
            val p1 = points[i].toCartesian()
            p1.translate(WIDTH / 2, HEIGHT / 2)
            val p2 = points[(i + 1) % points.size].toCartesian()
            p2.translate(WIDTH / 2, HEIGHT / 2)
            g.drawLine(p1.x, p1.y, p2.x, p2.y)
        }
    }
}