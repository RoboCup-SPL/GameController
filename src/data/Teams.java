package data;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.imageio.ImageIO;


/**
 * @author: Michel Bartsch
 * 
 * This class provides the icons and names including unique teamNumbers of all
 * teams written in the config file.
 * 
 * This class is a sigleton!
 */
public class Teams
{
    /** The path to the config file and icons. */
    private static final String PATH = "config/spl/";
    /** The name of the config file. */
    private static final String CONFIG = "teams.cfg";
    /** The charset to read the config file. */
    private final static String CHARSET = "UTF-8";
    /**
     * The possible file-endings icons may have.
     * The full name of an icon must be "<teamNumber>.<png|gif>", for example
     * "7.png".
     */
    private static final String[] PIC_ENDING = {"png", "gif"};
    
    /** The instance of the singleton. */
    private static Teams instance = new Teams();
    
    /** The names read from the config file. */
    private String[] names;
    /**
     * The icons read.
     * Note, that not all icons are read from the start but just when you ask
     * for them.
     */
    private BufferedImage[] icons;
    
    
    /**
     * Creates a new Teams.
     */
    private Teams()
    {
        int value;
        int maxValue = 0;
        BufferedReader br = null;
        try {
            InputStream inStream = new FileInputStream(PATH+CONFIG);
            br = new BufferedReader(
                    new InputStreamReader(inStream, CHARSET));
            String line;
            while((line = br.readLine()) != null) {
                value = Integer.valueOf(line.split("=")[0]);
                if(value > maxValue) {
                    maxValue = value;
                }
            }
        } catch(IOException e) {
            System.out.println("cannot load "+PATH+CONFIG);
        }
        finally {
            if(br != null) {
                try {
                    br.close();
                } catch(Exception e) {}
            }
        }
        names = new String[maxValue+1];
        icons = new BufferedImage[maxValue+1];
    }
    
    /**
     * Reads the names of all teams in the config file.
     * You dont need to use this because the getNames method automatically
     * uses this if needed.
     */
    public static void readNames()
    {
        int value;
        BufferedReader br = null;
        try {
            InputStream inStream = new FileInputStream(PATH+CONFIG);
            br = new BufferedReader(
                    new InputStreamReader(inStream, CHARSET));
            String line;
            while((line = br.readLine()) != null) {
                value = Integer.valueOf(line.split("=")[0]);
                instance.names[value] = line.split("=")[0]+": "+line.split("=")[1];
            }
        } catch(IOException e) {
            System.out.println("cannot load "+PATH+CONFIG);
        }
        finally {
            if(br != null) {
                try {
                    br.close();
                } catch(Exception e) {}
            }
        }
    }
    
    /**
     * Returns an array containing the names of all teams.
     * 
     * @param withNumbers   If true, each name starts with "<teamNumber>: ".
     * 
     * @return An array containing the names at their teamNumber`s position.
     */
    public static String[] getNames(boolean withNumbers)
    {
        if(instance.names[0] == null) {
            readNames();
        }
        if(withNumbers) {
            return instance.names;
        } else {
            String[] out = new String[instance.names.length];
            for(int i=0; i<instance.names.length; i++) {
                out[i] = instance.names[i].split(":")[1].substring(1);
            }
            return out;
        }
    }
    
    /**
     * Loads a team`s icon.
     * You dont need to use this because the getIcon method automatically
     * uses this if needed.
     * @param team Number of the team which icon should be read.
     */
    public static void readIcon(int team)
    {
        BufferedImage out = null;
        File file = null;
        for(int i=0; i< PIC_ENDING.length; i++) {
            file = new File(PATH+team+"."+PIC_ENDING[i]);
            if(file.exists()) {
                break;
            }
            if(i == PIC_ENDING.length-1) {
                file = null;
            }
        }
        if(file != null) {
            try{
                out = ImageIO.read(file);
            } catch(IOException e) {
                System.out.println("cannot load "+file);
            }
        }
        if(out == null) {
            out = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            Graphics graphics = out.getGraphics();
            graphics.setColor(new Color(0f, 0f, 0f, 0f));
            graphics.fillRect(0, 0, out.getWidth(), out.getHeight());
        }
        instance.icons[team] = out;
    }
    
    /**
     * Returns a team`s icon.
     * 
     * @param team   The unique teamNumber of the team you want the icon for.
     * 
     * @return The team´s icon.
     */
    public static BufferedImage getIcon(int team)
    {
        if(instance.icons[team] == null) {
            readIcon(team);
        }
        return instance.icons[team];
    }
}