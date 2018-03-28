package eventrecorder.gui;

import eventrecorder.EventRecorder;
import eventrecorder.export.MarkDownExporter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import data.PlayerInfo;

/**
 * The menu bar at the top of the program.
 *
 * @author Andre Muehlenbrock
 */
public class MenuBar extends JMenuBar {

    private static final long serialVersionUID = -915489966130126852L;

    private int MARKDOWN_WINDOW_WIDTH = 800;
    private int MARKDOWN_WINDOW_HEIGHT = 600;

    public MenuBar() {
        JMenu file = new JMenu("File");
        JMenuItem viewLogFile = new JMenuItem("View as MarkDown");
        viewLogFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame newWindow = new JFrame("Markdown View");
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int width = Math.min((int) screenSize.getWidth(), MARKDOWN_WINDOW_WIDTH);
                int height = Math.min((int) screenSize.getHeight(), MARKDOWN_WINDOW_HEIGHT);
                int x = ((int) screenSize.getWidth() - width) / 2;
                int y = ((int) screenSize.getHeight() - height) / 2;

                // Set some window settings:
                newWindow.setBounds(x, y, width, height);

                JPanel main = new JPanel();
                main.setLayout(new BorderLayout());

                JTextArea textArea = new JTextArea();
                textArea.setText(MarkDownExporter.toMarkDown(EventRecorder.model));

                main.add(new JScrollPane(textArea), BorderLayout.CENTER);

                newWindow.add(main);
                newWindow.setVisible(true);
            }

        });

        JMenuItem saveLogFile = new JMenuItem("Save as MarkDown");
        saveLogFile.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                EventRecorder.gui.saveAs();
            }

        });

        JMenuItem exit = new JMenuItem("Exit");

        file.add(viewLogFile);
        file.add(saveLogFile);
        file.addSeparator();
        file.add(exit);

        add(file);
        

        JMenu logging = new JMenu("Logging");
        
        JPanel loggingPanel = new JPanel();
        loggingPanel.setLayout(new BoxLayout(loggingPanel, BoxLayout.Y_AXIS));
        
        loggingPanel.add(new JLabel("<html><body><b>Penalty Logging Settings:</b></body></html>"));
        loggingPanel.add(new JSeparator(JSeparator.HORIZONTAL));

        Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
        for(int i=0;i<10;i++) {
            boolean checked = prefs.getBoolean("LOG_PENALTY_"+i, i == 4 || i == 6);
            // Load settings:
			EventRecorder.setLogPenalty(i, checked);
        	
			String penaltyString = i == 0 ? "Back In Game" : 
				EventRecorder.capitalize(PlayerInfo.getPenaltyName(i));
			
			final int finalI = i;
			
			// Setup the Checkbox:
        	final JCheckBox checkBox = new JCheckBox(penaltyString);
        	checkBox.setSelected(checked);
        	checkBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					EventRecorder.setLogPenalty(finalI, checkBox.isSelected());
				}
        	});
        	
        	loggingPanel.add(checkBox);
        }
        
        loggingPanel.add(new JSeparator(JSeparator.HORIZONTAL));

        final JCheckBox checkBox = new JCheckBox("Free Kicks");

        checkBox.setSelected(false);
        checkBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EventRecorder.setLogFreeKicks(checkBox.isSelected());
            }
        });
        loggingPanel.add(checkBox);

        loggingPanel.add(new JSeparator(JSeparator.HORIZONTAL));
        
        logging.add(loggingPanel);
        add(logging);
    }
}
