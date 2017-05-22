package web

import java.awt.Point

internal class PolarPoint(internal var angle: Double, internal var distance: Int) : Comparable<PolarPoint> {
    constructor(ppt: PolarPoint) : this(ppt.angle, ppt.distance)

    override fun compareTo(other: PolarPoint): Int {
        return angle.compareTo(other.angle)
    }

    fun toCartesian(): Point {
        return Point((Math.cos(angle) * distance).toInt(), (Math.sin(angle) * distance).toInt());
    }

    fun distanceTo(pt: PolarPoint): Double {
        val r1 = this.distance
        val phi1 = this.angle
        val r2 = pt.distance
        val phi2 = pt.angle

        val d = Math.sqrt(r1 * r1 + r2 * r2 - 2 * r1 * r2 * Math.cos(phi2 - phi1))
        return d
    }
}

private fun angleFromCartesian(x: Int, y: Int): Double {
    return Math.atan(y.toDouble() / x)
}

private fun distanceFromCartesian(x: Int, y: Int): Int {
    return Math.sqrt(x.toDouble() * x + y * y).toInt()
}