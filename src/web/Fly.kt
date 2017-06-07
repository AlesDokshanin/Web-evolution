package web

import java.awt.geom.Rectangle2D

internal class Fly(val center: PolarPoint) {
    var isCaught: Boolean? = null

    val rect: Rectangle2D.Double
        get() = generateRectangle()

    constructor(fly: Fly) : this(fly.center)

    private fun generateRectangle(): Rectangle2D.Double {
        val pt = center.toCartesian()
        val r = Rectangle2D.Double(pt.x - FLY_SIZE / 2,
                pt.y - FLY_SIZE / 2,
                FLY_SIZE, FLY_SIZE)
        return r
    }

    internal companion object Factory {
        internal fun generate(normalDistribution: Boolean): Fly {
            val center = generateCenterPoint(normalDistribution)
            val instance = Fly(center)
            return instance
        }

        private fun generateCenterPoint(normalDistribution: Boolean): PolarPoint {
            val maxAngle: Double = 2 * Math.PI
            val minAngle: Double = if (normalDistribution) 0.0 else 1.5 * Math.PI
            val phi = minAngle + random.nextDouble() * (maxAngle - minAngle)

            val maxDistance = maxDistanceForAngle(phi)

            val distance = (random.nextDouble() * maxDistance)
            val point = PolarPoint(phi, distance)
            return point
        }
    }
}