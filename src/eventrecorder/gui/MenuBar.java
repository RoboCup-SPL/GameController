package eventrecorder.gui;

import eventrecorder.EventRecorder;
import eventrecorder.export.MarkDownExporter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

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
    }
}
