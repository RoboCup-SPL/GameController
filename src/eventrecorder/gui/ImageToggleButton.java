package eventrecorder.gui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

@SuppressWarnings("serial")
public class ImageToggleButton extends JToggleButton{
	private ImageIcon enabledIcon;
	private ImageIcon disabledIcon;
	private boolean isActivated;
	
	public ImageToggleButton(String tooltip, String path, int width, int height){
		enabledIcon = new ImageIcon(path);
		enabledIcon.setImage(enabledIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
		setIcon(enabledIcon);
		setToolTipText(tooltip);
		setFocusPainted(false);
	}
	
	public ImageToggleButton(String tooltip, String path, String disabledPath, boolean activated, int width, int height){
		enabledIcon = new ImageIcon(path);
		enabledIcon.setImage(enabledIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
		disabledIcon = new ImageIcon(disabledPath);
		disabledIcon.setImage(disabledIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
		setFocusPainted(false);
		
		setActivated(activated);
		
		setToolTipText(tooltip);
		
		addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                setActivated(!isActivated);
            }
		    
		});
	}
	
	public void setActivated(boolean b) {
	    setSelected(b);
		setIcon(!b && disabledIcon != null ? disabledIcon : enabledIcon);
		isActivated = b;
	}
	
	public boolean isActivated(){
	    return isActivated;
	}

}
