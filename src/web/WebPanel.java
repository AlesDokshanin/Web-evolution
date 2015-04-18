package web;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class WebPanel extends JPanel {
    static final int PANEL_WIDTH = 600;
    static final int PANEL_HEIGHT = 600;
    private static final Color BG_COLOR = Color.white;
    private BufferedImage image = new BufferedImage(PANEL_WIDTH, PANEL_HEIGHT,
            BufferedImage.TYPE_INT_RGB);
    private Color color = Color.black;

    private Web web;

    public WebPanel() {
        Web.height = PANEL_HEIGHT;
        Web.width = PANEL_WIDTH;

  /*      Web bestWeb = new Web(), currWeb = null;
        boolean initNew = true;
        for (int i = 1; i < 1000; i++) {
            if(initNew) {
                currWeb = new Web();
                initNew = false;
            }
            else currWeb.build();
            if(bestWeb.getEfficiency() < currWeb.getEfficiency()) {
                bestWeb = currWeb;
                initNew = true;
            }
            System.out.print("\rIteration: " + (i+1) + ". Best result: " + bestWeb.getEfficiency());
        }
        web = bestWeb;*/

        web = new Web();


        Graphics g = image.getGraphics();
        g.setColor(BG_COLOR);
        g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        g.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
        Graphics2D g2 = (Graphics2D) g;
        web.draw(g2);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(PANEL_WIDTH, PANEL_HEIGHT);
    }

    public void drawWeb() {
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        web.draw(g);
        g.dispose();
    }

    public double getWebEfficiency() {
        return web.getEfficiency();
    }

    public void resetWeb() {
        web = new Web();
    }

    public void toggleDrawFlies() {
        Web.drawFlies = !Web.drawFlies;
    }
}
