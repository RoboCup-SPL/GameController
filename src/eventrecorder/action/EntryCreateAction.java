package eventrecorder.action;

import eventrecorder.EventRecorder;
import eventrecorder.data.LogEntry;

/**
 * Creates a LogEntry
 *
 * @author Andre Muehlenbrock
 */

public class EntryCreateAction extends Action{
    private int position;

    /**
     * Creates a CreateAction which adds the entry to the end of the list.
     *
     * @param entry LogEntry
     */

    public EntryCreateAction(LogEntry entry){
        this(entry, EventRecorder.model.logEntries.size(), true);
    }


    public EntryCreateAction(LogEntry entry, boolean addToHistory){
        this(entry, EventRecorder.model.logEntries.size(), addToHistory);
    }

    public EntryCreateAction(LogEntry entry, int position){
        super(entry, true);
        this.position = position;
    }

    /**
     * Creates a CreateAction which adds the entry to the given position of the list.
     *
     * @param entry     LogEntry
     * @param position  LogEntry position.
     */

    public EntryCreateAction(LogEntry entry, int position, boolean addToHistory){
        super(entry, addToHistory);
        this.position = position;
    }

    @Override
    public boolean executeAction() {
        EventRecorder.model.logEntries.add(position,entry);
        return true;
    }

    @Override
    public boolean undoAction() {
        position = EventRecorder.model.logEntries.indexOf(entry);
        if(position == -1)
            return false;
        return EventRecorder.model.logEntries.remove(position) != null;
    }

}
