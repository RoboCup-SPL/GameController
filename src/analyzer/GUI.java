package analyzer;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JFrame;

/**
 * @author: Michel Bartsch
 * 
 * This window is to be shown to select log-files
 */
public class GUI extends JFrame
{
    private final static String TITLE = "Log Analyzer";
    private final static int WINDOW_WIDTH = 600;
    private final static int WINDOW_HEIGHT = 400;
    
    public GUI()
    {
        super(TITLE);
        Dimension desktop = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((desktop.width-WINDOW_WIDTH)/2, (desktop.height-WINDOW_HEIGHT)/2);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        
        
        
        setVisible(true);
    }
}