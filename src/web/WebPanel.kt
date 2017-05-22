package web

import java.awt.*
import javax.swing.JPanel

class WebPanel(web: Web) : JPanel() {
    internal var web = web
        set

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        g.color = BG_COLOR
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT)
        val g2 = g as Graphics2D
        draw(g2)
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(PANEL_WIDTH, PANEL_HEIGHT)
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
        web.trappingNet.draw(g)
    }
}
