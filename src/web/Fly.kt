package web

import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Rectangle2D

internal class Fly(val center: PolarPoint) {
    var isCaught: Boolean? = null

    val rect: Rectangle2D.Float by lazy {
        generateRectangle()
    }

    constructor(fly: Fly) : this(fly.center)

    private fun generateRectangle(): Rectangle2D.Float {
        val pt = center.toCartesian()
        val r = Rectangle2D.Float(pt.x.toFloat() - FLY_SIZE / 2,
                pt.y.toFloat() - FLY_SIZE / 2,
                FLY_SIZE.toFloat(), FLY_SIZE.toFloat())
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

            val distance = (random.nextDouble() * maxDistance).toInt()
            val point = PolarPoint(phi, distance)
            return point
        }
    }

    internal fun draw(g: Graphics2D) {
        g.color = if (isCaught == true) CAUGHT_FLY_COLOR else UNCAUGHT_FLY_COLOR
        g.background = Color.BLACK
        g.fillRect((CENTER.x + rect.x).toInt(), (CENTER.y + rect.y).toInt(), FLY_SIZE, FLY_SIZE)
        g.drawRect((CENTER.x + rect.x).toInt(), (CENTER.y + rect.y).toInt(), FLY_SIZE, FLY_SIZE)
    }
}