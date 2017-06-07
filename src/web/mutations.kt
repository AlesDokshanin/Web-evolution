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

internal abstract class WebMutation protected constructor(protected val web: Web) {
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
        if (web.trappingNet.canAddCircle(web.skeleton)) {
            web.trappingNet.addNewCircle(web.skeleton)
            web.trappingNet.save()
        }
    }
}

private class SkeletonAngleMutation(web: Web) : WebMutation(web) {
    override fun apply() {
        do {
            val i = pickVectorIndex()

            var lowerBound = if (i != 0) web.skeleton.points[i - 1].angle else web.skeleton.points.last().angle
            var upperBound = if (i != web.skeleton.points.lastIndex) web.skeleton.points[i + 1].angle else web.skeleton.points.first().angle

            lowerBound += Config.minAngleBetweenSkeletonLines
            upperBound -= Config.minAngleBetweenSkeletonLines

            if (lowerBound > upperBound)
                upperBound += 2 * Math.PI

            web.skeleton.points[i].angle = ((lowerBound + random.nextDouble() * ((upperBound - lowerBound))) % (2 * Math.PI))

        } while (web.skeleton.isInvalid())

        updateTrappingNet()
    }

    private fun updateTrappingNet() {
        for (i in 0..Config.sidesCount - 1) {
            val newAngle = web.skeleton.points[i].angle
            for (circle in web.trappingNet.circles) {
                circle.points[i].angle = newAngle
            }
        }

        web.trappingNet.circles.forEach { circle -> circle.save() }
        web.trappingNet.save()
    }

    private fun pickVectorIndex(): Int {
        return random.nextInt(Config.sidesCount)
    }
}

    private class SkeletonDistanceMutation(web: Web) : WebMutation(web) {
        override fun apply() {
            do {
                val index = random.nextInt(Config.sidesCount)

                val angle = web.skeleton.points[index].angle
                val bound = Point((0.5 * WIDTH * Math.cos(angle)).toInt(), (0.5 * HEIGHT * Math.sin(angle)).toInt())
                val maxDistance = bound.distance(0.0, 0.0)

                val lowerBound = web.trappingNet.circles.map { c -> c.points[index].distance }.max()!!.toInt()
                web.skeleton.points[index].distance = (lowerBound + random.nextDouble() * (maxDistance - lowerBound))
                // FIXME: hangs sometimes

            } while (web.skeleton.isInvalid())
        }
    }


    private class TrappingNetCircleRemovalMutation(web: Web) : WebMutation(web) {
        override fun apply() {
            if (web.trappingNet.circles.size >= MIN_TRAPPING_NET_CIRCLES_COUNT) {
                val index = random.nextInt(web.trappingNet.circles.size)
                val deletedCircleLength = web.trappingNet.circles[index].perimeter
                web.trappingNet.circles.removeAt(index)
                web.trappingNet.length -= deletedCircleLength
            }
        }
    }

    private class VectorPointDistances private constructor(pointsCount: Int, minDistance: Double, maxDistance: Double) {

        internal val distances = mutableListOf<Double>()
        private var pointsCount: Int = pointsCount
        private var minDistance: Double = minDistance
        private var maxDistance: Double = maxDistance


        init {
            generate()
        }

        private fun generate() {
            while (distances.size < pointsCount) {
                val newDistance = minDistance + random.nextDouble() * (maxDistance - minDistance)
                distances.add(newDistance)
            }
            distances.sort()
        }


        companion object Factory {
            internal fun generate(pointsCount: Int, maxDistance: Double, minDistance: Double = 0.0): VectorPointDistances {
                return VectorPointDistances(pointsCount, minDistance, maxDistance)
            }
        }
    }

    private class TrappingNetVectorRegenerateMutation(web: Web) : WebMutation(web) {
        override fun apply() {
            val vectorIndex = random.nextInt(Config.sidesCount)
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
        val vectorIndex = random.nextInt(Config.sidesCount)
        val maxDistance = web.skeleton.points[vectorIndex].distance
        val minFactor = 0.5
        val maxExistingDistance = web.trappingNet.circles.map { it.points[vectorIndex].distance }.max()
        var maxFactor = 1.0
        maxExistingDistance?.let { maxFactor = maxDistance.toDouble() / maxExistingDistance }

        val factor = minFactor + random.nextDouble() * (maxFactor - minFactor)
        for(circle in web.trappingNet.circles) {
            val point = circle.points[vectorIndex]
            point.distance = (point.distance * factor)
            circle.save()
        }
        web.trappingNet.recalculateLength()
    }
}