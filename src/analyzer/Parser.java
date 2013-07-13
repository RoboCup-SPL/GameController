package analyzer;

import common.Log;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

/**
 * @author: Michel Bartsch
 * 
 */
public class Parser
{
    private static final String UNDONE_PREFIX = "<undone>";
    private static final String OUT_SEP = ",";
    
    public static void info(LogInfo log)
    {
        Date kickoffTime = null;
        Date endTime = null;
        int i = 0;
        for(String line: log.lines) {
            i++;
            int divPos = line.indexOf(": ");
            Date time = null;
            try{
                time = Log.timestampFormat.parse(line.substring(0, divPos));
            } catch(ParseException e) {
                log.parseErrors += "error in line "+i+": Cannot parse timestamp" + GUI.HTML_LF;
            }
            String action = line.substring(divPos+2);
            
            if(i == 1) {
                log.version = action;
            } else if(action.startsWith("Undo")) {
                String[] splitted = action.split(" ");
                if(splitted.length < 2) {
                    log.parseErrors += "error in line "+i+": cannot parse undo";
                } else {
                    int undos = Integer.valueOf(splitted[1]);
                    for(int j=0; j<undos; j++) {
                        log.lines.set(i-2-j, UNDONE_PREFIX+log.lines.get(i-2-j));
                    }
                }
            } else if(action.contains(" vs ")) {
                String[] teams = action.split(" vs ");
                if(teams.length == 2) {
                    log.team[0] = teams[0];
                    log.team[1] = teams[1];
                } else {
                    log.parseErrors += "error in line "+i+": Found vs but not 2 teams" + GUI.HTML_LF;
                }
            } else if( (kickoffTime == null) && (action.startsWith("Ready")) ) {
                kickoffTime = time;
            } else if(action.startsWith("Finished")) {
                endTime = time;
            }
        }
        log.start = kickoffTime;
        if( (kickoffTime != null) && (endTime != null) ) {
            log.duration = (int)((endTime.getTime()-kickoffTime.getTime())/1000);
        }
    }
    
    public static void statistic(LogInfo log)
    {
        FileWriter writer;
        try {
            Main.stats.createNewFile();
            writer = new FileWriter(Main.stats);
            writer.write("datetime"+OUT_SEP+"action"+OUT_SEP+"team"+OUT_SEP+"blue"+OUT_SEP+"red\n");
        } catch(IOException e) {
            Log.error("Cannot create and open/write to file "+Main.stats);
            return;
        }
        String time;
        String raw, action = "";
        String team = "";
        String[] teams = new String[2];
        if(log.team.length >= 2) {
            teams[0] = log.team[0];
            teams[1] = log.team[1];
        } else {
            teams[0] = "unknown";
            teams[1] = "unknown";
        }
        
        int i=0;
        for(String line: log.lines) {
            i++;
            if(line.startsWith(UNDONE_PREFIX)) {
                continue;
            }
            int divPos = line.indexOf(": ")+2;
            time = line.substring(0, divPos);
            raw = line.substring(divPos+1);
            
            //TODO
            
            try{
                writer.write(time+OUT_SEP+action+OUT_SEP+team+OUT_SEP+teams[0]+OUT_SEP+teams[1]+"\n");
                writer.flush();
            } catch(IOException e) {
                Log.error("cannot write to file "+Main.stats);
            }
        }
        try{
            writer.flush();
            writer.close();
        } catch(IOException e) {
            Log.error("cannot close file "+Main.stats);
        }
    }
}