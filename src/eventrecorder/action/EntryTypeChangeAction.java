package eventrecorder.action;

import eventrecorder.data.LogEntry;
import eventrecorder.data.LogType;

public class EntryTypeChangeAction extends Action{
    private final LogType savedLogType;
    private final LogType newLogType;

    public EntryTypeChangeAction(LogEntry entry, LogType newLogType, LogType savedLogType){
        this(entry, true, newLogType, savedLogType);
    }

    public EntryTypeChangeAction(LogEntry entry, boolean addToHistory, LogType newLogType, LogType savedLogType) {
        super(entry, addToHistory);
        this.savedLogType = savedLogType;
        this.newLogType = newLogType;
    }

    @Override
    public boolean executeAction() {
        entry.type = newLogType;
        return true;
    }

    @Override
    public boolean undoAction() {
        entry.type = savedLogType;
        return true;
    }

}
