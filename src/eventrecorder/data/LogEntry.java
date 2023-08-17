package eventrecorder.data;

/**
 * Data of an entry.
 *
 * @author Andre Muehlenbrock
 */

public class LogEntry {
    public String text;
    public String time;
    public LogType type;
    public boolean firstTimeSet;

    /**
     * Creates a new LogEntry object with the given values.
     *
     * @param text Text
     * @param time Time in Seconds
     * @param type LogType
     */

    public LogEntry(String text, String time, LogType type){
        this.text = text;
        this.time = time;
        this.type = type;
        this.firstTimeSet = "".equals(time);
    }

    /**
     * Sets the values of this LogEntry to the value of e.
     *
     * @param e The LogEntry containing the values.
     */

    public void set(LogEntry e){
        text = e.text;
        time = e.time;
        type = e.type;
    }
}
