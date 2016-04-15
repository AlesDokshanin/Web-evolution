package web_kotlin

import java.awt.Point

private val TRAPPING_NET_DISPERSION = 1

enum class WebMutationType {
    SKELETON_ANGLE,
    SKELETON_DISTANCE,
    TRAPPING_NET
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
                WebMutationType.TRAPPING_NET -> return TrappingNetMutation(web)
                WebMutationType.SKELETON_DISTANCE -> return SkeletonDistanceMutation(web)
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
            upperBound += WebConfig.minAngleBetweenSkeletonLines

            if (lowerBound > upperBound)
                upperBound += 2 * Math.PI

            web.skeleton.points[i].angle = ((lowerBound + random.nextDouble() * ((upperBound - lowerBound))) % (2 * Math.PI))

        } while (web.skeleton.isInvalid())

        web.skeleton.generatePolygon()
        web.updateTrappingNet()

        super.apply()
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

private class TrappingNetMutation(web: Web) : WebMutation(web) {

    protected fun deleteRandomCircle() {
        val index = random.nextInt(web.trappingNet.size)
        val deletedCircleLength = web.trappingNet[index].length
        web.trappingNet.removeAt(index)
        web.trappingNetLength -= deletedCircleLength
    }

    override fun apply() {
        val index = random.nextInt(web.trappingNet.size)
        val oldCircleLength = web.trappingNet[index].length

        for (i in 0..WebConfig.sidesCount - 1) {
            var lowerBound: Int
            var upperBound: Int

            if (index == 0)
                lowerBound = MIN_TRAPPING_NET_CIRCLE_DISTANCE
            else
                lowerBound = web.trappingNet[index - 1].points[i].distance + MIN_TRAPPING_NET_CIRCLE_DISTANCE

            if (index == web.trappingNet.size - 1)
                upperBound = web.skeleton.points[i].distance - MIN_TRAPPING_NET_CIRCLE_DISTANCE
            else
                upperBound = web.trappingNet[index + 1].points[i].distance - MIN_TRAPPING_NET_CIRCLE_DISTANCE

            val currentDistance = web.trappingNet[index].points[i].distance
            val lowerDistance = currentDistance - TRAPPING_NET_DISPERSION * MIN_TRAPPING_NET_CIRCLE_DISTANCE
            val upperDistance = currentDistance + TRAPPING_NET_DISPERSION * MIN_TRAPPING_NET_CIRCLE_DISTANCE

            lowerBound = Math.max(lowerBound, lowerDistance)
            upperBound = Math.min(upperBound, upperDistance)

            web.trappingNet[index].points[i].distance = lowerBound + (random.nextDouble() * (upperBound - lowerBound)).toInt()
        }

        web.trappingNet[index].save()
        val newCircleLength = web.trappingNet[index].length
        web.trappingNetLength = web.trappingNetLength - oldCircleLength + newCircleLength

        super.apply()
    }
}
