package tester;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import teamcomm.data.event.GameControlDataEvent;
import teamcomm.data.event.GameControlDataEventListener;
import teamcomm.data.event.GameControlDataTimeoutEvent;

/**
 * Main window of the GameController tester.
 *
 * @author Felix Thielke
 */
public class MainWindow extends JFrame implements GameControlDataEventListener {

    private static final String DEFAULT_TEXT = "Waiting for messages from the GameController...";

    final JLabel text = new JLabel(DEFAULT_TEXT, JLabel.LEADING);

    public MainWindow() {
        super("GameControllerTester");

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setLocationByPlatform(true);
                setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(final WindowEvent e) {
                        GameControllerTester.shutdown();
                    }
                });
                setPreferredSize(new Dimension(400, 300));

                final JPanel contentPane = new JPanel();
                setContentPane(contentPane);
                contentPane.add(text);

                pack();
                setVisible(true);
            }
        });
    }

    @Override
    public void gameControlDataChanged(final GameControlDataEvent e) {
        text.setText("<html>" + e.data.toString().replaceAll("\n", "<br/>") + "</html>");
        System.out.println(e.data.toString());
    }

    @Override
    public void gameControlDataTimeout(final GameControlDataTimeoutEvent e) {
        text.setText(DEFAULT_TEXT);
    }

}
