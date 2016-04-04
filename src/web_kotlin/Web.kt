package web_kotlin

import java.awt.*
import java.awt.geom.Line2D
import java.awt.geom.Rectangle2D
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
    private var flies: ArrayList<Fly>? = null

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
        for (c in w.trappingNet)
            trappingNet.add(TrappingNetCircle(c, this))

        if (!WebConfig.dynamicFlies) {
            flies = ArrayList<Fly>(w.flies!!.size)
            for (f in w.flies!!)
                flies!!.add(Fly(f, this))
        } else {
            generateFlies()
        }

        trappingNetLength = w.trappingNetLength

    }

    override fun compareTo(web: Web): Int {
        var value = java.lang.Double.compare(this.efficiency, web.efficiency)
        if (value == 0)
            value = -1 * java.lang.Double.compare(this.trappingNetLength.toDouble(), web.trappingNetLength.toDouble())
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
        //        efficiency = (double) caught * 10000 / trappingNetLength;
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

        for (c in trappingNet)
            c.save()

        calculateTrappingNetLength()
    }

    internal fun draw(g: Graphics2D) {
        skeleton.draw(g)
        drawTrappingNet(g)
        if (WebConfig.drawFlies) {
            drawFlies(g)
        }
    }

    private fun drawFlies(g: Graphics2D) {
        for (f in flies!!)
            f.draw(g)
    }

    private fun drawTrappingNet(g: Graphics2D) {
        val oldColor = g.color
        val oldStroke = g.stroke
        g.color = Color(255, 0, 0)
        g.stroke = BasicStroke(2f)
        for (circle in trappingNet)
            g.drawPolygon(circle.polygon)
        g.color = oldColor
        g.stroke = oldStroke
    }

    protected fun mutate() {
        val mutationType = WebMutationType.values()[random.nextInt(WebMutationType.values().size)]
        val mutation = WebMutation.Factory.create(mutationType, this)
        mutation.apply()
    }
}

private fun getPolygonFromPolarPoints(points: List<PolarPoint>): Polygon {
    val cartesianPoints: Array<Point> = Array(points.size, { i -> points[i].cartesianPoint() })
    val xPoints = cartesianPoints.map { it.x }.toIntArray()
    val yPoints = cartesianPoints.map { it.y }.toIntArray()

    return Polygon(xPoints, yPoints, cartesianPoints.size)
}

internal class WebSkeleton() {
    var points: ArrayList<PolarPoint>
    var polygon: Polygon? = null

    init {
        points = ArrayList<PolarPoint>()
    }

    constructor(skeleton: WebSkeleton) : this() {
        points.clear()
        skeleton.points.forEach { points.add(PolarPoint(it)) }
    }

    fun generatePolygon() {
        polygon = getPolygonFromPolarPoints(points)
        polygon!!.translate(CENTER.x, CENTER.y)
    }

    private fun generatePoint(): PolarPoint {
        val angle = random.nextDouble() * 2 * Math.PI
        var maxDistance = 0
        var bound: Point

        while (maxDistance <= MIN_SKELETON_DISTANCE_FROM_CENTER) {
            bound = Point((0.5 * WIDTH * Math.cos(angle)).toInt(), (0.5 * HEIGHT * Math.sin(angle)).toInt())
            maxDistance = bound.distance(0.0, 0.0).toInt()
        }

        val distance = (MIN_SKELETON_DISTANCE_FROM_CENTER +
                (maxDistance - MIN_SKELETON_DISTANCE_FROM_CENTER) * random.nextDouble()).toInt()

        return PolarPoint(angle, distance)
    }

    private fun pointIsValid(p: PolarPoint, points: Iterable<PolarPoint>): Boolean {
        return points.all({ currPoint -> Math.abs(p.angle - currPoint.angle) >= WebConfig.minAngleBetweenSkeletonLines })
    }

    private fun centerFitsIntoPolygon(): Boolean {
        val shift = MIN_TRAPPING_NET_CIRCLE_DISTANCE
        for (dx in -1..1) {
            for (dy in -1..1) {
                var p = Point(CENTER.x + dx * shift, CENTER.y + dy * shift)
                if (!polygon!!.contains(p))
                    return false
            }
        }
        return true
    }

    internal fun isInvalid(): Boolean {
        return !centerFitsIntoPolygon()
    }


    private fun generatePoints() {
        points.clear()

        for (i in 1..WebConfig.sidesCount) {
            var p = generatePoint()
            while (!pointIsValid(p, points))
                p = generatePoint()
            points.add(p)
        }
    }

    internal fun draw(g: Graphics2D) {
        g.stroke = BasicStroke(2f)
        g.color = Color(128, 128, 128)
        g.drawPolygon(polygon)

        for (i in 0..polygon!!.npoints - 1)
            g.drawLine(polygon!!.xpoints[i], polygon!!.ypoints[i], CENTER.x, CENTER.y)
    }

    internal fun generate() {
        do {
            generatePoints()
            Collections.sort(points)
            generatePolygon()
        } while (isInvalid())
    }
}

internal class TrappingNetCircle(web: Web) {
    internal val points = ArrayList<PolarPoint>()
    internal var polygon: Polygon? = null
    internal var length = 0
        private set
    internal var fits = false
        private set

    init {
        generateTrappingNetCircle(web.trappingNet, web.skeleton.points)
    }

    constructor(c: TrappingNetCircle, web: Web) : this(web) {
        c.points.forEach { points.add(PolarPoint(it)) }
        save()
    }

    internal fun calculateLength() {
        length = 0

        for (i in 0..polygon!!.xpoints.size - 1) {
            length += Math.sqrt((polygon!!.xpoints[i] * polygon!!.xpoints[i] +
                    polygon!!.ypoints[i] * polygon!!.ypoints[i]).toDouble()).toInt()
        }
    }

    internal fun save() {
        polygon = getPolygonFromPolarPoints(points)
        polygon!!.translate(CENTER.x, CENTER.y)
        calculateLength()
    }

    internal fun generateTrappingNetCircle(trappingNet: List<TrappingNetCircle>, skeletonPoints: List<PolarPoint>) {
        for (i in 0..WebConfig.sidesCount - 1) {
            var lowerBound = MIN_TRAPPING_NET_CIRCLE_DISTANCE
            if (!trappingNet.isEmpty()) {
                lowerBound += trappingNet.last().points[i].distance
            }
            val maxDistance = skeletonPoints[i].distance - MIN_TRAPPING_NET_CIRCLE_DISTANCE
            if (lowerBound > maxDistance) {
                fits = false
                return
            }
            val upperBound = Math.min(maxDistance, lowerBound + (TRAPPING_NET_CIRCLES_DISPERSION * MIN_TRAPPING_NET_CIRCLE_DISTANCE).toInt())
            val angle = skeletonPoints[i].angle
            val distance = (lowerBound + random.nextDouble() * (upperBound - lowerBound)).toInt()
            points.add(PolarPoint(angle, distance))
        }
        save()
        fits = true
    }
}

private class Fly(web: Web) {
    lateinit var body: Rectangle2D
    var caught = false
    val web = web

    constructor(normalDistribution: Boolean, web: Web) : this(web) {
        var x: Int
        var y: Int

        if (normalDistribution) {
            x = random.nextInt() % (WIDTH / 2 - FLY_SIZE)
            y = random.nextInt() % (HEIGHT / 2 - FLY_SIZE)
        } else {
            x = random.nextInt() % (WIDTH / 4 - FLY_SIZE) + WIDTH / 4
            y = random.nextInt() % (HEIGHT / 4 - FLY_SIZE) + HEIGHT / 4
        }
        body = Rectangle2D.Float(x.toFloat(), y.toFloat(), FLY_SIZE.toFloat(), FLY_SIZE.toFloat())
    }

    constructor(f: Fly, web: Web) : this(web) {
        body = Rectangle2D.Float(f.body.x.toFloat(), f.body.y.toFloat(), FLY_SIZE.toFloat(), FLY_SIZE.toFloat())
    }

    internal fun checkIfCaught(): Boolean {
        caught = false
        loop@ for (circle in web.trappingNet) {
            for (i in 0..circle.points.lastIndex) {
                val a = circle.points[i].cartesianPoint()
                val b = circle.points[(i + 1) % circle.points.size].cartesianPoint()
                val line = Line2D.Float(a, b)
                if (body.intersectsLine(line)) {
                    caught = true
                    break@loop
                }
            }
        }
        return caught
    }

    internal fun draw(g: Graphics2D) {
        g.color = if (caught) CAUGHT_FLY_COLOR else UNCAUGHT_FLY_COLOR
        g.background = Color.BLACK
        g.fillRect((CENTER.x + body.x).toInt(), (CENTER.y + body.y).toInt(), FLY_SIZE, FLY_SIZE)
        g.drawRect((CENTER.x + body.x).toInt(), (CENTER.y + body.y).toInt(), FLY_SIZE, FLY_SIZE)
    }
}