package tester;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BoxLayout;
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

    private static final long serialVersionUID = -2470905865009860120L;

    private static final String DEFAULT_TEXT = "Waiting for messages from the GameController...";

    final JLabel gcInfo = new JLabel(DEFAULT_TEXT, JLabel.CENTER);
    final JLabel team0Info = new JLabel("", JLabel.CENTER);
    final JLabel team1Info = new JLabel("", JLabel.CENTER);

    public MainWindow() {
        super("GameControllerTester");

        SwingUtilities.invokeLater(() -> {
            setLocationByPlatform(true);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(final WindowEvent e) {
                    GameControllerTester.shutdown();
                }
            });
            setPreferredSize(new Dimension(400, 1000));

            final JPanel contentPane = new JPanel();
            setContentPane(contentPane);
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            final JPanel gcInfoContainer = new JPanel();
            gcInfoContainer.setLayout(new BoxLayout(gcInfoContainer, BoxLayout.X_AXIS));
            gcInfoContainer.add(gcInfo);
            contentPane.add(gcInfoContainer);
            final JPanel teamInfoContainer = new JPanel();
            teamInfoContainer.setLayout(new BoxLayout(teamInfoContainer, BoxLayout.X_AXIS));
            teamInfoContainer.add(team0Info);
            teamInfoContainer.add(team1Info);
            contentPane.add(teamInfoContainer);

            pack();
            setVisible(true);
        });
    }

    @Override
    public void gameControlDataChanged(final GameControlDataEvent e) {
        gcInfo.setText("<html>" + e.data.toString().replaceAll("\n", "<br/>") + "</html>");
        team0Info.setText("<html>" + e.data.team[0].toString().replaceAll("\n", "<br/>") + "</html>");
        team1Info.setText("<html>" + e.data.team[1].toString().replaceAll("\n", "<br/>") + "</html>");
    }

    @Override
    public void gameControlDataTimeout(final GameControlDataTimeoutEvent e) {
        gcInfo.setText(DEFAULT_TEXT);
        team0Info.setText("");
        team1Info.setText("");
    }

}
