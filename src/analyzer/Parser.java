package analyzer;

import common.Log;
import java.text.ParseException;
import java.util.Date;

/**
 * @author: Michel Bartsch
 * 
 */
public class Parser
{
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
                log.parseErrors += "error in line "+i+": Cannot parse timestamp\n";
            }
            String action = line.substring(divPos+2);
            
            if(i == 1) {
                log.version = action;
            } else if(action.contains(" vs ")) {
                String[] teams = action.split(" vs ");
                if(teams.length == 2) {
                    log.team[0] = teams[0];
                    log.team[1] = teams[1];
                } else {
                    log.parseErrors += "error in line "+i+": Found vs but not 2 teams\n";
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
        int i=0;
        for(String line: log.lines) {
            i++;
            int divPos = line.indexOf(": ")+2;
            Date time = null;
            try{
                time = Log.timestampFormat.parse(line.substring(0, divPos));
            } catch(ParseException e) {
                log.parseErrors += "error in line "+i+": Cannot parse timestamp\n";
            }
            String action = line.substring(divPos+1);
            
            //TODO
        }
    }
}