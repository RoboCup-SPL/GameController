package eventrecorder.gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import eventrecorder.EventRecorder;
import eventrecorder.action.Action;
import eventrecorder.action.EntryChangeTextAction;
import eventrecorder.action.EntryChangeTimeAction;
import eventrecorder.action.EntryCreateAction;
import eventrecorder.action.EntryDeleteAction;
import eventrecorder.data.LogEntry;
import eventrecorder.data.LogType;

/**
 * This class is the shown logEntryTable which displays all saved log entries.
 *
 * It also implements for the timeField and textField how to Jump when
 * UP,DOWN,LEFT,RIGHT,BACK_SPACE,ENTF OR ENTER is pressed.
 *
 * @author Andre Muehlenbrock
 */

public class LogEntryTable extends JPanel{
    private static final long serialVersionUID = 6135881777310302271L;

    public LogEntryTable(){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        createLogEntryTable();
    }

    private void createLogEntryTable(){
        // Removes all childs:
        removeAll();

        // Adds all LogEntries from the DataModel:
        ArrayList<LogEntry> entries = EventRecorder.model.logEntries;
        for(LogEntry e : entries){
            EntryPanel entryPanel = new EntryPanel(e);
            entryPanel.getTimeField().addKeyListener(new TimeFieldKeyListener(this, e));
            entryPanel.getTextField().addKeyListener(new TextFieldKeyListener(this, e));
            add(entryPanel);
        }

        revalidate();
        repaint();
    }

    public int getIdByLogEntry(LogEntry entry){
        for(int i=0;i<getComponents().length;i++){
            if(entry == ((EntryPanel)getComponent(i)).getTextField().getLogEntry())
                return i;
        }

        return -1;
    }

    /**
     * KeyListener for a TimeField in the EntryPanel
     *
     * @author Andre Muehlenbrock
     */

    static class TimeFieldKeyListener extends KeyAdapter {
        private final LogEntryTable table;
        private final LogEntry entry;

        public TimeFieldKeyListener(LogEntryTable table, LogEntry entry){
            this.table = table;
            this.entry = entry;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int id = table.getIdByLogEntry(entry);
            EntryPanel nextPanel = id + 1 >= table.getComponents().length ? null : (EntryPanel)table.getComponent(id+1);
            EntryPanel thisPanel = (EntryPanel)table.getComponent(id);
            EntryPanel prevPanel = id - 1 < 0 ? null : (EntryPanel)table.getComponent(id-1);
            TimeField thisField = thisPanel.getTimeField();

            if(e.getKeyCode() == KeyEvent.VK_ENTER){
                thisField.executeChangeAction();
                EventRecorder.history.execute(new EntryCreateAction(new LogEntry("", "", LogType.Manually)));

                e.consume();
            } else  if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE && thisField.getCaretPosition() == 0){
                if(prevPanel != null && thisField.getSelectedText() == null){
                    if("".equals(thisPanel.getTextField().getText()) && "".equals(thisPanel.getTimeField().getText())){
                        thisField.executeChangeAction();
                        EventRecorder.history.execute(new EntryDeleteAction(entry));

                        e.consume();
                    } else {
                        prevPanel.getTextField().setCaretPosition(prevPanel.getTextField().getText().length());
                        prevPanel.getTextField().requestFocus();

                        e.consume();
                    }
                }
            } else if(e.getKeyCode() == KeyEvent.VK_UP){
                if(prevPanel != null){
                    int newCaretPosition = Math.min(prevPanel.getTimeField().getText().length(), thisField.getCaretPosition());
                    prevPanel.getTimeField().setCaretPosition(newCaretPosition);
                    prevPanel.getTimeField().requestFocus();

                    e.consume();
                }
            } else if(e.getKeyCode() == KeyEvent.VK_DOWN){
                if(nextPanel != null){
                    int newCaretPosition = Math.min(nextPanel.getTimeField().getText().length(), thisField.getCaretPosition());
                    nextPanel.getTimeField().setCaretPosition(newCaretPosition);
                    nextPanel.getTimeField().requestFocus();

                    e.consume();
                }
            } else if(e.getKeyCode() == KeyEvent.VK_RIGHT && thisField.getCaretPosition() == thisField.getText().length()){
                thisPanel.getTextField().setCaretPosition(0);
                thisPanel.getTextField().requestFocus();

                e.consume();
            } else if(e.getKeyCode() == KeyEvent.VK_LEFT && thisField.getCaretPosition() == 0){
                if(prevPanel != null){
                    prevPanel.getTextField().setCaretPosition(prevPanel.getTextField().getText().length());
                    prevPanel.getTextField().requestFocus();

                    e.consume();
                }
            } else if(thisField.getLogEntry().firstTimeSet){
                EventRecorder.history.execute(new EntryChangeTimeAction(thisField.getLogEntry(), MainFrame.TIME_FORMAT.format(EventRecorder.model.currentTime*1000),""));
            }
        }
    }

    /**
     * KeyListener for a TextField in the EntryPanel
     *
     * @author Andre Muehlenbrock
     */

    static class TextFieldKeyListener extends KeyAdapter {
        private final LogEntryTable table;
        private final LogEntry entry;

        public TextFieldKeyListener(LogEntryTable table, LogEntry entry){
            this.table = table;
            this.entry = entry;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int id = table.getIdByLogEntry(entry);
            EntryPanel nextPanel = id + 1 >= table.getComponents().length ? null : (EntryPanel)table.getComponent(id+1);
            EntryPanel thisPanel = (EntryPanel)table.getComponent(id);
            EntryPanel prevPanel = id - 1 < 0 ? null : (EntryPanel)table.getComponent(id-1);
            TextField thisField = thisPanel.getTextField();

            if(e.getKeyCode() == KeyEvent.VK_ENTER){
                thisField.executeChangeAction();
                EventRecorder.history.execute(new EntryCreateAction(new LogEntry("", "", LogType.Manually),id+1));

                e.consume();
            } else if(e.getKeyCode() == KeyEvent.VK_BACK_SPACE && thisField.getCaretPosition() == 0 && thisField.getSelectedText() == null){
                if("".equals(thisPanel.getTextField().getText())){
                    thisField.executeChangeAction();
                    EventRecorder.history.execute(new EntryDeleteAction(entry));

                    e.consume();
                } else {
                    thisField.executeChangeAction();
                    thisPanel.getTimeField().setCaretPosition(thisPanel.getTimeField().getText().length());
                    thisPanel.getTimeField().requestFocus();

                    e.consume();
                }
            } else if(e.getKeyCode() == KeyEvent.VK_UP){
                if(prevPanel != null){
                    int newCaretPosition = Math.min(prevPanel.getTextField().getText().length(), thisField.getCaretPosition());
                    prevPanel.getTextField().setCaretPosition(newCaretPosition);
                    prevPanel.getTextField().requestFocus();

                    e.consume();
                }
            } else if(e.getKeyCode() == KeyEvent.VK_DOWN){
                if(nextPanel != null){
                    int newCaretPosition = Math.min(nextPanel.getTextField().getText().length(), thisField.getCaretPosition());
                    nextPanel.getTextField().setCaretPosition(newCaretPosition);
                    nextPanel.getTextField().requestFocus();

                    e.consume();
                }
            } else if(e.getKeyCode() == KeyEvent.VK_LEFT && thisField.getCaretPosition() == 0){
                thisPanel.getTimeField().setCaretPosition(thisPanel.getTimeField().getText().length());
                thisPanel.getTimeField().requestFocus();

                e.consume();
            } else if(e.getKeyCode() == KeyEvent.VK_RIGHT && thisField.getCaretPosition() == thisField.getText().length()){
                if(nextPanel != null){
                    nextPanel.getTimeField().setCaretPosition(0);
                    nextPanel.getTimeField().requestFocus();

                    e.consume();
                }
            } else if(thisField.getLogEntry().firstTimeSet && !thisField.getText().isEmpty()){
                String time = (EventRecorder.model.currentTime < 0? "-":"")+MainFrame.TIME_FORMAT.format(Math.abs(EventRecorder.model.currentTime*1000));
                EventRecorder.history.execute(new EntryChangeTimeAction(thisField.getLogEntry(), time, ""));
            }
        }
    }

    public void entryActionWasExecuted(Action action){
        int id = getIdByLogEntry(action.getAffectedLogEntry());

        if(action instanceof EntryChangeTimeAction){
            if(id == -1)
                return;

            EntryPanel entryPanel = (EntryPanel)getComponent(id);
            String timeString = action.getAffectedLogEntry().time;

            if(entryPanel.getTimeField().getText().equals(timeString))
                return;

            entryPanel.getTimeField().setText(timeString);
            entryPanel.getTimeField().setCaretPosition(0);
            entryPanel.revalidate();
            entryPanel.repaint();

        } else if(action instanceof EntryChangeTextAction){
            if(id == -1)
                return;

            EntryPanel entryPanel = (EntryPanel)getComponent(id);
            if(entryPanel.getTextField().getText().equals(action.getAffectedLogEntry().text))
                return;

            entryPanel.getTextField().setText(action.getAffectedLogEntry().text);
            entryPanel.getTextField().setCaretPosition(0);
            entryPanel.revalidate();
            entryPanel.repaint();
        } else if(action instanceof EntryCreateAction && !action.shouldBeAddedToHistory()){
            EntryPanel entryPanel = new EntryPanel(action.getAffectedLogEntry());
            entryPanel.getTimeField().addKeyListener(new TimeFieldKeyListener(this, action.getAffectedLogEntry()));
            entryPanel.getTextField().addKeyListener(new TextFieldKeyListener(this, action.getAffectedLogEntry()));

            int index = EventRecorder.model.logEntries.indexOf(action.getAffectedLogEntry());

            if(index != -1)
                add(entryPanel, index);
        } else if(action instanceof EntryCreateAction || action instanceof EntryDeleteAction){
            createLogEntryTable();
            int newId = getIdByLogEntry(action.getAffectedLogEntry());

            EntryPanel entryPanel;
            // if the entry was added:
            if(newId != -1){
                entryPanel = (EntryPanel)getComponent(newId);
                entryPanel.getTextField().setCaretPosition(0);
                entryPanel.getTextField().requestFocus();
            } else { // if the entry was removed:
                if(getComponents().length > 0){
                    entryPanel = (EntryPanel)getComponent(Math.max(id - 1, 0));
                    if(entryPanel != null){
                        entryPanel.getTextField().setCaretPosition(entryPanel.getTextField().getText().length());
                        entryPanel.getTextField().requestFocus();
                    }
                }
            }
        } else {
            createLogEntryTable();
        }

    }
}
