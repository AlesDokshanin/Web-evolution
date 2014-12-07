import web.WebPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Runner {
    private JFrame frame;
    private JPanel controlsPanel = new JPanel();
    private WebPanel webPanel = new WebPanel();
    private JButton btnGenerate = new JButton("Generate");
    private JTextField tfEfficiency = new JTextField();
    private JCheckBox cbDrawFlies = new JCheckBox("Draw flies", false);

    public Runner() {
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
        controlsPanel.add(cbDrawFlies);
        controlsPanel.add(tfEfficiency);


        tfEfficiency.setEditable(false);
        tfEfficiency.setText("Efficiency: " + Double.toString(webPanel.getWebEfficiency()));

        btnGenerate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                webPanel.resetWeb();
                tfEfficiency.setText("Efficiency: " + Double.toString(webPanel.getWebEfficiency()));
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
    }

    private void setUpFrame() {
        frame = new JFrame("Web evolution");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
    }

    private void addListeners() {

    }

    private void addComponentsToPane(Container pane) {
        pane.add(controlsPanel, BorderLayout.PAGE_START);
        pane.add(webPanel, BorderLayout.CENTER);
    }
}