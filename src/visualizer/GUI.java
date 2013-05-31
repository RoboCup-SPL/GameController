package visualizer;

import common.TotalScaleLayout;
import data.GameControlData;
import data.Rules;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * @author: Michel Bartsch
 * 
 * This class displays the game-state and listens to the keyboard.
 */
public class GUI extends JFrame implements KeyListener
{
    /**
     * Some constants defining this GUI`s appearance as their names say.
     * Feel free to change them and see what happens.
     */
    private static final boolean IS_OSX = System.getProperty("os.name").contains("OS X");
    private static final String WINDOW_TITLE = "GameController";
    private static final String STANDARD_FONT = "Helvetica";
    private static final String TEST_FONT = "Lucida Console";
    private static final String CONFIG_PATH = "config/";
    private static final String BACKGROUND = "background.png";
    private static final String ICONS_PATH = "config/icons/";
    
    /** All the components of this GUI. */
    private ImagePanel background;
    private JTextArea testDisplayLeft;
    private JTextArea testDisplayRight;
    
    /** If a key is currently pressed. */
    private boolean pressing = false;
    /** If testmode is on to just display whole GameControlData. */
    private boolean testmode = false;
    
    /**
     * Creates a new GUI.
     */
    GUI()
    {
        super(WINDOW_TITLE);
        addKeyListener(this);
        setUndecorated(true);
        GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        devices[devices.length-1].setFullScreenWindow(this);
        
        background = new ImagePanel((new ImageIcon(CONFIG_PATH+Rules.league.leagueDirectory+"/"+BACKGROUND)).getImage(), true);
        testDisplayLeft = new JTextArea();
        testDisplayRight = new JTextArea();
        Font testDisplayFont = new Font(TEST_FONT, Font.PLAIN, 14);
        testDisplayLeft.setFont(testDisplayFont);
        testDisplayRight.setFont(testDisplayFont);
        
        //--layout--
        TotalScaleLayout layout = new TotalScaleLayout(this);
        setLayout(layout);
        layout.add(0, 0, 1, 1, background);
        layout.add(0.4, 0.2, 0.23, 0.7, testDisplayLeft);
        layout.add(0.65, 0.2, 0.23, 0.7, testDisplayRight);
        
        if(IS_OSX) {
            devices[devices.length-1].setFullScreenWindow(null);
            setSize(devices[devices.length-1].getDisplayMode().getWidth(), devices[devices.length-1].getDisplayMode().getHeight());
        }
        
        setVisible(true);
    }
    
    /**
     * This is called by the Listener after receiving GameCOntrolData to show
     * them on the gui.
     * 
     * @param data  The GameControlData to show.
     */
    public void update(GameControlData data)
    {
        
        if(testmode) {
            String disp = "";
            disp += data;
            for(int i=0; i<2; i++) {
                disp += data.team[i];
            }
            testDisplayLeft.setText(disp);
            disp = "";
            for(int i=0; i<2; i++) {
                for(int j=0; j<data.team[i].player.length; j++) {
                    disp += data.team[i].player[j];
                }
            }
            testDisplayRight.setText(disp);
        } else {
            testDisplayLeft.setText("");
            testDisplayRight.setText("");
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e)
    {
        if(!pressing) {
            switch(e.getKeyCode()) {
                case KeyEvent.VK_F10:
                    Main.exit();
                    break;
                case KeyEvent.VK_F11:
                    pressing = true;
                    testmode = !testmode;
                    if(testmode) {
                        testDisplayLeft.setText("waiting for package...");
                    } else {
                        testDisplayLeft.setText("");
                        testDisplayRight.setText("");
                    }
                    break;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        pressing = false;
    }
    
    /**
     * @author: Michel Bartsch
     * 
     * This is a normal JPanel, but it has a background image.
     */
    class ImagePanel extends JPanel
    {
        /** The image that is shown in the background. */
        private Image image;
        /** If true, the Image will be displayed at the top and not vertical centered. */
        private boolean alignTop = false;

        /**
         * Creates a new ImagePanel.
         * 
         * @param image     The Image to be shown in the background.
         * @param alignTop  If true, the Image will be displayed at the top and not vertical centered.
         */
        public ImagePanel(Image image, boolean alignTop)
        {
            this.image = image;
            this.alignTop = alignTop;
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
        public void paintComponent(Graphics g)
        {
            if(super.isOpaque()) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            float scaleFactor;
            if(image.getWidth(null) > image.getHeight(null)) {
                scaleFactor = (float)getWidth()/image.getWidth(null);
            } else {
                scaleFactor = (float)getHeight()/image.getHeight(null);
            }
            int imageWidth = (int)(scaleFactor*image.getWidth(null));
            int imageHeight = (int)(scaleFactor*image.getHeight(null));
            int offsetHorizontal = (int)((getWidth()-imageWidth)/2);;
            int offsetVetical = 0;
            if(!alignTop) {
                offsetVetical = (int)((getHeight()-imageHeight)/2);
            }
            g.drawImage(image, offsetHorizontal, offsetVetical, imageWidth, imageHeight, null);
        }
    }
}