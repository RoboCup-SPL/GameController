package controller.ui.setup;

import javax.swing.*;
import java.awt.*;

/**
 * @author Michel Bartsch
 *
 * This is a normal JPanel, but it has a background image.
 */
class ImagePanel extends JPanel
{
    private static final long serialVersionUID = 1L;

    /** The image that is shown in the background. */
    private Image image;

    /**
     * Creates a new ImagePanel.
     *
     * @param image     The Image to be shown in the background.
     */
    public ImagePanel(Image image)
    {
        this.image = image;
    }

    public ImagePanel()
    {
        this.image = null;
    }

    public void setVisible(){

    }

    /**
     * Changes the background image.
     *
     * @param image     Changes the image to this one.
     */
    public void setImage(Image image)
    {
        this.image = image;
    }

    /**
     * Paints this Component, should be called automatically.
     *
     * @param g     This components graphical content.
     */
    @Override
    public void paintComponent(Graphics g) {
        if (image != null) {
            if (super.isOpaque()) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            g.drawImage(image, (getWidth() - image.getWidth(null)) / 2, 0, image.getWidth(null), image.getHeight(null), null);
        }
    }
}