package data;

import common.Log;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

/**
 * This class provides the icons and names including unique teamNumbers of all
 * teams written in the config file.
 *
 * This class is a singleton!
 *
 * @author Michel Bartsch
 */
public class Teams {

    /**
     * Information about each team.
     */
    private static class Info {

        /**
         * The name of the team.
         */
        public final String name;
        /**
         * The icon of the team.
         */
        public BufferedImage icon;

        /**
         * Create a new team information.
         *
         * @param name The name of the team.
         */
        public Info(String name) {
            this.name = name;
        }
    }

    /**
     * The path to the leagues directories.
     */
    private static final String PATH = "config/";
    /**
     * The name of the config file.
     */
    private static final String CONFIG = "teams.cfg";
    /**
     * The charset to read the config file.
     */
    private final static String CHARSET = "UTF-8";
    /**
     * The possible file-endings icons may have. The full name of an icon must
     * be "<teamNumber>.<png|gif>", for example "7.png".
     */
    private static final String[] PIC_ENDING = {"png", "gif", "jpg", "jpeg"};

    /**
     * The instance of the singleton.
     */
    private static final Teams instance = new Teams();

    /**
     * The information read from the config files.
     */
    private final Info[][] teams;

    /**
     * Creates a new Teams object.
     */
    private Teams() {
        teams = new Info[Rules.LEAGUES.length][];
        for (int i = 0; i < Rules.LEAGUES.length; i++) {
            String dir = Rules.LEAGUES[i].leagueDirectory;
            int maxValue = 0;
            BufferedReader br = null;
            try {
                InputStream inStream = Files.newInputStream(Paths.get(PATH + dir + "/" + CONFIG));
                br = new BufferedReader(
                        new InputStreamReader(inStream, CHARSET));
                String line;
                while ((line = br.readLine()) != null) {
                    try {
                        final int value = Integer.parseInt(line.split("=", 2)[0]);
                        if (value > maxValue) {
                            maxValue = value;
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            } catch (IOException e) {
                Log.error("cannot load " + PATH + dir + "/" + CONFIG);
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (Exception e) {
                    }
                }
            }
            teams[i] = new Info[maxValue + 1];
        }
    }

    /**
     * Returns the index the current league has within the LEAGUES-array.
     *
     * @return the leagues index.
     */
    private static int getLeagueIndex() {
        for (int i = 0; i < Rules.LEAGUES.length; i++) {
            if (Rules.LEAGUES[i] == Rules.league) {
                return i;
            }
        }
        //should never happen
        Log.error("selected league is odd");
        return -1;
    }

    /**
     * Reads the names of all teams in the config file. You don't need to use
     * this because the getNames method automatically uses this if needed.
     */
    public static void readTeams() {
        BufferedReader br = null;
        try {
            InputStream inStream = Files.newInputStream(Paths.get(PATH + Rules.league.leagueDirectory + "/" + CONFIG));
            br = new BufferedReader(
                    new InputStreamReader(inStream, CHARSET));
            String line;
            while ((line = br.readLine()) != null) {
                final String[] entry = line.split("=", 2);
                if (entry.length == 2) {
                    int key = -1;
                    try {
                        key = Integer.parseInt(entry[0]);
                    } catch (NumberFormatException e) {
                    }
                    if (key >= 0) {
                        instance.teams[getLeagueIndex()][key] = new Info(entry[1]);
                    } else {
                        Log.error("error in teams.cfg: \"" + entry[0] + "\" is not a valid team number");
                    }
                } else if (!line.trim().isEmpty()) {
                    Log.error("malformed entry in teams.cfg: \"" + line + "\"");
                }
            }
        } catch (Exception e) {
            Log.error("cannot load " + PATH + Rules.league.leagueDirectory + "/" + CONFIG);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * Returns an array containing the names of all teams.
     *
     * @param withNumbers If true, each name starts with "<teamNumber>: ".
     * @return An array containing the names at their teamNumber's position.
     */
    public static String[] getNames(boolean withNumbers) {
        int leagueIndex = getLeagueIndex();
        if (instance.teams[leagueIndex][0] == null) {
            readTeams();
        }
        String[] out = new String[instance.teams[leagueIndex].length];
        for (int i = 0; i < instance.teams[leagueIndex].length; i++) {
            if (instance.teams[leagueIndex][i] != null) {
                out[i] = instance.teams[leagueIndex][i].name + (withNumbers ? " (" + i + ")" : "");
            }
        }
        return out;
    }

    /**
     * Loads a team's icon. You don't need to use this because the getIcon
     * method automatically uses this if needed.
     *
     * @param team Number of the team which icon should be read.
     */
    private static void readIcon(int team) {
        BufferedImage out = null;
        File file = getIconPath(team);
        if (file != null) {
            try {
                out = ImageIO.read(file);
            } catch (IOException e) {
                Log.error("cannot load " + file);
            }
        }
        if (out == null) {
            out = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            Graphics graphics = out.getGraphics();
            graphics.setColor(new Color(0f, 0f, 0f, 0f));
            graphics.fillRect(0, 0, out.getWidth(), out.getHeight());
        }
        instance.teams[getLeagueIndex()][team].icon = out;
    }

    /**
     * Returns the file path to a team's icon
     *
     * @param team The unique team number of the team you want the icon for.
     * @return The team's icon.
     */
    public static File getIconPath(int team) {
        for (final String ending : PIC_ENDING) {
            final File file = new File(PATH + Rules.league.leagueDirectory + "/" + team + "." + ending);
            if (file.exists()) {
                return file;
            }
        }

        return null;
    }

    /**
     * Returns a team's icon.
     *
     * @param team The unique team number of the team you want the icon for.
     * @return The team's icon.
     */
    public static BufferedImage getIcon(int team) {
        int leagueIndex = getLeagueIndex();
        if (instance.teams[leagueIndex][team] == null) {
            readTeams();
        }
        if (instance.teams[leagueIndex][team].icon == null) {
            readIcon(team);
        }
        return instance.teams[leagueIndex][team].icon;
    }
}
