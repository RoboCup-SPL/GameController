package eventrecorder.export;

import eventrecorder.DataModel;
import eventrecorder.LogEntry;
import eventrecorder.LogType;

/**
 * Exports the given data model to a markdown string.
 * 
 * @author Andre Muehlenbrock
 */

public class MarkDownExporter {
    public static String toMarkDown(DataModel model){
        String result = "## " + model.title + "\n" + model.additionalInfo+"\n\n";
        
        for(LogEntry entry : model.logEntries){
            if("".equals(entry.time) && "".equals(entry.text))
                continue;
            
            if(entry.type == LogType.Manually){
                result += "- "+entry.time+": "+entry.text+"\n";
            } else if(entry.type == LogType.GameState){
                result += "\n**"+entry.text+" ("+entry.time+")**\n\n";
            }
        }
        
        //result += "*Logfile End*";
        
        return result;
    }
}
