package eventrecorder;

import java.util.ArrayList;
import java.util.Date;

/**
 * Contains all information of the actual shown records.
 * 
 * @author AndreM
 */

public class DataModel {
    public String title = "";    
    public String additionalInfo = "";
    public ArrayList<LogEntry> logEntries = new ArrayList<LogEntry>();
    
    public int currentTime = 600;
    public boolean isManuallyRunning = false;
    public long lastGameControllerInfo = 0;
}
