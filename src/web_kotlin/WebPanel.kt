package web_kotlin

import java.awt.*
import java.awt.image.BufferedImage
import java.util.*
import javax.swing.JPanel

class WebPanel(web: Web) : JPanel() {
    private val image = BufferedImage(PANEL_WIDTH, PANEL_HEIGHT,
            BufferedImage.TYPE_INT_RGB)

    var web = web
        get

    init {
        val g = image.graphics
        g.color = BG_COLOR
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT)
        g.dispose()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        g.drawImage(image, 0, 0, null)
        val g2 = g as Graphics2D
        draw(g2)
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(PANEL_WIDTH, PANEL_HEIGHT)
    }

    fun reproduceWeb() {
        val children = web.reproduce()
        Collections.sort(children, Collections.reverseOrder<Any>())
        web = children[0]
    }

    companion object {
        internal val PANEL_WIDTH = 800
        internal val PANEL_HEIGHT = 800
        private val BG_COLOR = Color.white
    }

    internal fun draw(g: Graphics2D) {
        web.skeleton.draw(g)
        drawTrappingNet(g)
        if (WebConfig.drawFlies) {
            drawFlies(g)
        }
    }

    private fun drawFlies(g: Graphics2D) {
        for (f in web.flies!!)
            f.draw(g)
    }

    private fun drawTrappingNet(g: Graphics2D) {
        g.color = Color(255, 0, 0)
        g.stroke = BasicStroke(2f)
        for (circle in web.trappingNet)
            g.drawPolygon(circle.polygon)
    }
}
