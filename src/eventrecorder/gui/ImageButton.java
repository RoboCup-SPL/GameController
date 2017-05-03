package eventrecorder.gui;

import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JButton;

@SuppressWarnings("serial")
public class ImageButton extends JButton{
	ImageIcon enabledIcon;
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
