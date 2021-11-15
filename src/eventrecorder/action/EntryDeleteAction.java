package eventrecorder.action;

import eventrecorder.EventRecorder;
import eventrecorder.data.LogEntry;

/**
 * Action for delete a logEntry.
 *
 * @author Andre Muehlenbrock
 */
public class EntryDeleteAction extends Action{
    private int savedPosition;

    public EntryDeleteAction(LogEntry entry) {
        super(entry, true);
    }

    @Override
    public boolean executeAction() {
        savedPosition = EventRecorder.model.logEntries.indexOf(entry);
        return EventRecorder.model.logEntries.remove(savedPosition) != null;
    }

    @Override
    public boolean undoAction() {
        EventRecorder.model.logEntries.add(savedPosition, entry);
        return true;
    }

}
