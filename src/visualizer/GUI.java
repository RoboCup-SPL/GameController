package visualizer;

import common.Log;
import data.GameControlData;
import data.Rules;
import data.Teams;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    private static final double STANDARD_FONT_S_SIZE = 0.04;
    private static final String TEST_FONT = "Lucida Console";
    private static final double TEST_FONT_SIZE = 0.01;
    private static final String CONFIG_PATH = "config/";
    private static final String BACKGROUND = "background";
    private static final String WAITING_FOR_PACKAGE = "waiting for package...";

    /** Available screens. */
    private static final GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

    BufferStrategy bufferStrategy;
    /** If testmode is on to just display whole GameControlData. */
    private boolean testmode = false;
    /** The current data to show. */
    private GameControlData data;
    
    private BufferedImage background;
    
    private Font testFont;
    private Font standardFont;
    private Font standardSmalFont;
    private Font scoreFont;
    private SimpleDateFormat clockFormat = new SimpleDateFormat("mm:ss");
    
    /**
     * Creates a new GUI.
     */
    GUI()
    {
        super(WINDOW_TITLE, devices[devices.length - 1].getDefaultConfiguration());
        
        setUndecorated(true);
        if(IS_OSX && devices.length != 1) {
            setSize(devices[devices.length-1].getDefaultConfiguration().getBounds().getSize());
        } else {
            devices[devices.length-1].setFullScreenWindow(this);
        }

        for (String format : new String [] {".png", ".jpeg", ".jpg"}) {
            try {
                background = ImageIO.read(new File(CONFIG_PATH+Rules.league.leagueDirectory+"/"+BACKGROUND+format));
            } catch(IOException e) {
            }
        }
        if(background == null) {
            Log.error("Unable to load background image");
        }
        float scaleFactor = (float)getWidth()/background.getWidth();
        Image tmp = (new ImageIcon(background).getImage()).getScaledInstance(
                (int)(background.getWidth()*scaleFactor),
                (int)(background.getHeight()*scaleFactor),
                Image.SCALE_DEFAULT);
        background = new BufferedImage((int) (background.getWidth() * scaleFactor), (int) (background.getWidth() * scaleFactor), BufferedImage.TYPE_INT_ARGB);
        background.getGraphics().drawImage(tmp, 0, 0, null);
        
        testFont = new Font(TEST_FONT, Font.PLAIN, (int)(TEST_FONT_SIZE*getWidth()));
        standardFont = new Font(STANDARD_FONT, Font.PLAIN, (int)(STANDARD_FONT_SIZE*getWidth()));
        standardSmalFont = new Font(STANDARD_FONT, Font.PLAIN, (int)(STANDARD_FONT_S_SIZE*getWidth()));
        scoreFont = new Font(STANDARD_FONT, Font.PLAIN, (int)(STANDARD_FONT_XXL_SIZE*getWidth()));
        
        setVisible(true);
        createBufferStrategy(2);
        bufferStrategy = getBufferStrategy();
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
        update(data);
    }
    
    /**
     * This is called by the Listener after receiving GameControlData to show
     * them on the gui.
     * 
     * @param data  The GameControlData to show.
     */
    public synchronized void update(GameControlData data)
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
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.drawImage(background, 0, 0, null);
        
        if(data == null) {
            drawNoPackage(g);
        } else if(testmode) {
            drawTestmode(g);
        } else {
            drawTeams(g);
            drawScores(g);
            drawTime(g);
            drawSecState(g);
            drawState(g);
            drawSubTime(g);
            drawPenaltyInfo(g);
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
        int x = getRelativeSize(0.08);
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
            x = getRelativeSize(0.64);
        }
    }

    private void drawTeams(Graphics g)
    {
        int x = getRelativeSize(0.02);
        int y = (int)(0.30*getHeight());
        int size = getRelativeSize(0.27);
        int yName = (int)(y + size * 1.2);
        BufferedImage[] icons = new BufferedImage[] {
            Teams.getIcon(data.team[0].teamNumber),
            Teams.getIcon(data.team[1].teamNumber)};
        g.setFont(standardSmalFont);
        int fontSize = g.getFont().getSize();
        boolean fittingSize = false;
        while(!fittingSize) {
            fittingSize = true;
            for(int i=0; i<2; i++) {
                if(g.getFontMetrics().stringWidth(Teams.getNames(false)[data.team[i].teamNumber]) > size) {
                    fittingSize = false;
                    g.setFont(g.getFont().deriveFont(Font.PLAIN, --fontSize));
                }
            }
        }
        for(int i=0; i<2; i++) {
            g.setColor(Rules.league.teamColor[data.team[i].teamColor]);
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
            drawCenteredString(g, Teams.getNames(false)[data.team[i].teamNumber],
                    (i==1 ? x : getWidth()-x-size) + offsetX,
                    yName,
                    size);
        }
    }
    
    private void drawScores(Graphics g)
    {
        g.setFont(scoreFont);
        int x = getRelativeSize(0.34);
        int y = (int)(0.62*getHeight());
        int yDiv = (int)(0.62*getHeight());
        int size = getRelativeSize(0.12);
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
    
    private void drawTime(Graphics g)
    {
        g.setColor(Color.BLACK);
        g.setFont(standardFont);
        int x = getRelativeSize(0.4);
        int y = (int)(0.37*getHeight());
        int size = getRelativeSize(0.2);
        drawCenteredString(g, formatTime(data.secsRemaining), x, y, size);
    }
    
    private void drawSecState(Graphics g)
    {
        g.setColor(Color.BLACK);
        g.setFont(standardSmalFont);
        int x = getRelativeSize(0.4);
        int y = (int)(0.74*getHeight());
        int size = getRelativeSize(0.2);
        String state;
        switch(data.secGameState) {
            case GameControlData.STATE2_NORMAL:
                if(data.firstHalf == GameControlData.C_TRUE) {
                    state = "First Half";
                } else {
                    state = "Second Half";
                }
                break;
            case GameControlData.STATE2_OVERTIME:     state = "Overtime";   break;
            case GameControlData.STATE2_PENALTYSHOOT: state = "Penalty Shoot";     break;
            default: state = "";
        }
        drawCenteredString(g, state, x, y, size);
    }
    
    private void drawState(Graphics g)
    {
        g.setColor(Color.BLACK);
        g.setFont(standardSmalFont);
        int x = getRelativeSize(0.4);
        int y = (int)(0.84*getHeight());
        int size = getRelativeSize(0.2);
        String state;
        switch(data.gameState) {
            case GameControlData.STATE_INITIAL:  state = "Initial"; break;
            case GameControlData.STATE_READY:    state = "Ready";   break;
            case GameControlData.STATE_SET:      state = "Set";     break;
            case GameControlData.STATE_PLAYING:  state = "Playing"; break;
            case GameControlData.STATE_FINISHED: state = "Finished";  break;
            default: state = "";
        }
        drawCenteredString(g, state, x, y, size);
    }
    
    private void drawSubTime(Graphics g)
    {
        if(data.subTime == 0) {
            return;
        }
        g.setColor(Color.BLACK);
        g.setFont(standardSmalFont);
        int x = getRelativeSize(0.4);
        int y = (int)(0.94*getHeight());
        int size = getRelativeSize(0.2);
        drawCenteredString(g, formatTime(data.subTime), x, y, size);
    }
    
    private void drawPenaltyInfo(Graphics g)
    {
        g.setColor(Color.RED);
        int x = getRelativeSize(0.05);
        int y = (int)(0.88*getHeight());
        int size = getRelativeSize(0.02);
        for(int i=0; i<2; i++) {
            for(int j=0; j<data.penaltyShot[i]; j++) {
                if((data.penaltyTries[i] & (1<<j)) != 0) {
                    g.fillOval(i==1 ? x+j*2*size : getWidth()-x-(5-j)*2*size-size, y, size, size);
                } else {
                    g.drawOval(i==1 ? x+j*2*size : getWidth()-x-(5-j)*2*size-size, y, size, size);
                }
            }
        }
    }
    
    private int getRelativeSize(double size)
    {
        return (int)(size*getWidth());
    }
    
    private void drawCenteredString(Graphics g, String s, int x, int y, int width)
    {
        int offset = (width - g.getFontMetrics().stringWidth(s)) / 2;
        g.drawString(s, x+offset, y);
    }
    
    private String formatTime(int seconds) {
        return (seconds < 0 ? "-" : "") + clockFormat.format(new Date(Math.abs(seconds) * 1000));
    }
}