package web

import java.awt.Polygon
import java.util.*

internal class TrappingNetCircle {
    internal val points = ArrayList<PolarPoint>()
    internal var polygon: Polygon? = null
    internal var length = 0
        private set
    internal var fits = false
        private set

    constructor(web: Web) {
        generate(web.trappingNet, web.skeleton.points)
    }

    constructor(circle: TrappingNetCircle) {
        circle.points.forEach { pt -> points.add(PolarPoint(pt)) }
        save()
    }


    internal fun calculateLength() {
        length = 0

        for (i in 0..polygon!!.xpoints.size - 1) {
            length += Math.sqrt((polygon!!.xpoints[i] * polygon!!.xpoints[i] +
                    polygon!!.ypoints[i] * polygon!!.ypoints[i]).toDouble()).toInt()
        }
    }

    internal fun save() {
        polygon = buildPolygonFromPolarPoints(points)
        polygon!!.translate(CENTER.x, CENTER.y)
        calculateLength()
    }

    internal fun generate(trappingNet: List<TrappingNetCircle>, skeletonPoints: List<PolarPoint>) {
        for (i in 0..WebConfig.sidesCount - 1) {
            var lowerBound = MIN_TRAPPING_NET_CIRCLE_DISTANCE
            if (!trappingNet.isEmpty()) {
                lowerBound += trappingNet.last().points[i].distance
            }
            val maxDistance = skeletonPoints[i].distance - MIN_TRAPPING_NET_CIRCLE_DISTANCE
            if (lowerBound > maxDistance) {
                fits = false
                return
            }
            val upperBound = Math.min(maxDistance, lowerBound + (TRAPPING_NET_CIRCLES_DISPERSION * MIN_TRAPPING_NET_CIRCLE_DISTANCE).toInt())
            val angle = skeletonPoints[i].angle
            val distance = (lowerBound + random.nextDouble() * (upperBound - lowerBound)).toInt()
            points.add(PolarPoint(angle, distance))
        }
        save()
        fits = true
    }
}