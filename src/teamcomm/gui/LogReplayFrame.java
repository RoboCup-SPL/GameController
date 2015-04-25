package teamcomm.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import teamcomm.net.logging.LogReplayEvent;
import teamcomm.net.logging.LogReplayEventListener;

/**
 *
 * @author Felix Thielke
 */
public class LogReplayFrame extends JFrame implements LogReplayEventListener {

    private static final long serialVersionUID = -2837554836011688982L;

    private final JLabel stateLabel = new JLabel("Paused");
    private final JLabel timeLabel = new JLabel("00:00");

    private final JToggleButton rewindFastButton = new JToggleButton("<<", false);
    private final JToggleButton rewindButton = new JToggleButton("<", false);
    private final JToggleButton pauseButton = new JToggleButton("||", true);
    private final JToggleButton playButton = new JToggleButton(">", false);
    private final JToggleButton fastForwardButton = new JToggleButton(">>", false);

    public LogReplayFrame(final JFrame parent) {
        super("Replay log file");

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final JPanel contentPane = new JPanel();
                contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
                contentPane.setBorder(new EmptyBorder(5, 5, 10, 5));
                setContentPane(contentPane);

                final JPanel infoPanel = new JPanel();
                infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));
                infoPanel.setBorder(new EmptyBorder(5, 8, 5, 8));
                stateLabel.setHorizontalAlignment(SwingConstants.LEFT);
                infoPanel.add(stateLabel);
                infoPanel.add(new Box.Filler(new Dimension(), new Dimension(), new Dimension(32767, 0)));
                timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
                infoPanel.add(timeLabel);
                contentPane.add(infoPanel);

                contentPane.add(new Box.Filler(new Dimension(), new Dimension(), new Dimension(0, 32767)));

                final JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
                controlsPanel.add(rewindFastButton);
                controlsPanel.add(new Box.Filler(new Dimension(), new Dimension(), new Dimension(32767, 0)));
                controlsPanel.add(rewindButton);
                controlsPanel.add(new Box.Filler(new Dimension(), new Dimension(), new Dimension(32767, 0)));
                controlsPanel.add(pauseButton);
                controlsPanel.add(new Box.Filler(new Dimension(), new Dimension(), new Dimension(32767, 0)));
                controlsPanel.add(playButton);
                controlsPanel.add(new Box.Filler(new Dimension(), new Dimension(), new Dimension(32767, 0)));
                controlsPanel.add(fastForwardButton);
                contentPane.add(controlsPanel);

                setLocationRelativeTo(parent);
                setAlwaysOnTop(true);
                setResizable(false);
                pack();
                setVisible(true);
            }
        });
    }

    @Override
    public void loggingStatus(LogReplayEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
