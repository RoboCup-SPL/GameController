package eventrecorder.action;

import eventrecorder.data.LogEntry;

/**
 * Changes the time value of a LogEntry
 *
 * @author Andre Muehlenbrock
 */

public class EntryChangeTimeAction extends Action{
    private final String newTime;
    private final String savedTime;
    private final boolean wasFirstTimeSet;

    /**
     * Creates a new ChangeAction.
     *
     * @param entry     LogEntry in the DataModel.
     * @param newTime   New time value.
     * @param savedTime Previous time value to support undo.
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
