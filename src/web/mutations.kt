package web

import java.awt.Point

enum class WebMutationType {
    SKELETON_ANGLE,
    SKELETON_DISTANCE,
    TRAPPING_NET_VECTOR_REGENERATE,
    TRAPPING_NET_VECTOR_RESCALE,
    TRAPPING_NET_ADD_CIRCLE,
    TRAPPING_NET_REMOVE_CIRCLE
}

internal abstract class WebMutation protected constructor(web: Web) {
    val web = web

    internal fun run() {
        this.apply()
        this.postMutationStuff()
    }

    internal fun postMutationStuff() {
        web.generation++
        web.calculateEfficiency()
    }

    abstract internal fun apply()

    internal companion object Factory {
        fun create(type: WebMutationType, web: Web): WebMutation {
            when (type) {
                WebMutationType.SKELETON_ANGLE -> return SkeletonAngleMutation(web)
                WebMutationType.TRAPPING_NET_ADD_CIRCLE -> return TrappingNetAddCircleMutation(web)
                WebMutationType.TRAPPING_NET_REMOVE_CIRCLE -> return TrappingNetCircleRemovalMutation(web)
                WebMutationType.SKELETON_DISTANCE -> return SkeletonDistanceMutation(web)
                WebMutationType.TRAPPING_NET_VECTOR_REGENERATE -> return TrappingNetVectorRegenerateMutation(web)
                WebMutationType.TRAPPING_NET_VECTOR_RESCALE -> return TrappingNetVectorRescaleMutation(web)
                else -> throw IllegalArgumentException("Not supported web mutation type")
            }
        }
    }
}

private class TrappingNetAddCircleMutation(web: Web) : WebMutation(web) {
    override fun apply() {
        if (web.trappingNet.canAddCircle())
            web.trappingNet.addNewCircle()
    }
}

private class SkeletonAngleMutation(web: Web) : WebMutation(web) {
    override fun apply() {
        do {
            val i = this.pickVectorIndex()

            var lowerBound = if (i != 0) web.skeleton.points[i - 1].angle else web.skeleton.points.last().angle
            var upperBound = if (i != web.skeleton.points.lastIndex) web.skeleton.points[i + 1].angle else web.skeleton.points.first().angle

            lowerBound += WebConfig.minAngleBetweenSkeletonLines
            upperBound -= WebConfig.minAngleBetweenSkeletonLines

            if (lowerBound > upperBound)
                upperBound += 2 * Math.PI

            web.skeleton.points[i].angle = ((lowerBound + random.nextDouble() * ((upperBound - lowerBound))) % (2 * Math.PI))

        } while (web.skeleton.isInvalid())

        updateTrappingNet()
    }

    private fun updateTrappingNet() {
        for (i in 0..WebConfig.sidesCount - 1) {
            val newAngle = web.skeleton.points[i].angle
            for (circle in web.trappingNet.circles) {
                circle.points[i].angle = newAngle
            }
        }

        web.trappingNet.circles.forEach { circle -> circle.save() }
        web.trappingNet.recalculateLength()
    }

    private fun pickVectorIndex(): Int {
        return random.nextInt(WebConfig.sidesCount)
    }
}

    private class SkeletonDistanceMutation(web: Web) : WebMutation(web) {

        override fun apply() {
            do {
                val index = random.nextInt(WebConfig.sidesCount)

                val angle = web.skeleton.points[index].angle
                val bound = Point((0.5 * WIDTH.toDouble() * Math.cos(angle)).toInt(), (0.5 * HEIGHT.toDouble() * Math.sin(angle)).toInt())
                val maxDistance = bound.distance(0.0, 0.0)

                val lowerBound = web.trappingNet.circles.map { c -> c.points[index].distance }.max()!!.toInt()
                web.skeleton.points[index].distance = (lowerBound + random.nextDouble() * (maxDistance - lowerBound)).toInt()
                // FIXME: hangs sometimes

            } while (web.skeleton.isInvalid())
        }
    }


    private class TrappingNetCircleRemovalMutation(web: Web) : WebMutation(web) {
        override fun apply() {
            if (web.trappingNet.circles.size >= MIN_TRAPPING_NET_CIRCLES_COUNT) {
                val index = random.nextInt(web.trappingNet.circles.size)
                val deletedCircleLength = web.trappingNet.circles[index].length
                web.trappingNet.circles.removeAt(index)
                web.trappingNet.length -= deletedCircleLength
            }
        }
    }

    private class VectorPointDistances private constructor(pointsCount: Int, minDistance: Int, maxDistance: Int) {

        internal val distances = mutableListOf<Int>()
        private var pointsCount: Int = pointsCount
        private var minDistance: Int = minDistance
        private var maxDistance: Int = maxDistance


        init {
            generate()
        }

        private fun generate() {
            while (distances.size < pointsCount) {
                val newDistance = minDistance + random.nextInt(maxDistance - minDistance)
                distances.add(newDistance)
            }
            distances.sort()
        }


        companion object Factory {
            internal fun generate(pointsCount: Int, maxDistance: Int, minDistance: Int = 0): VectorPointDistances {
                return VectorPointDistances(pointsCount, minDistance, maxDistance)
            }
        }
    }

    private class TrappingNetVectorRegenerateMutation(web: Web) : WebMutation(web) {
        override fun apply() {
            val vectorIndex = random.nextInt(WebConfig.sidesCount)
            val maxDistance = web.skeleton.points[vectorIndex].distance
            val pointsCount = web.trappingNet.circles.size

            val distances = VectorPointDistances.generate(pointsCount, maxDistance).distances
            distances.forEachIndexed { i, distance ->
                web.trappingNet.circles[i].points[vectorIndex].distance = distance
                web.trappingNet.circles[i].save()
            }
            web.trappingNet.recalculateLength()
        }
    }

private class TrappingNetVectorRescaleMutation(web: Web): WebMutation(web) {
    override fun apply() {
        val vectorIndex = random.nextInt(WebConfig.sidesCount)
        val maxDistance = web.skeleton.points[vectorIndex].distance
        val minFactor = 0.5
        val maxExistingDistance = web.trappingNet.circles.map { it.points[vectorIndex].distance }.max()
        var maxFactor = 1.0
        maxExistingDistance?.let { maxFactor = maxDistance.toDouble() / maxExistingDistance }

        val factor = minFactor + random.nextDouble() * (maxFactor - minFactor)
        for(circle in web.trappingNet.circles) {
            val point = circle.points[vectorIndex]
            point.distance = (point.distance * factor).toInt()
            circle.save()
        }
        web.trappingNet.recalculateLength()
    }
}