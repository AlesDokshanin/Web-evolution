package web

import java.awt.Graphics2D

internal class TrappingNet(internal val web: Web) {
    internal var length: Int = 0
    internal val circles: MutableList<TrappingNetCircle> = mutableListOf()

    internal fun generate() {
        this.generateCircles()
        this.recalculateLength()
    }

    internal constructor(net: TrappingNet) : this(net.web) {
        this.length = net.length
        net.circles.forEach { circle -> this.circles.add(TrappingNetCircle(circle)) }
    }

    internal fun recalculateLength() {
        this.length = circles.map { it.length }.sum()
    }

    private fun generateCircles() {
        for (i in 1..MIN_TRAPPING_NET_CIRCLES_COUNT) {
            val circle = TrappingNetCircle(web)
            this.circles.add(circle)
        }
    }

    internal val canAddMoreCircles: Boolean
        get() = this.length < WebConfig.maxTrappingNetLength

    internal fun addNewCircle() {
        val circle = TrappingNetCircle(this.web)
        this.circles.add(circle)
        this.recalculateLength()
    }

    internal fun tryToCatch(fly: Fly): Boolean {
        val result = circles.any({c -> c.catchesFly(fly)})
        return result
    }

    internal fun draw(g: Graphics2D) {
        for(circle in circles)
            circle.draw(g)
    }
}