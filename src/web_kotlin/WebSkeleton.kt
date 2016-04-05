package web_kotlin

import java.awt.*
import java.util.*

internal class WebSkeleton() {
    var points: ArrayList<PolarPoint>
    var polygon: Polygon? = null

    init {
        points = ArrayList<PolarPoint>()
    }

    constructor(skeleton: WebSkeleton) : this() {
        points.clear()
        skeleton.points.forEach { points.add(PolarPoint(it)) }
    }

    fun generatePolygon() {
        polygon = getPolygonFromPolarPoints(points)
        polygon!!.translate(CENTER.x, CENTER.y)
    }

    private fun generatePoint(): PolarPoint {
        val angle = random.nextDouble() * 2 * Math.PI
        var maxDistance = 0
        var bound: Point

        while (maxDistance <= MIN_SKELETON_DISTANCE_FROM_CENTER) {
            bound = Point((0.5 * WIDTH * Math.cos(angle)).toInt(), (0.5 * HEIGHT * Math.sin(angle)).toInt())
            maxDistance = bound.distance(0.0, 0.0).toInt()
        }

        val distance = (MIN_SKELETON_DISTANCE_FROM_CENTER +
                (maxDistance - MIN_SKELETON_DISTANCE_FROM_CENTER) * random.nextDouble()).toInt()

        return PolarPoint(angle, distance)
    }

    private fun pointIsValid(p: PolarPoint, points: Iterable<PolarPoint>): Boolean {
        return points.all({ currPoint -> Math.abs(p.angle - currPoint.angle) >= WebConfig.minAngleBetweenSkeletonLines })
    }

    private fun centerFitsIntoPolygon(): Boolean {
        val shift = MIN_TRAPPING_NET_CIRCLE_DISTANCE
        for (dx in -1..1) {
            for (dy in -1..1) {
                var p = Point(CENTER.x + dx * shift, CENTER.y + dy * shift)
                if (!polygon!!.contains(p))
                    return false
            }
        }
        return true
    }

    internal fun isInvalid(): Boolean {
        return !centerFitsIntoPolygon()
    }


    private fun generatePoints() {
        points.clear()

        for (i in 1..WebConfig.sidesCount) {
            var p = generatePoint()
            while (!pointIsValid(p, points))
                p = generatePoint()
            points.add(p)
        }
    }

    internal fun draw(g: Graphics2D) {
        g.stroke = BasicStroke(2f)
        g.color = Color(128, 128, 128)
        g.drawPolygon(polygon)

        for (i in 0..polygon!!.npoints - 1)
            g.drawLine(polygon!!.xpoints[i], polygon!!.ypoints[i], CENTER.x, CENTER.y)
    }

    internal fun generate() {
        do {
            generatePoints()
            Collections.sort(points)
            generatePolygon()
        } while (isInvalid())
    }
}