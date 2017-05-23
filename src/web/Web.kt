package web

import java.util.*


internal val random: Random = Random()

private fun copyFlies(flies: MutableList<Fly>): MutableList<Fly> {
    val copy = mutableListOf<Fly>()
    flies.forEach { f -> copy.add(Fly(f)) }
    return copy
}

class Web internal constructor(val skeleton: Skeleton, val trappingNet: TrappingNet, val flies: MutableList<Fly>) : Comparable<Web> {
    var generation = 1
        internal set

    var efficiency = 0.0
        internal set

    init {
        calculateEfficiency()
    }

    companion object Factory {
        fun generate(): Web {
            val skeleton = Skeleton.generate()
            val trappingNet = TrappingNet.generateFor(skeleton)
            val flies = generateFlies()
            val web = Web(skeleton, trappingNet, flies)
            return web
        }

        private fun generateFlies(): MutableList<Fly> {
            val flies = mutableListOf<Fly>()
            (0..Config.fliesCount - 1).forEach { i ->
                val abnormalDistribution = !Config.normalFliesDistribution &&
                        i > Config.fliesCount * PART_OF_NORMAL_DISTRIBUTED_FLIES
                val normalDistribution = !abnormalDistribution
                flies.add(Fly.generate(normalDistribution))
            }
            return flies
        }
    }

    constructor(w: Web): this(Skeleton(w.skeleton), TrappingNet(w.trappingNet), copyFlies(w.flies)) {
        generation = w.generation
        efficiency = w.efficiency
    }

    override fun compareTo(other: Web): Int {
        var value = java.lang.Double.compare(this.efficiency, other.efficiency)
        if (value == 0)
            value = -1 * java.lang.Double.compare(this.trappingNet.length.toDouble(), other.trappingNet.length.toDouble())
        return value
    }

    private fun generateFlies() {

    }

    internal fun reproduce(): ArrayList<Web> {
        val children = ArrayList<Web>(CHILDREN_COUNT)
        for (i in 0..CHILDREN_COUNT - 1) {
            val child = Web(this)
            child.mutate()
            children.add(child)
        }
        generation++
        if (Config.dynamicFlies)
            generateFlies()

        calculateEfficiency()
        children.add(this)
        return children
    }

    internal fun calculateEfficiency() {
        val caught = calculateCaughtFlies()
        efficiency = caught.toDouble() + (1 / trappingNet.length)
    }

    private fun calculateCaughtFlies(): Int {
        flies.forEach { f -> trappingNet.tryToCatch(f) }
        val caught = flies.count { it.isCaught == true }
        return caught
    }


    private fun mutate() {
        val mutationType = WebMutationType.values()[random.nextInt(WebMutationType.values().size)]
        val mutation = WebMutation.Factory.create(mutationType, this)
        mutation.run()
    }
}
