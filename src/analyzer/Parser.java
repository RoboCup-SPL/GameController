package analyzer;

import common.Log;
import data.Rules;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Michel Bartsch
 * 
 * This class contains all methods meant to parse logs.
 */
public class Parser
{
    /* This prefix will be placed at the beginning of every logInfo´s log line,
     which was made undone by an undo*/
    private static final String UNDONE_PREFIX = "<undone>";
    /* The seperator used for the output within a row. */
    private static final String OUT_SEP = ",";
    /* The output´s date format (date-time) */
    public static final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
    
    /* All the actions that would cause the teams to change colors */
    private static final String[] colorChangeActions = {
        "1st Half",
        "1st Half Extra Time",
        "2nd Half",
        "2nd Half Extra Time"
    };
    
    /*
     * All the actions to extract from the logs into statistics.
     * If there are actions which are not prefix-free, the longer
     * action´s string must be first. For example "Playing with Hands"
     * for "Playing".
     */
    private static final String[] actions = {
        "Manually Penalised",
        "Dropped Ball",
        "Kickoff Goal",
        "Global Game Stuck",
        "Goal for Team",
        "Goal decrease for Team",
        "Out by",
        "Substituted by Player",
        "Timeout",
        "Illegal Attack",
        "Ball Manipulation",
        "Illegal Defender",
        "Illegal Defense",
        "Fallen Robot",
        "Playing with Hands",
        "Ball Holding",
        "Inactive Player",
        "Leaving the Field",
        "Request for PickUp",
        "Request for Service",
        "Additional Request for Service",
        "Player Pushing",
        "Substitute Player",
        "Finished",
        "Initial",
        "Playing",
        "Ready",
        "Set"
    };
    
    
     /**
     * Parsing a log to get some information and place the undone-prefix,
     * so this is needded before parsing for statistics. The information
     * will be written into the LogInfo instance.
     * 
     * @param log   The log to parse.
     */
    public static void info(LogInfo log)
    {
        Date kickoffTime = null;
        Date endTime = null;
        int i = 0;
        for (String line : log.lines) {
            i++;
            int divPos = line.indexOf(": ");
            Date time = null;
            try{
                time = Log.timestampFormat.parse(line.substring(0, divPos));
            } catch (ParseException e) {
                log.parseErrors += "error in line "+i+": Cannot parse timestamp" + GUI.HTML_LF;
            }
            String action = line.substring(divPos+2);
            
            if (i == 1) {
                log.version = action;
            } else if (action.startsWith("League = ")) {
                String league = action.substring(9);
                for (int j=0; j<Rules.LEAGUES.length; j++) {
                    if (Rules.LEAGUES[j].leagueName.equals(league)) {
                        log.league = Rules.LEAGUES[j];
                    }
                }
            } else if (action.startsWith("Auto color change = false")) {
                log.keepColors = true;
            } else if (action.startsWith("Undo")) {
                String[] splitted = action.split(" ");
                if (splitted.length < 2) {
                    log.parseErrors += "error in line "+i+": cannot parse undo";
                } else {
                    int undos = Integer.valueOf(splitted[1]);
                    for (int j=0; j<undos; j++) {
                        log.lines.set(i-2-j, UNDONE_PREFIX+log.lines.get(i-2-j));
                    }
                }
            } else if (action.contains(" vs ")) {
                String[] teams = action.split(" vs ");
                if (teams.length == 2) {
                    log.team[0] = teams[0];
                    log.team[1] = teams[1];
                } else {
                    log.parseErrors += "error in line "+i+": Found vs but not 2 teams" + GUI.HTML_LF;
                }
            } else if ((kickoffTime == null) && (action.startsWith("Ready"))) {
                kickoffTime = time;
            } else if (action.startsWith("Finished")) {
                endTime = time;
            }
        }
        log.start = kickoffTime;
        if ((kickoffTime != null) && (endTime != null)) {
            log.duration = (int)((endTime.getTime()-kickoffTime.getTime())/1000);
        }
    }
    
    /**
     * Parsing a log to write all statistics from it into a file. The file
     * is set in the main class.
     * 
     * @param log   The log to parse.
     */
    public static void statistic(LogInfo log)
    {
        Date rawTime;
        String time;
        String raw, action = "";
        String team;
        String[] teams = new String[2];
        if (log.team.length >= 2) {
            teams[0] = log.team[0];
            teams[1] = log.team[1];
        } else {
            teams[0] = "unknown";
            teams[1] = "unknown";
        }
        
        int i=0;
        for (String line : log.lines) {
            i++;
            if (line.startsWith(UNDONE_PREFIX)) {
                continue;
            }
            int divPos = line.indexOf(": ")+2;
            try{
                rawTime = Log.timestampFormat.parse(line.substring(0, divPos-2));
            } catch (ParseException e) {
                Log.error("Cannot parse time in line "+i+" of "+log.file);
                return;
            }
            raw = line.substring(divPos);
            
            time = timeFormat.format(rawTime);
            
            if (!log.keepColors) {
                for (String ca : colorChangeActions) {
                    if (raw.startsWith(ca)) {
                        String tmp = teams[0];
                        teams[0] = teams[1];
                        teams[1] = tmp;
                        break;
                    }
                }
            }
            
            boolean actionMatch = false;
            for (String a : actions) {
                if (raw.startsWith(a)) {
                    action = a;
                    actionMatch = true;
                    break;
                }
            }
            if (!actionMatch) {
                continue;
            }
            
            if (raw.contains(log.league.teamColorName[0])) {
                team = teams[0];
            } else if (raw.contains(log.league.teamColorName[1])) {
                team = teams[1];
            } else {
                team = "";
            }
            
            try{
                Main.writer.write(time+OUT_SEP+action+OUT_SEP+team+OUT_SEP+teams[0]+OUT_SEP+teams[1]+"\n");
            } catch (IOException e) {
                Log.error("cannot write to file "+Main.stats);
            }
        }
    }
}