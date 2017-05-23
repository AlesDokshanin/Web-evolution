package web

import java.awt.*
import javax.swing.JPanel

class Panel(web: Web) : JPanel() {
    internal var web = web
        set

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        drawBackground(g2)
        draw(g2)
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(PANEL_WIDTH, PANEL_HEIGHT)
    }

    private fun drawBackground(g2: Graphics2D) {
        g2.color = BG_COLOR
        g2.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT)
    }

    companion object {
        internal val PANEL_WIDTH = 800
        internal val PANEL_HEIGHT = 800
        private val BG_COLOR = Color.white
    }

    internal fun draw(g: Graphics2D) {
        WebDrawer(g).draw(web, Config.drawFlies)
    }
}
