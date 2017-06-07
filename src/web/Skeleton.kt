package web

import java.awt.*
import java.awt.geom.Point2D
import java.util.*

internal class Skeleton private constructor() {
    var points = mutableListOf<PolarPoint>()

    companion object Factory {
        fun generate(): Skeleton {
            val skeleton = Skeleton()
            do {
                skeleton.generatePoints()
                Collections.sort(skeleton.points)
            } while (skeleton.isInvalid())
            return skeleton
        }
    }

    constructor(skeleton: Skeleton) : this() {
        skeleton.points.forEach { points.add(PolarPoint(it)) }
    }

    private fun generatePoint(): PolarPoint {
        val angle = random.nextDouble() * 2 * Math.PI
        val maxDistance = maxDistanceForAngle(angle)

        val distance = (MIN_SKELETON_DISTANCE_FROM_CENTER +
                (maxDistance - MIN_SKELETON_DISTANCE_FROM_CENTER) * random.nextDouble())

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
                val p = Point2D.Double(dx * shift, dy * shift)
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

        for (i in 1..Config.sidesCount) {
            var p = generatePoint()
            while (!pointIsValid(p, points)) {
                p = generatePoint()
            }
            points.add(p)
        }
    }
}