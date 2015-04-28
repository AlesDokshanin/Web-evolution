import web.Web;
import web.WebPanel;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Runner {
    private JFrame frame;
    private final JPanel controlsPanel = new JPanel();


    private JPanel statusPanel = new JPanel();
    private JLabel statusLabel = new JLabel("Ready");

    private final WebPanel webPanel = new WebPanel();

    private final JButton btnGenerate = new JButton("Generate");

    private final JCheckBox cbDrawFlies = new JCheckBox("Draw flies", false);

    private final JSpinner fliesCountSpinner = new JSpinner();
    private final JLabel fliesCountLabel = new JLabel("Flies (x100):");

    private final JSpinner sidesCountSpinner = new JSpinner();
    private final JLabel sidesCountLabel = new JLabel("Sides:");

    private final JButton btnReproduce = new JButton("Reproduce");
    private final JLabel reproduceStepLabel = new JLabel("Step: ");
    private final JSpinner reproduceStepSpinner = new JSpinner();

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
        controlsPanel.add(reproduceStepLabel);
        controlsPanel.add(reproduceStepSpinner);
        controlsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        controlsPanel.add(sidesCountLabel);
        controlsPanel.add(sidesCountSpinner);
        controlsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        controlsPanel.add(fliesCountLabel);
        controlsPanel.add(fliesCountSpinner);
        controlsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        controlsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        controlsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        controlsPanel.add(cbDrawFlies);


        sidesCountSpinner.setValue(Web.getSidesCount());
        sidesCountSpinner.setToolTipText("Sides count:");

        fliesCountSpinner.setValue(Web.getFliesCount() / 100);
        fliesCountSpinner.setToolTipText("Flies (x100):");

        reproduceStepSpinner.setValue(1);
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
                updateWebParams();
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
                    Web.setFliesCount(100 * value);
                    btnReproduce.setEnabled(false);
                } catch (IllegalArgumentException e) {
                    fliesCountSpinner.setValue(Web.getFliesCount() / 100);
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
                for (int i = 0; i < (Integer) reproduceStepSpinner.getValue(); i++) {
                    webPanel.reproduceWeb();
                    updateWebParams();
                }
                frame.repaint();
            }
        });
    }

    private void updateWebParams() {
        String generation = String.valueOf((int) webPanel.getGeneration());
        String efficiency = String.valueOf(webPanel.getWebEfficiency());
        if(efficiency.length() > 5)
            efficiency = efficiency.substring(0, 5);
        String length = String.valueOf(webPanel.getTrappingNetLength());
        statusLabel.setText("Generation: " + generation + ". Efficiency: " + efficiency + ". Length: " + length + ".");
    }

    private void addComponentsToPane(Container pane) {
        pane.add(controlsPanel, BorderLayout.PAGE_START);
        pane.add(webPanel, BorderLayout.CENTER);
        pane.add(statusPanel, BorderLayout.SOUTH);
    }

}