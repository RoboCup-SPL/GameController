package eventrecorder.data;

import java.util.ArrayList;

/**
 * Contains all information of the actual shown records.
 *
 * @author Andre Muehlenbrock
 */

public class DataModel {
    public String title = "";
    public String additionalInfo = "";
    public final ArrayList<LogEntry> logEntries = new ArrayList<>();

    public int currentTime = 600;
    public boolean isManuallyRunning = false;
    public long lastGameControllerInfo = 0;
}
