package teamcomm.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.text.DecimalFormat;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import teamcomm.data.RobotState;
import teamcomm.data.event.RobotStateEvent;
import teamcomm.data.event.RobotStateEventListener;

/**
 * Class for the panel showing basic information about robots.
 *
 * @author Felix Thielke
 */
public class RobotPanel extends JPanel implements RobotStateEventListener {

    private static final long serialVersionUID = 6656251707032959704L;

    public static final int PANEL_WIDTH = 175;
    public static final int PANEL_HEIGHT = 105;

    private final RobotState robot;
    private final RobotDetailFrame detailFrame;

    /**
     * Constructor.
     *
     * @param robot robot to create the panel for
     */
    public RobotPanel(final RobotState robot) {
        super();

        this.robot = robot;

        final JPanel panel = this;
        final RobotStateEventListener listener = this;

        setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), robot.getAddress(), TitledBorder.CENTER, TitledBorder.TOP));
        setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));

        for (int i = 0; i < 5; i++) {
            add(new JLabel(" ", JLabel.LEFT));
        }

        update();
        robot.addListener(listener);

        detailFrame = new RobotDetailFrame(robot, this);
    }

    @Override
    public void robotStateChanged(final RobotStateEvent e) {
        if (isVisible()) {
            update();
        }
    }

    /**
     * Updates the panel with information of the given robot.
     */
    private void update() {
        final DecimalFormat df = new DecimalFormat("#.#####");
        synchronized (getTreeLock()) {
            ((JLabel) getComponent(0)).setText("Player no: " + (robot.getLastMessage() == null ? "?" : robot.getLastMessage().playerNum));
            ((JLabel) getComponent(1)).setText("Messages: " + robot.getMessageCount());
            ((JLabel) getComponent(2)).setText("Current mps: " + df.format(robot.getRecentMessageCount()));
            ((JLabel) getComponent(3)).setText("Average mps: " + df.format(robot.getMessagesPerSecond()));
            ((JLabel) getComponent(4)).setText("Illegal: " + robot.getIllegalMessageCount() + " (" + Math.round(robot.getIllegalMessageRatio() * 100.0) + "%)");
        }
    }

    public void dispose() {
        robot.removeListener(this);
        detailFrame.dispose();
    }

    public String getRobotAddress() {
        return robot.getAddress();
    }

}
