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
        return Dimension(800, 800)
    }

    private fun drawBackground(g2: Graphics2D) {
        g2.color = BG_COLOR
        g2.fill(bounds)
    }

    companion object {
        private val BG_COLOR = Color.WHITE
    }

    internal fun draw(g: Graphics2D) {
        g.color = Color.BLACK
        WebDrawer(g).draw(web, Config.drawFlies, bounds)
    }
}
