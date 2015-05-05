import web.Web;
import web.WebPanel;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

public class Runner {
    private JFrame frame;
    private final JPanel controlsPanel = new JPanel();


    private JPanel statusPanel = new JPanel();
    private JLabel statusLabel = new JLabel("Ready");

    private final WebPanel webPanel = new WebPanel();

    private final JButton btnGenerate = new JButton("Generate");

    private final JCheckBox cbDrawFlies = new JCheckBox("Draw flies", false);
    private final JCheckBox cbNormalDistribution = new JCheckBox("Normal distribution", true);

    private final JSpinner fliesCountSpinner = new JSpinner();
    private final JLabel fliesCountLabel = new JLabel("Flies:");

    private final JSpinner sidesCountSpinner = new JSpinner();
    private final JLabel sidesCountLabel = new JLabel("Sides:");

    private final JButton btnReproduce = new JButton("Reproduce");
    private final JSpinner reproduceStepSpinner = new JSpinner();

    private final JLabel maxLengthLabel = new JLabel("Length:");
    private final JSpinner maxLengthSpinner = new JSpinner();

    private Runner() {
        createAndShowUI();
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    System.err.print(e.toString());
                }
                new Runner();
            }
        });
    }

    private void createAndShowUI() {
        setUpFrame();
        setUpControlsPanel();
        addListeners();
        addComponentsToPane(frame.getContentPane());
        frame.pack();
        frame.setVisible(true);
    }

    private void setUpControlsPanel() {
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.X_AXIS));
        controlsPanel.add(btnGenerate);
        controlsPanel.add(btnReproduce);
        controlsPanel.add(reproduceStepSpinner);
        controlsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        controlsPanel.add(sidesCountLabel);
        controlsPanel.add(sidesCountSpinner);
        controlsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        controlsPanel.add(fliesCountLabel);
        controlsPanel.add(fliesCountSpinner);
        controlsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        controlsPanel.add(maxLengthLabel);
        controlsPanel.add(maxLengthSpinner);
        controlsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        controlsPanel.add(cbNormalDistribution);
        controlsPanel.add(cbDrawFlies);


        sidesCountSpinner.setValue(Web.getSidesCount());
        sidesCountSpinner.setToolTipText("Web sides count");

        fliesCountSpinner.setValue(Web.getFliesCount() / 10);
        fliesCountSpinner.setToolTipText("Flies (x10)");

        maxLengthSpinner.setToolTipText("Max trapping net length (x1000)");
        maxLengthSpinner.setValue(Web.getMaxTrappingNetLength() / 1000);

        reproduceStepSpinner.setToolTipText("Number of generations for reproducing");
        reproduceStepSpinner.setValue(1);
        reproduceStepSpinner.setPreferredSize(new Dimension(50, 0));
    }

    private void setUpFrame() {
        frame = new JFrame("Web evolution");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusPanel.setPreferredSize(new Dimension(frame.getWidth(), 20));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        statusPanel.add(statusLabel);
    }

    private void addListeners() {
        btnGenerate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    Web.setSidesCount((Integer) sidesCountSpinner.getValue());
                } catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(frame, e.getMessage());
                }
                webPanel.resetWeb();
                updateStatusBarText();
                btnReproduce.setEnabled(true);
                frame.repaint();
            }
        });
        cbDrawFlies.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                webPanel.toggleDrawFlies();
                frame.repaint();
            }
        });
        sidesCountSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                Integer value = (Integer) sidesCountSpinner.getValue();
                try {
                    Web.setSidesCount(value);
                    btnReproduce.setEnabled(false);
                } catch (IllegalArgumentException e) {
                    sidesCountSpinner.setValue(Web.getSidesCount());
                }
            }
        });
        fliesCountSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                Integer value = (Integer) fliesCountSpinner.getValue();
                try {
                    Web.setFliesCount(10 * value);
                    btnReproduce.setEnabled(false);
                } catch (IllegalArgumentException e) {
                    fliesCountSpinner.setValue(Web.getFliesCount() / 10);
                }
            }
        });
        reproduceStepSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                Integer value = (Integer) reproduceStepSpinner.getValue();
                if(value < 1)
                    reproduceStepSpinner.setValue(1);
            }
        });
        btnReproduce.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new Thread() {
                    @Override
                    public void run() {
                        int totalIterations = (Integer) reproduceStepSpinner.getValue();
                        int updateProgressStep = totalIterations / 100 - 1;
                        updateProgressStep = updateProgressStep < 1 ? 1 : updateProgressStep;
                        for (int i = 0; i < totalIterations; i++) {
                            webPanel.reproduceWeb();

                            if (totalIterations >= 50 && i % updateProgressStep == 0)
                                setStatusBarWorkingText(100 * i / totalIterations);
                        }
                        updateStatusBarText();
                        frame.repaint();
                    }
                }.start();
            }
        });
        maxLengthSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                Integer value = (Integer) maxLengthSpinner.getValue();
                try {
                    Web.setMaxTrappingNetLength(1000 * value);
                    btnReproduce.setEnabled(false);
                } catch (IllegalArgumentException e) {
                    maxLengthSpinner.setValue(Web.getMaxTrappingNetLength() / 1000);
                }
            }
        });
        frame.addWindowStateListener(new WindowStateListener() {
            @Override
            public void windowStateChanged(WindowEvent windowEvent) {
                // Repaint if window became unminimized
                if (windowEvent.getNewState() == 0)
                    frame.repaint();
            }
        });
        cbNormalDistribution.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Web.normalFliesDistribution = !Web.normalFliesDistribution;
                frame.repaint();
            }
        });
    }

    private void updateStatusBarText() {
        String generation = String.valueOf((int) webPanel.getGeneration());
        String efficiency = String.valueOf(webPanel.getWebEfficiency());
        if(efficiency.length() > 5)
            efficiency = efficiency.substring(0, 5);
        String length = String.valueOf(webPanel.getTrappingNetLength());
        statusLabel.setText("Generation: " + generation + ". Efficiency: " + efficiency + ". Length: " + length + ".");
    }

    private void setStatusBarWorkingText(int percentsDone) {
        String text = "Working: " + String.valueOf(percentsDone) + "%";
        statusLabel.setText(text);
    }

    private void addComponentsToPane(Container pane) {
        pane.add(controlsPanel, BorderLayout.PAGE_START);
        pane.add(webPanel, BorderLayout.CENTER);
        pane.add(statusPanel, BorderLayout.SOUTH);
    }
}