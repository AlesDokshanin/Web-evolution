package web

import java.awt.*
import java.util.*

internal class WebSkeleton() {
    var points = mutableListOf<PolarPoint>()

    constructor(skeleton: WebSkeleton) : this() {
        skeleton.points.forEach { points.add(PolarPoint(it)) }
    }

    private fun generatePoint(): PolarPoint {
        val angle = random.nextDouble() * 2 * Math.PI
        val maxDistance = maxDistanceForAngle(angle)

        val distance = (MIN_SKELETON_DISTANCE_FROM_CENTER +
                (maxDistance - MIN_SKELETON_DISTANCE_FROM_CENTER) * random.nextDouble()).toInt()

        return PolarPoint(angle, distance)
    }

    private fun pointIsValid(p: PolarPoint, points: Iterable<PolarPoint>): Boolean {
        return true
    }

    private fun centerFitsIntoPolygon(): Boolean {
        val shift = MIN_TRAPPING_NET_CIRCLE_DISTANCE
        val polygon = generatePolygon()

        for (dx in -1..1) {
            for (dy in -1..1) {
                val p = Point(dx * shift, dy * shift)
                if (!polygon.contains(p))
                    return false
            }
        }
        return true
    }

    private fun generatePolygon(): Polygon {
        val polygon = buildPolygonFromPolarPoints(this.points)
        return polygon
    }

    internal fun isInvalid(): Boolean {
        return !centerFitsIntoPolygon()
    }


    private fun generatePoints() {
        points.clear()

        for (i in 1..WebConfig.sidesCount) {
            var p = generatePoint()
            while (!pointIsValid(p, points)) {
                p = generatePoint()
            }
            points.add(p)
        }
    }

    internal fun draw(g: Graphics2D) {
        g.stroke = BasicStroke(2f)
        g.color = Color(128, 128, 128)

        val polygon = this.generatePolygon()
        polygon.translate(WIDTH / 2, HEIGHT / 2)
        g.drawPolygon(polygon)

        for (i in 0..polygon!!.npoints - 1)
            g.drawLine(polygon!!.xpoints[i], polygon!!.ypoints[i], CENTER.x, CENTER.y)
    }

    internal fun generate() {
        do {
            generatePoints()
            Collections.sort(points)
        } while (isInvalid())
    }
}