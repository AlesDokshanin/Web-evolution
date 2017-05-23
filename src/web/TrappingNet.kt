package web

internal class TrappingNet private constructor() {
    internal var length: Int = 0
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
        this.length = net.length
        net.circles.forEach { circle -> this.circles.add(TrappingNetCircle(circle)) }
    }

    internal fun recalculateLength() {
        this.length = circles.map { it.length }.sum()
    }

    private fun generateCircles(skeleton: Skeleton) {
        (1..MIN_TRAPPING_NET_CIRCLES_COUNT).forEach {
            val circle = TrappingNetCircle.createOn(skeleton)
            this.circles.add(circle)
        }
    }

    internal fun canAddCircle(skeleton: Skeleton): Boolean {
        return length < Config.maxTrappingNetLength
    }

    internal fun addNewCircle(skeleton: Skeleton) {
        val circle = TrappingNetCircle.createOn(skeleton)
        circles.add(circle)
        length += circle.length
    }

    internal fun tryToCatch(fly: Fly): Boolean {
        val result = circles.any({ c -> c.catchesFly(fly) })
        return result
    }

    internal fun save() {
        this.recalculateLength()
    }
}