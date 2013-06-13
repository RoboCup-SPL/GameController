package visualizer;

import common.Log;
import data.GameControlData;
import data.Rules;
import data.Teams;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

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
    private static final double STANDARD_FONT_SIZE = 0.06;
    private static final double STANDARD_FONT_XXL_SIZE = 0.16;
    private static final String TEST_FONT = "Lucida Console";
    private static final double TEST_FONT_SIZE = 0.01;
    private static final String CONFIG_PATH = "config/";
    private static final String BACKGROUND = "background.png";
    private static final String ICONS_PATH = "config/icons/";
    private final static String WAITING_FOR_PACKAGE = "waiting for package...";
    
    
    BufferStrategy bufferStrategy;
    /** If testmode is on to just display whole GameControlData. */
    private boolean testmode = false;
    /** The current data to show. */
    private GameControlData data;
    
    private BufferedImage background;
    
    private Font testFont;
    private Font standardFont;
    private Font scoreFont;
    
    /**
     * Creates a new GUI.
     */
    GUI()
    {
        super(WINDOW_TITLE);
        
        setUndecorated(true);
        GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        devices[devices.length-1].setFullScreenWindow(this);
        if(IS_OSX) {
            devices[devices.length-1].setFullScreenWindow(null);
            setSize(devices[devices.length-1].getDisplayMode().getWidth(), devices[devices.length-1].getDisplayMode().getHeight());
        }
        createBufferStrategy(2);
        bufferStrategy = getBufferStrategy();
        
        try {
            background = ImageIO.read(new File(CONFIG_PATH+Rules.league.leagueDirectory+"/"+BACKGROUND));
        } catch(IOException e) {
            Log.error("Unable to load background image");
        }
        float scaleFactor;
        if(background.getWidth() > background.getHeight()) {
            scaleFactor = (float)getWidth()/background.getWidth();
        } else {
            scaleFactor = (float)getHeight()/background.getHeight();
        }
        Image tmp = (new ImageIcon(background).getImage()).getScaledInstance(
                (int)(background.getWidth()*scaleFactor),
                (int)(background.getHeight()*scaleFactor),
                Image.SCALE_DEFAULT);
        background = new BufferedImage(background.getWidth(), background.getWidth(), BufferedImage.TYPE_INT_ARGB);
        background.getGraphics().drawImage(tmp, 0, 0, null);
        
        testFont = new Font(TEST_FONT, Font.PLAIN, (int)(TEST_FONT_SIZE*getWidth()));
        standardFont = new Font(STANDARD_FONT, Font.PLAIN, (int)(STANDARD_FONT_SIZE*getWidth()));
        scoreFont = new Font(STANDARD_FONT, Font.PLAIN, (int)(STANDARD_FONT_XXL_SIZE*getWidth()));
        
        setVisible(true);
        Graphics g = bufferStrategy.getDrawGraphics();
        draw(g);
        bufferStrategy.show();
        g.dispose();
    }
    
    /**
     * This toggles the visualizer´s testmode on and off.
     */
    public void toggleTestmode()
    {
        testmode = !testmode;
    }
    
    /**
     * This is called by the Listener after receiving GameControlData to show
     * them on the gui.
     * 
     * @param data  The GameControlData to show.
     */
    public void update(GameControlData data)
    {
        this.data = data;
        Graphics g = bufferStrategy.getDrawGraphics();
        if (!bufferStrategy.contentsLost()) {
            draw(g);
            bufferStrategy.show();
            g.dispose();
        }
    }
    
    public final void draw(Graphics g)
    {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.drawImage(background, 0, 0, null);
        
        if(data == null) {
            drawNoPackage(g);
        } else if(testmode) {
            drawTestmode(g);
        } else {
            drawIcons(g);
            drawScores(g);
        }
    }
    
    private void drawNoPackage(Graphics g)
    {
        g.setColor(Color.BLACK);
        g.setFont(testFont);
        g.drawString(WAITING_FOR_PACKAGE, (int)(0.2*getWidth()), (int)(0.3*getHeight()));
    }
    
    private void drawTestmode(Graphics g)
    {
        g.setColor(Color.BLACK);
        g.setFont(testFont);
        int x = getRealtiveSize(0.08);
        int y = (int)(0.3*getHeight());
        String[] out = data.toString().split("\n");
        for(int i=0; i<out.length; i++) {
            g.drawString(out[i], x, y);
            y += testFont.getSize()*1.2;
        }
        for(int j=0; j<2; j++) {
            out = data.team[j].toString().split("\n");
            for(int i=0; i<out.length; i++) {
                g.drawString(out[i], x, y);
                y += testFont.getSize()*1.2;
            }
        }
        
        x = (int)(0.35*getWidth());
        for(int i=0; i<2; i++) {
            y = (int)(0.2*getHeight());
            for(int j=0; j<data.team[i].player.length; j++) {
                out = data.team[i].player[j].toString().split("\n");
                for(int k=0; k<out.length; k++) {
                    g.drawString(out[k], x, y);
                    y += testFont.getSize()*1.2;
                }
            }
            x = getRealtiveSize(0.64);
        }
    }

    private void drawIcons(Graphics g)
    {
        int x = getRealtiveSize(0.05);
        int y = getRealtiveSize(0.18);
        int size = getRealtiveSize(0.24);
        BufferedImage[] icons = new BufferedImage[] {
            Teams.getIcon(data.team[0].teamNumber),
            Teams.getIcon(data.team[1].teamNumber)};
        for(int i=0; i<2; i++) {
            float scaleFactorX = 1;
            float scaleFactorY = 1;
            if(icons[i].getWidth() > icons[i].getHeight()) {
                scaleFactorY = icons[i].getHeight()/(float)icons[i].getWidth();
            } else {
                scaleFactorX = icons[i].getWidth()/(float)icons[i].getHeight();
            }
            int offsetX = (int)((size - size*scaleFactorX)/2);
            int offsetY = (int)((size - size*scaleFactorY)/2);
            g.drawImage(icons[i],
                    (i==1 ? x : getWidth()-x-size) + offsetX,
                    y+offsetY,
                    (int)(scaleFactorX*size),
                    (int)(scaleFactorY*size), null);
        }
    }
    
    private void drawScores(Graphics g)
    {
        g.setFont(scoreFont);
        int x = getRealtiveSize(0.34);
        int y = getRealtiveSize(0.36);
        int yDiv = getRealtiveSize(0.35);
        int size = getRealtiveSize(0.12);
        g.setColor(Color.BLACK);
        drawCenteredString(g, ":", getWidth()/2-size, yDiv, 2*size);
        for(int i=0; i<2; i++) {
            g.setColor(Rules.league.teamColor[data.team[i].teamColor]);
            drawCenteredString(
                    g,
                    data.team[i].score+"",
                    i==1 ? x : getWidth()-x-size,
                    y,
                    size);
        }
    }
    
    private int getRealtiveSize(double size)
    {
        return (int)(size*getWidth());
    }
    
    private void drawCenteredString(Graphics g, String s, int x, int y, int width)
    {
        int offset = (width - g.getFontMetrics().stringWidth(s)) / 2;
        g.drawString(s, x+offset, y);
    }
}