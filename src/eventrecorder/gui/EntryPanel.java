package eventrecorder.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import eventrecorder.EventRecorder;
import eventrecorder.LogEntry;
import eventrecorder.action.EntryDeleteAction;

/**
 * Displays a line in the logEntryTable.
 * 
 * @author AndreM
 */

public class EntryPanel extends JPanel{
    private static final long serialVersionUID = -907777557585101050L;
    private TimeField timeField;
    private TextField textField;
    
    public EntryPanel(final LogEntry e){
        timeField = new TimeField(e);
        textField = new TextField(e);
        
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(3, 3, 0, 3));
        add(timeField, BorderLayout.WEST);
        add(textField, BorderLayout.CENTER);
        
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent aE) {
                EventRecorder.history.execute(new EntryDeleteAction(e));
            }
        });
        add(deleteButton, BorderLayout.EAST);
    }
    
    public TimeField getTimeField(){
        return timeField;
    }
    
    public TextField getTextField(){
        return textField;
    }
}
