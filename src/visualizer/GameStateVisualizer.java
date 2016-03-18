package visualizer;

import common.Log;
import data.Rules;

/**
 * @author Michel Bartsch
 * 
 * The game-state-visualizer-program starts in this class.
 * The main components are initialized here.
 */
public class GameStateVisualizer
{        
    private static final String HELP_TEMPLATE = "Usage: java -jar GameStateVisualizer.jar {options}"
            + "\n  (-h | --help)                   display help"
            + "\n  (-l | --league) %s%sselect league (default is spl)"
            + "\n  (-w | --window)                 select window mode (default is fullscreen)"
            + "\n";
    private static final String COMMAND_HELP = "--help";
    private static final String COMMAND_HELP_SHORT = "-h";
    private final static String COMMAND_LEAGUE = "--league";
    private final static String COMMAND_LEAGUE_SHORT = "-l";
    private static final String COMMAND_WINDOW = "--window";
    private static final String COMMAND_WINDOW_SHORT = "-w";
    
    private static Listener listener;

    /**
     * The program starts here.
     * 
     * @param args  This is ignored.
     */
    public static void main(String[] args)
    {
        boolean windowMode = false;
        //commands
        parsing:
        for (int i=0; i<args.length; i++) {
            if ((args.length > i+1)
                    && ((args[i].equalsIgnoreCase(COMMAND_LEAGUE_SHORT))
                    || (args[i].equalsIgnoreCase(COMMAND_LEAGUE))) ) {
                i++;
                for (int j=0; j < Rules.LEAGUES.length; j++) {
                    if (Rules.LEAGUES[j].leagueDirectory.equals(args[i])) {
                        Rules.league = Rules.LEAGUES[j];
                        continue parsing;
                    }
                }
            } else if (args[i].equals(COMMAND_WINDOW_SHORT) || args[i].equals(COMMAND_WINDOW)) {
                windowMode = true;
                continue parsing;
            }
            String leagues = "";
            for (Rules rules : Rules.LEAGUES) {
                leagues += (leagues.equals("") ? "" : " | ") + rules.leagueDirectory;
            }
            if (leagues.contains("|")) {
                leagues = "(" + leagues + ")";
            }
            System.out.printf(HELP_TEMPLATE, leagues, leagues.length() < 17
                              ? "                ".substring(leagues.length())
                              : "\n                                  ");
            System.exit(0);
        }
        
        GUI gui = new GUI(!windowMode);
        new KeyboardListener(gui);
        listener = new Listener(gui);
        listener.start();
    }
    
    /**
     * This should be called when the program is shutting down to close
     * sockets and finally exit.
     */
    public static void exit()
    {
        listener.interrupt();
        try {
            listener.join();
        } catch (InterruptedException e) {
            Log.error("Waiting for listener to shutdown was interrupted.");
        }
        System.exit(0);
    }
}