package eventrecorder.gui;

import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * A JButton with an ImageIcon.
 *
 * @author Andre Muehlenbrock
 */
public class ImageButton extends JButton{
    private static final long serialVersionUID = -2848062138312840891L;
    final ImageIcon enabledIcon;
    ImageIcon disabledIcon;

    public ImageButton(String tooltip, String path, int width, int height){
        enabledIcon = new ImageIcon(path);

        enabledIcon.setImage(enabledIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        setIcon(enabledIcon);
        setToolTipText(tooltip);
        setFocusPainted(false);
    }

    public ImageButton(String tooltip, String path, String disabledPath, boolean enabled, int width, int height){

        enabledIcon = new ImageIcon(path);
        disabledIcon = new ImageIcon(disabledPath);

        enabledIcon.setImage(enabledIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
        disabledIcon.setImage(disabledIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));

        setFocusPainted(false);

        setEnabled(enabled);

        setToolTipText(tooltip);

    }

    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);

        setIcon(!b && disabledIcon != null ? disabledIcon : enabledIcon);

    }
}
