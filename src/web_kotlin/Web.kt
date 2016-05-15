package web_kotlin

import java.util.*


internal val random: Random = Random()


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
        w.trappingNet.forEach { circle -> trappingNet.add(TrappingNetCircle(circle)) }

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

    internal fun calculateTrappingNetLength() {
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

        // FIXME improve efficiency formula
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

    protected fun mutate() {
        val mutationType = WebMutationType.values()[random.nextInt(WebMutationType.values().size)]
        val mutation = WebMutation.Factory.create(mutationType, this)
        mutation.apply()
    }
}
