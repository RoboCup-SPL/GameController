package visualizer;

import common.TotalScaleLayout;
import data.GameControlData;
import data.Rules;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * @author: Michel Bartsch
 * 
 * This class displays the game-state
 */
public class GUI extends JFrame
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
    private final static String WAITING_FOR_PACKAGE = "waiting for package...";
    
    /** All the components of this GUI. */
    private ImagePanel background;
    private JTextArea testDisplayMain;
    private JTextArea testDisplayRobotsLeft;
    private JTextArea testDisplayRobotsRight;
    private JTextArea state;
    
    /** If testmode is on to just display whole GameControlData. */
    private boolean testmode = false;
    /** The current data to show. */
    private GameControlData data;
    
    /**
     * Creates a new GUI.
     */
    GUI()
    {
        super(WINDOW_TITLE);
        setUndecorated(true);
        GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        devices[devices.length-1].setFullScreenWindow(this);
        
        background = new ImagePanel((new ImageIcon(CONFIG_PATH+Rules.league.leagueDirectory+"/"+BACKGROUND)).getImage(), true);
        testDisplayMain = new JTextArea();
        testDisplayRobotsLeft = new JTextArea();
        testDisplayRobotsRight = new JTextArea();
        Font testDisplayFont = new Font(TEST_FONT, Font.PLAIN, 14);
        testDisplayMain.setFont(testDisplayFont);
        testDisplayRobotsLeft.setFont(testDisplayFont);
        testDisplayRobotsRight.setFont(testDisplayFont);
        testDisplayMain.setFocusable(false);
        testDisplayRobotsLeft.setFocusable(false);
        testDisplayRobotsRight.setFocusable(false);
        
        state = new JTextArea();
        
        //--layout--
        TotalScaleLayout layout = new TotalScaleLayout(this);
        setLayout(layout);
        layout.add(0, 0, 1, 1, background);
        layout.add(0.2, 0.3, 0.2, 0.6, testDisplayMain);
        layout.add(0.425, 0.2, 0.2, 0.7, testDisplayRobotsLeft);
        layout.add(0.65, 0.2, 0.2, 0.7, testDisplayRobotsRight);
        layout.add(0.4, 0.7, 0.2, 0.1, state);
        
        if(IS_OSX) {
            devices[devices.length-1].setFullScreenWindow(null);
            setSize(devices[devices.length-1].getDisplayMode().getWidth(), devices[devices.length-1].getDisplayMode().getHeight());
        }
        
        setVisible(true);
        
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                testDisplayMain.setText(WAITING_FOR_PACKAGE);
            }
        } );
    }
    
    /**
     * This toggles the visualizer´s testmode on and off.
     */
    public void toggleTestmode()
    {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                testmode = !testmode;
                testDisplayMain.setText(WAITING_FOR_PACKAGE);
                testDisplayRobotsLeft.setText("");
                testDisplayRobotsRight.setText("");
                state.setText("");
                //debug
                update(new GameControlData());
            }
        } );
    }
    
    /**
     * This is called by the Listener after receiving GameControlData to show
     * them on the gui.
     * 
     * @param data  The GameControlData to show.
     */
    public void update(GameControlData newData)
    {
        data = newData;
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if(testmode) {
                    String disp = "";
                    disp += data;
                    for(int i=0; i<2; i++) {
                        disp += data.team[i];
                    }
                    testDisplayMain.setText(disp);
                    disp = "";
                    for(int j=0; j<data.team[0].player.length; j++) {
                        disp += data.team[0].player[j];
                    }
                    testDisplayRobotsLeft.setText(disp);
                    disp = "";
                    for(int j=0; j<data.team[1].player.length; j++) {
                        disp += data.team[1].player[j];
                    }
                    testDisplayRobotsRight.setText(disp);
                    state.setText("");
                } else {
                    testDisplayMain.setText("");
                    testDisplayRobotsLeft.setText("");
                    testDisplayRobotsRight.setText("");
                    String temp;
                    switch(data.gameState) {
                        case GameControlData.STATE_INITIAL:  temp = "initial"; break;
                        case GameControlData.STATE_READY:    temp = "ready";   break;
                        case GameControlData.STATE_SET:      temp = "set";     break;
                        case GameControlData.STATE_PLAYING:  temp = "playing"; break;
                        case GameControlData.STATE_FINISHED: temp = "finish";  break;
                        default: temp = "undefinied("+data.gameState+")";
                    }
                    state.setText(temp);
                }
            }
        } );
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