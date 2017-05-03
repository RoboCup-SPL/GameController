package eventrecorder.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JTextField;

import eventrecorder.EventRecorder;
import eventrecorder.LogEntry;
import eventrecorder.LogType;
import eventrecorder.action.EntryChangeTimeAction;

/**
 * This class just display the time of a given LogEntry.
 * 
 * Here is also implemented when a Change-Action is called.
 * 
 * @author Andre Muehlenbrock
 */

public class TimeField extends JTextField {
    private static final long serialVersionUID = 1446285867560827423L;
    private LogEntry entry;
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
