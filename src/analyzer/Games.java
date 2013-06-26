package analyzer;

import java.io.File;
import java.util.LinkedList;

/**
 * @author: Michel Bartsch
 * 
 */
public class Games
{
    private final static String PATH = "logs";
    
    public LinkedList<LogInfo> logs = new LinkedList<LogInfo>();
    
    public Games()
    {
        File dir = new File(PATH);
        File[] files = dir.listFiles();
        for(File file: files) {
            logs.add(new LogInfo(file));
        }
    }
    
    public String[] getLogs()
    {
        String[] out = new String[logs.size()];
        int i = 0;
        for(LogInfo log: logs) {
            out[i++] = log+"";
        }
        return out;
    }
}