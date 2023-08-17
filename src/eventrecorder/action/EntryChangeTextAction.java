package eventrecorder.action;

import eventrecorder.data.LogEntry;

/**
 * Changes the text value of a LogEntry
 *
 * @author Andre Muehlenbrock
 */

public class EntryChangeTextAction extends Action{
    private final String newText;
    private final String savedText;

    /**
     * Creates a new ChangeAction.
     *
     * @param entry     LogEntry in the DataModel.
     * @param newText   entry's text.
     * @param savedText entry's previous text.
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
