import javax.swing.*;


public class ControlsPanel {
    JPanel panel;
    JSpinner reproduceGenerationsSpinner;
    JLabel reproduceGenerationsLabel;
    JSpinner sidesCountSpinner;
    JLabel sidesCountLabel;
    JLabel fliesCountLabel;
    JSpinner fliesCountSpinner;
    JLabel maxLengthLabel;
    JSpinner maxLengthSpinner;
    JPanel spinnerPanel;
    JCheckBox normalDistributionCb;
    JCheckBox drawFliesCb;
    JCheckBox dynamicFliesCb;
    JPanel checkBoxPanel;
    JPanel buttonPanel;
    JButton reproduceBtn;
    JButton resetBtn;

    private final JComponent[] controls = {reproduceGenerationsSpinner, sidesCountSpinner, fliesCountSpinner, maxLengthSpinner,
            drawFliesCb, dynamicFliesCb, normalDistributionCb, reproduceBtn, resetBtn};

    void lockControls() {
        for(JComponent c: controls)
            c.setEnabled(false);
    }

    void unlockControls() {
        for(JComponent c: controls)
            c.setEnabled(true);
    }
}
