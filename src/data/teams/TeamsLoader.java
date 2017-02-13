package data.teams;

import common.Log;
import data.Rules;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;


/**
 * This class provides the icons and names including unique teamNumbers of all
 * teams written in the config file.
 * 
 * This class is a singleton!
 * 
 * @author Michel Bartsch
 */
public class TeamsLoader
{
    /** Dynamically settable path to the config root folder */
    private static final String CONFIG_ROOT = System.getProperty("CONFIG_ROOT", "");

    /** The path to the leagues directories. */
    private static final String PATH = CONFIG_ROOT + "config/";

    /** The name of the config file. */
    private static final String CONFIG = "teams.cfg";

    /** The charset to read the config file. */
    private final static String CHARSET = "UTF-8";

    /**
     * The possible file-endings icons may have.
     * The full name of an icon must be "<teamNumber>.<png|gif>", for example
     * "7.png".
     */
    private static final String[] ALLOWED_IMAGE_FORMATS = {"png", "gif", "jpg", "jpeg"};


    /** The instance of the singleton. */
    private static TeamsLoader instance;

    public static TeamsLoader getInstance(){
        if (instance == null){
            try {
                instance = new TeamsLoader();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    private Map<String, ArrayList<TeamLoadInfo>> _team_mapping;

    private TeamsLoader() throws FileNotFoundException {

        _team_mapping = new HashMap<>();

        for (int i=0; i < Rules.LEAGUES.length; i++) {

            String dir = Rules.LEAGUES[i].leagueDirectory;
            String leagueName = Rules.LEAGUES[i].leagueName;

            ArrayList<TeamLoadInfo> infos = new ArrayList<TeamLoadInfo>();
            _team_mapping.put(leagueName, infos);

            Scanner s = new Scanner(new File(PATH+dir+"/"+CONFIG));

            while(s.hasNext()){
                String line = s.nextLine();

                String[] components = line.split("=");
                int identifier = Integer.valueOf(components[0]);

                String[] name_and_colors = components[1].split(",");
                String name = name_and_colors[0];

                TeamLoadInfo ti;
                if (name_and_colors.length > 1){
                    String[] values = Arrays.copyOfRange(name_and_colors, 1, name_and_colors.length);
                    ti = new TeamLoadInfo(identifier, name, values);
                } else {
                    ti = new TeamLoadInfo(identifier, name);
                }

                BufferedImage bf = readIcon(dir, identifier);

                // Add the picture to the team info if present
                if (bf != null){
                    ti.icon = bf;
                }
                infos.add(ti);
            }
        }
    }


    /**
     * Returns an array containing the names of all teams.
     * @param withNumbers If true, each name starts with "<teamNumber>: ".
     * @return An array containing the names at their teamNumber's position.
     */
    public ArrayList<String> getNames(String leagueName, boolean withNumbers)
    {
        ArrayList<TeamLoadInfo> teams = _team_mapping.get(leagueName);
        ArrayList<String> teams_strigns = new ArrayList<>();

        for (TeamLoadInfo tli : teams) {
            String team = tli.name + (withNumbers ? " (" + tli.identifier + ")" : "");
            teams_strigns.add(team);
        }
        return teams_strigns;
    }
    

    private BufferedImage readIcon(String leagueDir, int teamId)
    {
        BufferedImage out = null;
        File file = getIconPath(leagueDir, teamId);
        if (file != null) {
            try{
                out = ImageIO.read(file);
            } catch (IOException e) {
                Log.error("cannot load "+file);
            }
        }
        if (out == null) {
            out = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            Graphics graphics = out.getGraphics();
            graphics.setColor(new Color(0f, 0f, 0f, 0f));
            graphics.fillRect(0, 0, out.getWidth(), out.getHeight());
        }
        return out;
    }

    /**
     * Tests the different endings because teams might supply the icon in different formats
     * @param leagueDir - the directory of the lague
     * @param teamId - the team id
     * @return A file object or null if the file was not found
     */
    private File getIconPath(String leagueDir, int teamId) {
        for (final String ending : ALLOWED_IMAGE_FORMATS) {
            final File file = new File(PATH + leagueDir + "/" + teamId + "." + ending);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    public ArrayList<TeamLoadInfo> getTeamLoadInfoList(String leagueName) {
        return _team_mapping.get(leagueName);
    }
}