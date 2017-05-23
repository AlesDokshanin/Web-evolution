package web

import java.awt.geom.Line2D

internal class TrappingNetCircle private constructor() {
    internal val points = mutableListOf<PolarPoint>()
    internal var length = 0
        private set

    constructor(circle: TrappingNetCircle): this() {
        circle.points.forEach { pt -> points.add(PolarPoint(pt)) }
        calculateLength()
    }

    companion object Factory {
        fun createOn(skeleton: Skeleton): TrappingNetCircle {
            val circle = TrappingNetCircle()
            circle.generate(skeleton)
            circle.save()
            return circle
        }
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

    internal fun generate(skeleton: Skeleton) {
        for (i in 0..Config.sidesCount - 1) {
            val lowerBound = 0
            val upperBound = skeleton.points[i].distance
            val angle = skeleton.points[i].angle
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
}