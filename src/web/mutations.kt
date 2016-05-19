package web

import java.awt.Point

private val TRAPPING_NET_DISPERSION = 1

enum class WebMutationType {
    SKELETON_ANGLE,
    SKELETON_DISTANCE,
    TRAPPING_NET_ANGLE,
    TRAPPING_NET_VECTOR,
}

internal abstract class WebMutation protected constructor(web: Web) {
    val web = web

    open internal fun apply() {
        postProcess()
        web.generation++
        web.calculateEfficiency()
    }

    private fun postProcess() {
        tryToAddTrappingNetCircle()
    }

    private fun tryToAddTrappingNetCircle() {
        if (web.trappingNetLength > WebConfig.maxTrappingNetLength)
            return

        val netCircle = TrappingNetCircle(web)
        if (netCircle.fits) {
            netCircle.save()
            web.trappingNet.add(netCircle)
            web.trappingNetLength += netCircle.length
        }
    }

    internal companion object Factory {
        fun create(type: WebMutationType, web: Web): WebMutation {
            when (type) {
                WebMutationType.SKELETON_ANGLE -> return SkeletonAngleMutation(web)
                WebMutationType.TRAPPING_NET_ANGLE -> return TrappingNetCirleMutation(web)
                WebMutationType.SKELETON_DISTANCE -> return SkeletonDistanceMutation(web)
                WebMutationType.TRAPPING_NET_VECTOR -> return TrappingNetVectorMutation(web)
                else -> throw IllegalArgumentException("Not supported web mutation type")
            }
        }
    }
}

private class SkeletonAngleMutation(web: Web) : WebMutation(web) {
    override fun apply() {
        do {
            val i = vectorIndex

            var lowerBound = if (i != 0) web.skeleton.points[i - 1].angle else web.skeleton.points.last().angle
            var upperBound = if (i != web.skeleton.points.lastIndex) web.skeleton.points[i + 1].angle else web.skeleton.points.first().angle

            lowerBound += WebConfig.minAngleBetweenSkeletonLines
            upperBound -= WebConfig.minAngleBetweenSkeletonLines

            if (lowerBound > upperBound)
                upperBound += 2 * Math.PI

            web.skeleton.points[i].angle = ((lowerBound + random.nextDouble() * ((upperBound - lowerBound))) % (2 * Math.PI))

        } while (web.skeleton.isInvalid())

        web.skeleton.generatePolygon()
        updateTrappingNet()

        super.apply()
    }

    private fun updateTrappingNet() {
        for (i in 0..WebConfig.sidesCount - 1) {
            val newAngle = web.skeleton.points[i].angle
            for (circle in web.trappingNet) {
                circle.points[i].angle = newAngle
            }
        }

        web.trappingNet.forEach { circle -> circle.save() }
        web.calculateTrappingNetLength()
    }

    private val vectorIndex: Int
        get() = random.nextInt(WebConfig.sidesCount)
}

private class SkeletonDistanceMutation(web: Web) : WebMutation(web) {

    override fun apply() {
        do {
            val index = vectorIndex

            val angle = web.skeleton.points[index].angle
            val bound = Point((0.5 * WIDTH.toDouble() * Math.cos(angle)).toInt(), (0.5 * HEIGHT.toDouble() * Math.sin(angle)).toInt())
            val maxDistance = bound.distance(0.0, 0.0).toInt().toDouble()

            val lowerBound = web.trappingNet.last().points[index].distance + MIN_TRAPPING_NET_CIRCLE_DISTANCE
            web.skeleton.points[index].distance = (lowerBound + random.nextDouble() * (maxDistance - lowerBound)).toInt()
        } while (web.skeleton.isInvalid())

        web.skeleton.generatePolygon()

        super.apply()
    }

    private val vectorIndex: Int
        get() = random.nextInt(WebConfig.sidesCount)
}

private class TrappingNetCirleMutation(web: Web) : WebMutation(web) {
    override fun apply() {
        val circleIndex = random.nextInt(web.trappingNet.lastIndex)
        val oldCircleLength = web.trappingNet[circleIndex].length

        for (i in 0..WebConfig.sidesCount - 1) {
            var lowerBound: Int
            var upperBound: Int

            if (circleIndex == 0)
                lowerBound = MIN_TRAPPING_NET_CIRCLE_DISTANCE
            else
                lowerBound = web.trappingNet[circleIndex - 1].points[i].distance + MIN_TRAPPING_NET_CIRCLE_DISTANCE

            if (circleIndex == web.trappingNet.lastIndex)
                upperBound = web.skeleton.points[i].distance - MIN_TRAPPING_NET_CIRCLE_DISTANCE
            else
                upperBound = web.trappingNet[circleIndex + 1].points[i].distance - MIN_TRAPPING_NET_CIRCLE_DISTANCE

            val currentDistance = web.trappingNet[circleIndex].points[i].distance
            val lowerDistance = currentDistance - TRAPPING_NET_DISPERSION * MIN_TRAPPING_NET_CIRCLE_DISTANCE
            val upperDistance = currentDistance + TRAPPING_NET_DISPERSION * MIN_TRAPPING_NET_CIRCLE_DISTANCE

            lowerBound = Math.max(lowerBound, lowerDistance)
            upperBound = Math.min(upperBound, upperDistance)

            web.trappingNet[circleIndex].points[i].distance = lowerBound + (random.nextDouble() * (upperBound - lowerBound)).toInt()
        }

        web.trappingNet[circleIndex].save()
        val newCircleLength = web.trappingNet[circleIndex].length
        web.trappingNetLength = web.trappingNetLength - oldCircleLength + newCircleLength

        super.apply()
    }
}


private class TrappingNetCircleRemovalMutation(web: Web) : WebMutation(web) {
    // It doesn't works fine with current efficiency criteria, so we don't want to use this
    override fun apply() {
        if (web.trappingNet.size < 3)
            return

        val index = random.nextInt(web.trappingNet.size)
        val deletedCircleLength = web.trappingNet[index].length
        web.trappingNet.removeAt(index)
        web.trappingNetLength -= deletedCircleLength
    }
}

private class VectorPointDistances private constructor(pointsCount: Int, minDistance: Int, maxDistance: Int, minDistanceBetweenPoints: Int) {

    internal val distances = mutableListOf<Int>()
    private var pointsCount: Int = pointsCount
    private var minDistance: Int = minDistance
    private var maxDistance: Int = maxDistance
    private var minDistanceBetweenPoints: Int = minDistanceBetweenPoints


    init {
        generate()
    }

    private fun generate() {
        while (distances.size < pointsCount) {
            val newDistance = minDistance + random.nextInt(maxDistance - minDistance)
            if (canInsertPoint(newDistance))
                distances.add(newDistance)

        }

        distances.sort()
    }


    private fun canInsertPoint(newPointDistance: Int): Boolean {
        return distances.all { Math.abs(it - newPointDistance) >= minDistanceBetweenPoints }
    }

    companion object Factory {
        internal fun generate(pointsCount: Int, maxDistance: Int, minDistance: Int = MIN_TRAPPING_NET_CIRCLE_DISTANCE,
                              minDistanceBetweenPoints: Int = MIN_TRAPPING_NET_CIRCLE_DISTANCE): VectorPointDistances {

            return VectorPointDistances(pointsCount, minDistance, maxDistance, minDistanceBetweenPoints)

        }
    }
}

private class TrappingNetVectorMutation(web: Web) : WebMutation(web) {

    override fun apply() {
        val vectorIndex = random.nextInt(web.trappingNet.size)
        val maxDistance = web.skeleton.points[vectorIndex].distance - MIN_TRAPPING_NET_CIRCLE_DISTANCE
        val pointsCount = web.trappingNet.size

        val distances = VectorPointDistances.generate(pointsCount, maxDistance).distances
        distances.forEachIndexed { i, distance ->
            web.trappingNet[i].points[vectorIndex].distance = distance
            web.trappingNet[i].save()
        }

        web.trappingNetLength = web.trappingNet.map { it.length }.sum()
        super.apply()
    }
}