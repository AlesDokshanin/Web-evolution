import web.Web
import web.Config
import web.Panel

import java.awt.BorderLayout
import java.awt.Container
import java.awt.Dimension
import java.util.*
import javax.swing.*
import javax.swing.border.BevelBorder

class MainWindow constructor(web: Web) {
    private var web = web
        set(value) {
            field = value
            webPanel.web = value
        }

    lateinit private var frame: JFrame

    private val statusPanel = JPanel()
    private val statusLabel = JLabel("Ready")
    private val controlsPanel = ControlsPanel()

    private val webPanel = Panel(web)

    init {
        createAndShowUI()
    }

    private fun createAndShowUI() {
        setUpFrame()
        setUpControlsPanel()
        addListeners()
        addComponentsToPane(frame.contentPane)
        frame.pack()
        frame.isVisible = true
    }

    private fun setUpControlsPanel() {
        controlsPanel.sidesCountSpinner.value = Config.sidesCount
        controlsPanel.fliesCountSpinner.value = Config.fliesCount
        controlsPanel.maxLengthSpinner.value = Config.maxTrappingNetLength / 1000
        controlsPanel.reproduceGenerationsSpinner.value = 1
    }

    private fun setUpFrame() {
        frame = JFrame("Web evolution")
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.layout = BorderLayout()

        statusPanel.border = BevelBorder(BevelBorder.LOWERED)
        statusPanel.preferredSize = Dimension(frame.width, 20)
        statusPanel.layout = BoxLayout(statusPanel, BoxLayout.X_AXIS)
        statusLabel.horizontalAlignment = SwingConstants.LEFT
        statusPanel.add(statusLabel)
    }

    private fun addListeners() {
        controlsPanel.drawFliesCb.addActionListener {
            Config.drawFlies = !Config.drawFlies
            frame.repaint()
        }
        controlsPanel.sidesCountSpinner.addChangeListener {
            val value = controlsPanel.sidesCountSpinner.value as Int
            try {
                Config.sidesCount = value
                resetWeb()

            } catch (e: IllegalArgumentException) {
                controlsPanel.sidesCountSpinner.value = Config.sidesCount
            }

        }

        controlsPanel.fliesCountSpinner.addChangeListener {
            val value = controlsPanel.fliesCountSpinner.value as Int
            try {
                Config.fliesCount = value
                resetWeb()
            } catch (e: IllegalArgumentException) {
                controlsPanel.fliesCountSpinner.value = Config.fliesCount
            }
        }
        controlsPanel.reproduceGenerationsSpinner.addChangeListener {
            val value = controlsPanel.reproduceGenerationsSpinner.value as Int
            if (value < 1)
                controlsPanel.reproduceGenerationsSpinner.value = 1
        }
        controlsPanel.reproduceBtn.addActionListener {
            controlsPanel.lockControls()

            object : Thread() {
                override fun run() {
                    controlsPanel.lockControls()

                    val totalIterations = controlsPanel.reproduceGenerationsSpinner.value as Int
                    var updateProgressStep = totalIterations / 100 - 1
                    updateProgressStep = if (updateProgressStep < 1) 1 else updateProgressStep
                    for (i in 0..totalIterations - 1) {
                        reproduceWeb()

                        if (totalIterations >= 50 && i % updateProgressStep == 0)
                            setStatusBarWorkingText(100 * i / totalIterations)
                    }
                    updateStatusBarText()
                    controlsPanel.unlockControls()
                    frame.repaint()
                }
            }.start()

            controlsPanel.unlockControls()
        }
        controlsPanel.maxLengthSpinner.addChangeListener {
            val value = controlsPanel.maxLengthSpinner.value as Int
            try {
                Config.maxTrappingNetLength = 1000 * value
            } catch (e: IllegalArgumentException) {
                controlsPanel.maxLengthSpinner.value = Config.maxTrappingNetLength / 1000
            }
        }
        frame.addWindowStateListener { windowEvent ->
            // Repaint if window became unminimized
            if (windowEvent.newState == 0)
                frame.repaint()
        }
        controlsPanel.normalDistributionCb.addActionListener {
            Config.normalFliesDistribution = !Config.normalFliesDistribution
            frame.repaint()
        }
        controlsPanel.dynamicFliesCb.addActionListener { Config.dynamicFlies = controlsPanel.dynamicFliesCb.isSelected }
        controlsPanel.resetBtn.addActionListener {
            resetWeb()
        }
    }

    private fun reproduceWeb() {
        val children = web.reproduce()
        Collections.sort(children, Collections.reverseOrder<Any>())
        web = children[0]
    }


    private fun resetWeb() {
        web = Web.generate()
        updateStatusBarText()
        controlsPanel.reproduceBtn.isEnabled = true
        frame.repaint()
    }

    private fun updateStatusBarText() {
        val generation = web.generation
        val length = web.trappingNet.length

        var efficiency = web.efficiency.toString()
        if (efficiency.length > 5)
            efficiency = efficiency.substring(0, 5)

        statusLabel.text = "Generation: $generation. Efficiency: $efficiency. Length: $length."
    }

    private fun setStatusBarWorkingText(percentsDone: Int) {
        statusLabel.text = "Working: $percentsDone%"
    }

    private fun addComponentsToPane(pane: Container) {
        pane.add(controlsPanel.panel, BorderLayout.EAST)
        pane.add(webPanel, BorderLayout.CENTER)
        pane.add(statusPanel, BorderLayout.SOUTH)
    }

}

fun main(args: Array<String>) {
    javax.swing.SwingUtilities.invokeLater {
        try {
            javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

        } catch (e: Exception) {
            System.err.print(e.toString())
        }
        val web = Web.generate()
        MainWindow(web)
    }
}
