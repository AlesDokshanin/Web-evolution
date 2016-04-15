package web_kotlin

import java.awt.*
import java.util.*


internal val random: Random = Random()
internal val WIDTH = 800
internal val HEIGHT = 800
internal val CENTER: Point = Point(WIDTH / 2, HEIGHT / 2)
internal val MIN_SKELETON_DISTANCE_FROM_CENTER = (Math.min(HEIGHT, WIDTH) / 5).toInt()
internal val MIN_TRAPPING_NET_CIRCLE_DISTANCE = Math.min(WIDTH, HEIGHT) / 75;
internal val TRAPPING_NET_CIRCLES_DISPERSION = 8.0
internal val FLY_SIZE = (Math.min(WIDTH, HEIGHT) / 50).toInt()
internal val MIN_SIDES = 10
internal val MAX_SIDES = 20
internal val MIN_FLIES_COUNT = 10
internal val MAX_FLIES_COUNT = 1000
internal val PART_OF_NORMAL_DISTRIBUTED_FLIES = 0.25f
internal val CAUGHT_FLY_COLOR = Color(94, 104, 205, 128)
internal val UNCAUGHT_FLY_COLOR = Color(37, 205, 7, 128)
internal val CHILDREN_COUNT = 3
internal val MAX_TRAPPING_NET_LENGTH_UPPER = 200 * 1000
internal val MAX_TRAPPING_NET_LENGTH_LOWER = 10 * 1000


class Web : Comparable<Web> {
    internal var trappingNetLength = 0
    var generation = 1
        internal set
    var efficiency = 0.0
        internal set
    internal var skeleton = WebSkeleton()
    internal var trappingNet = ArrayList<TrappingNetCircle>()
    internal var flies: ArrayList<Fly>? = null

    constructor() {
        generateFlies()
        build()
        calculateTrappingNetLength()
    }

    constructor(w: Web) {
        // Primitives
        generation = w.generation
        efficiency = w.efficiency

        // Deep copying
        skeleton = WebSkeleton(w.skeleton)

        trappingNet = ArrayList<TrappingNetCircle>(w.trappingNet.size)
        w.trappingNet.forEach { circle -> trappingNet.add(TrappingNetCircle(circle, this)) }

        if (!WebConfig.dynamicFlies) {
            flies = ArrayList<Fly>(w.flies!!.size)
            for (f in w.flies!!)
                flies!!.add(Fly(f, this))
        } else {
            generateFlies()
        }

        trappingNetLength = w.trappingNetLength

    }

    override fun compareTo(other: Web): Int {
        var value = java.lang.Double.compare(this.efficiency, other.efficiency)
        if (value == 0)
            value = -1 * java.lang.Double.compare(this.trappingNetLength.toDouble(), other.trappingNetLength.toDouble())
        return value
    }

    private fun calculateTrappingNetLength() {
        trappingNetLength = 0

        for (circle in trappingNet)
            trappingNetLength += circle.length
    }

    private fun generateFlies() {
        flies = ArrayList<Fly>(WebConfig.fliesCount)
        for (i in 0..WebConfig.fliesCount - 1) {
            if (WebConfig.normalFliesDistribution || i <= WebConfig.fliesCount * PART_OF_NORMAL_DISTRIBUTED_FLIES)
                flies!!.add(Fly(true, this))
            else
                flies!!.add(Fly(false, this))
        }

    }

    private fun build() {
        skeleton = WebSkeleton()
        trappingNet = ArrayList<TrappingNetCircle>()
        skeleton.generate()
        generateTrappingNet()
        calculateTrappingNetLength()
        calculateEfficiency()
    }

    internal fun reproduce(): ArrayList<Web> {
        val children = ArrayList<Web>(CHILDREN_COUNT)
        for (i in 0..CHILDREN_COUNT - 1) {
            val child = Web(this)
            child.mutate()
            children.add(child)
        }
        generation++
        if (WebConfig.dynamicFlies)
            generateFlies()
        calculateEfficiency()

        children.add(this)

        return children
    }

    internal fun calculateEfficiency() {
        var caught = 0
        for (fly in flies!!) {
            if (fly.checkIfCaught()) {
                caught++
            }
        }
        efficiency = caught.toDouble()
    }

    private fun generateTrappingNet() {
        trappingNet.clear()

        while (true) {
            val circle = TrappingNetCircle(this)
            if (circle.fits)
                trappingNet.add(circle)
            else
                break
        }
    }

    internal fun updateTrappingNet() {
        for (i in 0..WebConfig.sidesCount - 1) {
            val newAngle = skeleton.points[i].angle
            for (circle in trappingNet) {
                circle.points[i].angle = newAngle
            }
        }

        trappingNet.forEach { circle -> circle.save() }
        calculateTrappingNetLength()
    }

    protected fun mutate() {
        val mutationType = WebMutationType.values()[random.nextInt(WebMutationType.values().size)]
        val mutation = WebMutation.Factory.create(mutationType, this)
        mutation.apply()
    }
}
