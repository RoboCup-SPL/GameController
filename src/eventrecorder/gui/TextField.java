package eventrecorder.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JTextField;

import eventrecorder.EventRecorder;
import eventrecorder.LogEntry;
import eventrecorder.LogType;
import eventrecorder.action.EntryChangeTextAction;

/**
 * This class just display the text of a given LogEntry.
 * 
 * Here is also implemented when a Change-Action is called.
 * 
 * @author AndreM
 */

public class TextField extends JTextField{
    private static final long serialVersionUID = -634374539579879231L;
    
    private LogEntry entry;
    private String savedText = null;
    
    public TextField(LogEntry entry){
        this.entry = entry;
        setText(entry.text);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));   
        setPreferredSize(new Dimension(50,getPreferredSize().height));
        setFont(new Font("Monospaced", Font.PLAIN, 14));
        
        addFocusListener(new FocusListener(){
            
            @Override
            public void focusGained(FocusEvent e) {
                savedText = getText();
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
        String newText = getText();
        
        if(savedText != null && !savedText.equals(newText)){
            EventRecorder.history.execute(new EntryChangeTextAction(entry, newText, savedText));
        }
        
        savedText = null;
    }
    
    public LogEntry getLogEntry(){
        return entry;
    }
}
