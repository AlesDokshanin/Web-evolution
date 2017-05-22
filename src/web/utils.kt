package web

import java.awt.Point
import java.awt.Polygon


internal fun buildPolygonFromPolarPoints(points: List<PolarPoint>): Polygon {
    val cartesianPoints: Array<Point> = Array(points.size, { i -> points[i].toCartesian() })
    val xPoints = cartesianPoints.map { it.x }.toIntArray()
    val yPoints = cartesianPoints.map { it.y }.toIntArray()

    return Polygon(xPoints, yPoints, cartesianPoints.size)
}

internal fun maxDistanceForAngle(phi: Double): Double {
    var phiInPiBy4Range = phi

    while (phiInPiBy4Range > Math.PI / 4)
        phiInPiBy4Range -= Math.PI / 2
    while (phiInPiBy4Range < -Math.PI / 4)
        phiInPiBy4Range += Math.PI / 2

    val maxDistance = 0.5 * WIDTH / Math.cos(phiInPiBy4Range)
    return maxDistance
}