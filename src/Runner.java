import web.WebPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Runner {
    private JFrame frame;
    private JPanel controlsPanel = new JPanel();
    private WebPanel webPanel = new WebPanel();
    private JButton btnReset = new JButton("Reset");

    public static void main(String[] args)
    {
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
                catch(Exception e) { System.err.print(e.toString()); }
                new Runner();
            }
        });
    }

    public Runner() {
        createAndShowUI();

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
        controlsPanel.add(btnReset);
        btnReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                webPanel.resetWeb();
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