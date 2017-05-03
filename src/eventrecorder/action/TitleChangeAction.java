package eventrecorder.action;

import eventrecorder.EventRecorder;
import eventrecorder.LogEntry;

public class TitleChangeAction extends Action{
    String savedTitle;
    String savedAdditional;
    String newTitle;
    String newAdditional;
    
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
