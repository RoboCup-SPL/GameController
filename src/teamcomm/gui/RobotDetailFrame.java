package teamcomm.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import teamcomm.data.RobotState;
import teamcomm.data.event.RobotStateEvent;
import teamcomm.data.event.RobotStateEventListener;

/**
 * Class for the windows showing detailed information about robots.
 *
 * @author Felix Thielke
 */
public abstract class RobotDetailFrame extends JFrame implements RobotStateEventListener {

    private static final long serialVersionUID = 4709653396291218508L;
    private final RobotState robot;

    /**
     * Constructor.
     *
     * @param robot robot to create the frame for
     * @param anchor panel which triggers the frame on doubleclick
     */
    public RobotDetailFrame(final RobotState robot, final JPanel anchor) {
        super(robot.getAddress());
        this.robot = robot;

        final RobotStateEventListener listener = this;

        SwingUtilities.invokeLater(() -> {
            setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            anchor.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        if (!isVisible()) {
                            setLocationRelativeTo(anchor);
                        }
                        setVisible(true);
                        robotStateChanged(new RobotStateEvent(robot));
                        connectionStatusChanged(new RobotStateEvent(robot));
                    }
                }
            });

            init(robot);
            robot.addListener(listener);
        });
    }

    /**
     * Initialises the frame for the given RobotState.
     *
     * @param robot RobotState to visualize
     */
    protected abstract void init(final RobotState robot);

    /**
     * Releases resources of this frame.
     */
    public void destroy() {
        setVisible(false);
        robot.removeListener(this);
    }

    /**
     * Dispose this frame.
     */
    @Override
    public void dispose() {
        super.dispose();
        robot.removeListener(this);
    }
}
