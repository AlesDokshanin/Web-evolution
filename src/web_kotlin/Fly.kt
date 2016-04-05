package web_kotlin

import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Line2D
import java.awt.geom.Rectangle2D

internal class Fly(web: Web) {
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