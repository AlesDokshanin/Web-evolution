package web_kotlin

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.util.*
import javax.swing.JPanel

class WebPanel : JPanel() {
    private val image = BufferedImage(PANEL_WIDTH, PANEL_HEIGHT,
            BufferedImage.TYPE_INT_RGB)

    private var web = Web()

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
        web.draw(g2)
    }

    override fun getPreferredSize(): Dimension {
        return Dimension(PANEL_WIDTH, PANEL_HEIGHT)
    }

    val webEfficiency: Double
        get() = web.efficiency

    val generation: Double
        get() = web.generation.toDouble()

    fun resetWeb() {
        web = Web()
    }

    fun toggleDrawFlies() {
        WebConfig.drawFlies = !WebConfig.drawFlies
    }

    fun reproduceWeb() {
        val children = web.reproduce()
        Collections.sort(children, Collections.reverseOrder<Any>())
        web = children[0]
    }

    val trappingNetLength: Int
        get() = web.trappingNetLength

    companion object {
        internal val PANEL_WIDTH = 800
        internal val PANEL_HEIGHT = 800
        private val BG_COLOR = Color.white
    }
}
