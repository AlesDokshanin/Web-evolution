import web_kotlin.WebConfig
import web_kotlin.WebPanel

import java.awt.BorderLayout
import java.awt.Container
import java.awt.Dimension
import javax.swing.*
import javax.swing.border.BevelBorder

class MainWindow constructor() {
    private var frame: JFrame? = null
    private val controlsPanel = JPanel()

    private val statusPanel = JPanel()
    private val statusLabel = JLabel("Ready")

    private val webPanel = WebPanel()

    private val cbDrawFlies = JCheckBox("Draw flies", false)
    private val cbNormalDistribution = JCheckBox("Normal distribution", true)
    private val cbDynamicFlies = JCheckBox("Dynamic flies", false)

    private val fliesCountSpinner = JSpinner()
    private val fliesCountLabel = JLabel("Flies:")

    private val sidesCountSpinner = JSpinner()
    private val sidesCountLabel = JLabel("Sides:")

    private val btnReproduce = JButton("Reproduce")
    private val reproduceStepSpinner = JSpinner()

    private val maxLengthLabel = JLabel("Length:")
    private val maxLengthSpinner = JSpinner()

    init {
        createAndShowUI()
    }

    private fun createAndShowUI() {
        setUpFrame()
        setUpControlsPanel()
        addListeners()
        addComponentsToPane(frame!!.contentPane)
        frame!!.pack()
        frame!!.isVisible = true
    }

    private fun setUpControlsPanel() {
        controlsPanel.layout = BoxLayout(controlsPanel, BoxLayout.X_AXIS)
        controlsPanel.add(btnReproduce)
        controlsPanel.add(reproduceStepSpinner)
        controlsPanel.add(Box.createRigidArea(Dimension(5, 0)))
        controlsPanel.add(sidesCountLabel)
        controlsPanel.add(sidesCountSpinner)
        controlsPanel.add(Box.createRigidArea(Dimension(5, 0)))
        controlsPanel.add(fliesCountLabel)
        controlsPanel.add(fliesCountSpinner)
        controlsPanel.add(Box.createRigidArea(Dimension(5, 0)))
        controlsPanel.add(maxLengthLabel)
        controlsPanel.add(maxLengthSpinner)
        controlsPanel.add(Box.createRigidArea(Dimension(5, 0)))
        controlsPanel.add(cbNormalDistribution)
        controlsPanel.add(cbDrawFlies)
        controlsPanel.add(cbDynamicFlies)


        sidesCountSpinner.value = WebConfig.sidesCount
        sidesCountSpinner.toolTipText = "Web sides count"

        fliesCountSpinner.value = WebConfig.fliesCount / 10
        fliesCountSpinner.toolTipText = "Flies (x10)"

        maxLengthSpinner.toolTipText = "Max trapping net length (x1000)"
        maxLengthSpinner.value = WebConfig.maxTrappingNetLength / 1000

        reproduceStepSpinner.toolTipText = "Number of generations for reproducing"
        reproduceStepSpinner.value = 1
        reproduceStepSpinner.preferredSize = Dimension(50, 0)
    }

    private fun setUpFrame() {
        frame = JFrame("Web evolution")
        frame!!.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame!!.layout = BorderLayout()

        statusPanel.border = BevelBorder(BevelBorder.LOWERED)
        statusPanel.preferredSize = Dimension(frame!!.width, 20)
        statusPanel.layout = BoxLayout(statusPanel, BoxLayout.X_AXIS)
        statusLabel.horizontalAlignment = SwingConstants.LEFT
        statusPanel.add(statusLabel)
    }

    private fun addListeners() {
        cbDrawFlies.addActionListener {
            webPanel.toggleDrawFlies()
            frame!!.repaint()
        }
        sidesCountSpinner.addChangeListener {
            val value = sidesCountSpinner.value as Int
            try {
                WebConfig.sidesCount = value

            } catch (e: IllegalArgumentException) {
                sidesCountSpinner.value = WebConfig.sidesCount
            }

            resetWeb()
        }
        fliesCountSpinner.addChangeListener {
            val value = fliesCountSpinner.value as Int
            try {
                WebConfig.fliesCount = 10 * value
                resetWeb()
            } catch (e: IllegalArgumentException) {
                fliesCountSpinner.value = WebConfig.fliesCount / 10
            }
        }
        reproduceStepSpinner.addChangeListener {
            val value = reproduceStepSpinner.value as Int
            if (value < 1)
                reproduceStepSpinner.value = 1
        }
        btnReproduce.addActionListener {
            object : Thread() {
                override fun run() {
                    val totalIterations = reproduceStepSpinner.value as Int
                    var updateProgressStep = totalIterations / 100 - 1
                    updateProgressStep = if (updateProgressStep < 1) 1 else updateProgressStep
                    for (i in 0..totalIterations - 1) {
                        webPanel.reproduceWeb()

                        if (totalIterations >= 50 && i % updateProgressStep == 0)
                            setStatusBarWorkingText(100 * i / totalIterations)
                    }
                    updateStatusBarText()
                    frame!!.repaint()
                }
            }.start()
        }
        maxLengthSpinner.addChangeListener {
            val value = maxLengthSpinner.value as Int
            try {
                WebConfig.maxTrappingNetLength = 1000 * value
            } catch (e: IllegalArgumentException) {
                maxLengthSpinner.value = WebConfig.maxTrappingNetLength / 1000
            }
        }
        frame!!.addWindowStateListener { windowEvent ->
            // Repaint if window became unminimized
            if (windowEvent.newState == 0)
                frame!!.repaint()
        }
        cbNormalDistribution.addActionListener {
            WebConfig.normalFliesDistribution = !WebConfig.normalFliesDistribution
            frame!!.repaint()
        }
        cbDynamicFlies.addActionListener { WebConfig.dynamicFlies = cbDynamicFlies.isSelected }
    }

    private fun resetWeb() {
        webPanel.resetWeb()
        updateStatusBarText()
        btnReproduce.isEnabled = true
        frame!!.repaint()
    }

    private fun updateStatusBarText() {
        val generation = webPanel.generation.toInt().toString()
        var efficiency = webPanel.webEfficiency.toString()
        if (efficiency.length > 5)
            efficiency = efficiency.substring(0, 5)
        val length = webPanel.trappingNetLength.toString()
        statusLabel.text = "Generation: $generation. Efficiency: $efficiency. Length: $length."
    }

    private fun setStatusBarWorkingText(percentsDone: Int) {
        val text = "Working: " + percentsDone.toString() + "%"
        statusLabel.text = text
    }

    private fun addComponentsToPane(pane: Container) {
        pane.add(controlsPanel, BorderLayout.PAGE_START)
        pane.add(webPanel, BorderLayout.CENTER)
        pane.add(statusPanel, BorderLayout.SOUTH)
    }
}

fun main(args: Array<String>) {
//    javax.swing.SwingUtilities.invokeLater {
//        try {
//            javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
//        } catch (e: Exception) {
//            System.err.print(e.toString())
//        }
//    }
    MainWindow()
}
