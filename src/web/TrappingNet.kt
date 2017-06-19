package web

internal class TrappingNet private constructor() {
    internal var perimeter: Double = 0.0
    internal val circles = mutableListOf<TrappingNetCircle>()

    companion object Factory {
        fun generateFor(skeleton: Skeleton): TrappingNet {
            val net = TrappingNet()
            net.generateCircles(skeleton)
            net.save()
            return net
        }
    }

    internal constructor(net: TrappingNet) : this() {
        this.perimeter = net.perimeter
        net.circles.forEach { circle -> this.circles.add(TrappingNetCircle(circle)) }
    }

    internal fun recalculateLength() {
        this.perimeter = circles.map { it.perimeter }.sum()
    }

    private fun generateCircles(skeleton: Skeleton) {
        (1..MIN_TRAPPING_NET_CIRCLES_COUNT).forEach {
            val circle = TrappingNetCircle.createOn(skeleton)
            this.circles.add(circle)
        }
    }

    internal fun canAddCircle(skeleton: Skeleton): Boolean {
        return perimeter < Config.maxTrappingNetPerimeter
    }

    internal fun addNewCircle(skeleton: Skeleton) {
        val circle = TrappingNetCircle.createOn(skeleton)
        circles.add(circle)
        perimeter += circle.perimeter
    }

    internal fun tryToCatch(fly: Fly): Boolean {
        val result = circles.any({ c -> c.catchesFly(fly) })
        return result
    }

    internal fun save() {
        this.recalculateLength()
    }
}