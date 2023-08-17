package eventrecorder.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import eventrecorder.EventRecorder;
import eventrecorder.action.EntryChangeTimeAction;
import eventrecorder.data.LogEntry;
import eventrecorder.data.LogType;

/**
 * This class just display the time of a given LogEntry.
 *
 * Here is also implemented when a Change-Action is called.
 *
 * @author Andre Muehlenbrock
 */

public class TimeField extends JTextField {
    private static final long serialVersionUID = 1446285867560827423L;
    private final LogEntry entry;
    private String savedTime = null;

    public TimeField(LogEntry entry) {
        this.entry = entry;
        setText(entry.time);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(65,getPreferredSize().height));
        setFont(new Font("Monospaced", Font.PLAIN, 14));

        addFocusListener(new FocusListener(){

            @Override
            public void focusGained(FocusEvent e) {
                makeVisibleInScrollPane();

                savedTime = getText();
            }

            @Override
            public void focusLost(FocusEvent e) {
                executeChangeAction();
            }

        });

        if(entry.type == LogType.Manually){
            setBackground(MainFrame.MANUALLY_LOG_ENTRY_COLOR);
        } else if(entry.type == LogType.GameState){
            setBackground(MainFrame.GAMESTATE_LOG_ENTRY_COLOR);
        } else if(entry.type == LogType.PlayerState){
            setBackground(MainFrame.PLAYERSTATE_LOG_ENTRY_COLOR);
        }
    }

    /**
     * This method scrolls the parent scrollpane so that this element is visible.
     *
     * Ugly code but it works... It is necessary that the parent/child structure is:
     * ScrollPane -> (JViewport) -> LogEntryTable -> EntryPanel -> This
     */

    public void makeVisibleInScrollPane(){
        LogEntryTable entryTable = (LogEntryTable) getParent().getParent();
        entryTable.getParent().getParent().getParent().validate();
        entryTable.getParent().getParent().getParent().repaint();
        JComponent parent = (JComponent) entryTable.getParent().getParent();
        JScrollPane scrollPane = (JScrollPane)entryTable.getParent().getParent().getParent();
        float scrollPaneValue = scrollPane.getVerticalScrollBar().getValue();

        Rectangle b = getParent().getBounds();

        parent.scrollRectToVisible(new Rectangle(0, (int)(b.y - scrollPaneValue), b.width, b.height));
    }

    public void executeChangeAction(){
        String newTime = getText();

        if(savedTime != null && !savedTime.equals(newTime))
            EventRecorder.history.execute(new EntryChangeTimeAction(entry, newTime, savedTime));

        savedTime = null;
    }

    public LogEntry getLogEntry(){
        return entry;
    }
}
