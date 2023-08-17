package eventrecorder.action;

import eventrecorder.EventRecorder;
import eventrecorder.data.LogEntry;

/**
 * Action for changing title and additional information.
 *
 * @author Andre Muehlenbrock
 */

public class TitleChangeAction extends Action{
    final String savedTitle;
    final String savedAdditional;
    final String newTitle;
    final String newAdditional;

    public TitleChangeAction(LogEntry entry, String newTitle, String newAdditional, String savedTitle, String savedAdditional) {
        super(entry, true);
        this.savedTitle = savedTitle;
        this.savedAdditional = savedAdditional;
        this.newTitle = newTitle;
        this.newAdditional = newAdditional;
    }

    @Override
    public boolean executeAction() {
        EventRecorder.model.title = newTitle;
        EventRecorder.model.additionalInfo = newAdditional;

        return true;
    }

    @Override
    public boolean undoAction() {
        EventRecorder.model.title = savedTitle;
        EventRecorder.model.additionalInfo = savedAdditional;

        return true;
    }

}
