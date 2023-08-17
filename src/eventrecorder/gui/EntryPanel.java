package eventrecorder.gui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import eventrecorder.EventRecorder;
import eventrecorder.action.EntryDeleteAction;
import eventrecorder.action.EntryTypeChangeAction;
import eventrecorder.data.LogEntry;
import eventrecorder.data.LogType;

/**
 * Displays a line in the logEntryTable.
 *
 * @author Andre Muehlenbrock
 */

public class EntryPanel extends JPanel{
    private static final long serialVersionUID = -907777557585101050L;
    private final TimeField timeField;
    private final TextField textField;

    public EntryPanel(final LogEntry e){
        timeField = new TimeField(e);
        textField = new TextField(e);

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(3, 3, 0, 3));
        add(timeField, BorderLayout.WEST);
        add(textField, BorderLayout.CENTER);

        JPanel controlEntryPanel = new JPanel();
        controlEntryPanel.setLayout(new BoxLayout(controlEntryPanel,BoxLayout.X_AXIS));
        controlEntryPanel.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));


        final JComboBox<LogType> logTypeChooser = new JComboBox<>(new LogType[]{LogType.Manually, LogType.GameState, LogType.PlayerState, LogType.SetPlayState});
        logTypeChooser.setSelectedItem(e.type);
        logTypeChooser.addActionListener(aE -> EventRecorder.history.execute(new EntryTypeChangeAction(e, (LogType)logTypeChooser.getSelectedItem(), e.type)));

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(aE -> EventRecorder.history.execute(new EntryDeleteAction(e)));

        controlEntryPanel.add(logTypeChooser);
        controlEntryPanel.add(deleteButton);


        add(controlEntryPanel, BorderLayout.EAST);
    }

    public TimeField getTimeField(){
        return timeField;
    }

    public TextField getTextField(){
        return textField;
    }
}
