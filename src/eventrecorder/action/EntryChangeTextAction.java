package eventrecorder.action;

import eventrecorder.LogEntry;

/**
 * Changes the text value of a LogEntry
 * 
 * @author Andre Muehlenbrock
 */

public class EntryChangeTextAction extends Action{
    private String newText;
    private String savedText;
    
    /**
     * Creates a new ChangeAction.
     * 
     * @param entry         LogEntry in the DataModel.
     * @param newLogEntry   Copy of the LogEntry with new values.
     * @param savedLogEntry Copy of the LogEntry with old values.
     */
    
    public EntryChangeTextAction(LogEntry entry, String newText, String savedText){
        super(entry, true);
        this.newText = newText;
        this.savedText = savedText;
    }
    
    @Override
    public boolean executeAction() {
        entry.text = newText;
        return true;
    }

    @Override
    public boolean undoAction() {
        entry.text = savedText;
        return true;
    }
}
