package web

import java.util.*


internal val random: Random = Random()


class Web : Comparable<Web> {
    var generation = 1
        internal set

    var efficiency = 0.0
        internal set

    internal var skeleton = WebSkeleton()
    internal var trappingNet = TrappingNet(this)
    internal var flies: MutableList<Fly>? = null


    constructor() {
        generateFlies()
        build()
    }

    internal val trappingNetLength: Int
        get() = this.trappingNet.length

    constructor(w: Web) {
        // Primitives
        generation = w.generation
        efficiency = w.efficiency

        // Deep copying
        skeleton = WebSkeleton(w.skeleton)
        trappingNet = TrappingNet(w.trappingNet)

        if (!WebConfig.dynamicFlies) {
            flies = ArrayList<Fly>(w.flies!!.size)
            for (f in w.flies!!)
                flies!!.add(Fly(f))
        } else {
            generateFlies()
        }
    }

    override fun compareTo(other: Web): Int {
        var value = java.lang.Double.compare(this.efficiency, other.efficiency)
        if (value == 0)
            value = -1 * java.lang.Double.compare(this.trappingNetLength.toDouble(), other.trappingNetLength.toDouble())
        return value
    }

    private fun generateFlies() {
        flies = mutableListOf<Fly>()

        (0..WebConfig.fliesCount - 1).forEach { i ->
            val abnormalDistribution = !WebConfig.normalFliesDistribution &&
                    i > WebConfig.fliesCount * PART_OF_NORMAL_DISTRIBUTED_FLIES
            val normalDistribution = !abnormalDistribution
            flies!!.add(Fly.Factory.generate(normalDistribution))
        }
    }

    private fun build() {
        this.skeleton = WebSkeleton()
        this.skeleton.generate()
        this.trappingNet.generate()
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
        val caught = calculateCaughtFlies()
        efficiency = caught.toDouble() +  (1 / trappingNetLength)
    }

    private fun calculateCaughtFlies(): Int {
        flies!!.forEach { f -> trappingNet.tryToCatch(f) }
        val caught = flies!!.count { it.isCaught == true }
        return caught
    }


    private fun mutate() {
        val mutationType = WebMutationType.values()[random.nextInt(WebMutationType.values().size)]
        val mutation = WebMutation.Factory.create(mutationType, this)
        mutation.run()
    }
}
