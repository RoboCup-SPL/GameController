package eventrecorder.action;

import eventrecorder.LogEntry;

/**
 * Changes the time value of a LogEntry
 * 
 * @author Andre Muehlenbrock
 */

public class EntryChangeTimeAction extends Action{
    private String newTime;
    private String savedTime;
    private boolean wasFirstTimeSet;
    
    /**
     * Creates a new ChangeAction.
     * 
     * @param entry         LogEntry in the DataModel.
     * @param newLogEntry   Copy of the LogEntry with new values.
     * @param savedLogEntry Copy of the LogEntry with old values.
     */
    
    public EntryChangeTimeAction(LogEntry entry, String newTime, String savedTime){
        super(entry, true);
        this.newTime = newTime;
        this.savedTime = savedTime;
        wasFirstTimeSet = entry.firstTimeSet;
    }
    
    @Override
    public boolean executeAction() {
        entry.time = newTime;
        entry.firstTimeSet = false;
        return true;
    }

    @Override
    public boolean undoAction() {
        entry.time = savedTime;
        entry.firstTimeSet = wasFirstTimeSet;
        return true;
    }
}
