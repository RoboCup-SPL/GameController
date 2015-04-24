package teamcomm.gui;

import data.SPLStandardMessage;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import teamcomm.data.AdvancedMessage;
import teamcomm.data.GameState;
import teamcomm.data.RobotState;
import teamcomm.data.event.RobotStateEvent;
import teamcomm.data.event.RobotStateEventListener;

/**
 * Class for the windows showing detailed information about robots.
 *
 * @author Felix Thielke
 */
public class RobotDetailFrame extends JFrame implements RobotStateEventListener {

    private static final long serialVersionUID = 4709653396291218508L;

    private final RobotState robot;
    private final JPanel leftPanel = new JPanel();
    private final JPanel rightPanel = new JPanel();

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

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                anchor.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            if (!isVisible()) {
                                setLocationRelativeTo(anchor);
                            }
                            setVisible(true);
                        }
                    }
                });

                final JPanel contentPane = new JPanel();
                setContentPane(contentPane);

                contentPane.setLayout(new GridLayout(1, 2, 0, 5));
                contentPane.add(leftPanel);

                contentPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), robot.getAddress(), TitledBorder.CENTER, TitledBorder.TOP));

                leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
                rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

                for (int i = 0; i < 22; i++) {
                    leftPanel.add(new JLabel(" ", JLabel.LEFT));
                }

                update();

                pack();
                setResizable(false);

                robot.addListener(listener);
            }
        });
    }

    @Override
    public void robotStateChanged(final RobotStateEvent e) {
        if (isVisible()) {
            update();
            repaint();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        robot.removeListener(this);
    }

    /**
     * Updates the frame with information of the given robot.
     */
    private void update() {
        final SPLStandardMessage msg = robot.getLastMessage();
        if (msg == null) {
            setIllegalValues();
        } else {
            final DecimalFormat df = new DecimalFormat("#.#####");
            synchronized (leftPanel.getTreeLock()) {
                ((JLabel) leftPanel.getComponent(0)).setText(GameState.getInstance().getTeamName((int) msg.teamNum));
                ((JLabel) leftPanel.getComponent(1)).setText("Player no: " + msg.playerNum);
                ((JLabel) leftPanel.getComponent(2)).setText("Messages: " + robot.getMessageCount());
                ((JLabel) leftPanel.getComponent(3)).setText("Per second: " + df.format(robot.getMessagesPerSecond()));
                ((JLabel) leftPanel.getComponent(4)).setText("Illegal: " + robot.getIllegalMessageCount() + " (" + Math.round(robot.getIllegalMessageRatio() * 100.0) + "%)");
                ((JLabel) leftPanel.getComponent(6)).setText(msg.fallen ? "fallen" : "upright");
                ((JLabel) leftPanel.getComponent(7)).setText("Activity: " + msg.intention.toString());
                ((JLabel) leftPanel.getComponent(8)).setText("Pos.X: " + df.format(msg.pose[0]));
                ((JLabel) leftPanel.getComponent(9)).setText("Pos.Y: " + df.format(msg.pose[1]));
                ((JLabel) leftPanel.getComponent(10)).setText("Pos.T: " + df.format(msg.pose[2]));
                ((JLabel) leftPanel.getComponent(11)).setText("Target.X: " + df.format(msg.walkingTo[0]));
                ((JLabel) leftPanel.getComponent(12)).setText("Target.Y: " + df.format(msg.walkingTo[1]));
                ((JLabel) leftPanel.getComponent(13)).setText("Shot.X: " + df.format(msg.shootingTo[0]));
                ((JLabel) leftPanel.getComponent(14)).setText("Shot.Y: " + df.format(msg.shootingTo[1]));
                ((JLabel) leftPanel.getComponent(15)).setText("BallRel.X: " + df.format(msg.ball[0]));
                ((JLabel) leftPanel.getComponent(16)).setText("BallRel.Y: " + df.format(msg.ball[1]));
                ((JLabel) leftPanel.getComponent(17)).setText("BallVel.X: " + df.format(msg.ballVel[0]));
                ((JLabel) leftPanel.getComponent(18)).setText("BallVel.Y: " + df.format(msg.ballVel[1]));
                ((JLabel) leftPanel.getComponent(19)).setText("BallAge: " + msg.ballAge);
                ((JLabel) leftPanel.getComponent(21)).setText("Additional data: " + msg.data.length + "B (" + (msg.data.length * 100 / SPLStandardMessage.SPL_STANDARD_MESSAGE_DATA_SIZE) + "%)");
            }
        }

        if (msg instanceof AdvancedMessage) {
            final String[] data = ((AdvancedMessage) msg).display();
            if (data != null && data.length != 0) {
                synchronized (rightPanel.getTreeLock()) {
                    final int componentCount = rightPanel.getComponentCount() - 6;
                    for (int i = componentCount; i < data.length; i++) {
                        rightPanel.add(new JLabel(" ", JLabel.LEFT));
                    }
                    for (int i = componentCount - 1; i >= data.length; i++) {
                        rightPanel.remove(i);
                    }

                    for (int i = 0; i < data.length; i++) {
                        if (data[i] != null) {
                            if (data[i].isEmpty()) {
                                ((JLabel) rightPanel.getComponent(i + 6)).setText(" ");
                            } else {
                                ((JLabel) rightPanel.getComponent(i + 6)).setText(data[i]);
                            }
                        }
                    }
                }

                if (getContentPane().getComponentCount() == 1) {
                    getContentPane().add(rightPanel);
                    pack();
                }
            } else if (getContentPane().getComponentCount() == 2) {
                getContentPane().remove(rightPanel);
                pack();
            }
        } else if (getContentPane().getComponentCount() == 2) {
            getContentPane().remove(rightPanel);
            pack();
        }
    }

    private void setIllegalValues() {
        final DecimalFormat df = new DecimalFormat("#.#####");
        synchronized (leftPanel.getTreeLock()) {
            ((JLabel) leftPanel.getComponent(0)).setText(GameState.getInstance().getTeamName(null));
            ((JLabel) leftPanel.getComponent(1)).setText("Player no: ?");
            ((JLabel) leftPanel.getComponent(2)).setText("Messages: " + robot.getMessageCount());
            ((JLabel) leftPanel.getComponent(3)).setText("Per second: " + df.format(robot.getMessagesPerSecond()));
            ((JLabel) leftPanel.getComponent(4)).setText("Illegal: " + robot.getIllegalMessageCount() + " (" + Math.round(robot.getIllegalMessageRatio() * 100.0) + "%)");
            ((JLabel) leftPanel.getComponent(6)).setText("unknown state");
            ((JLabel) leftPanel.getComponent(7)).setText("Activity: ?");
            ((JLabel) leftPanel.getComponent(8)).setText("Pos.X: ?");
            ((JLabel) leftPanel.getComponent(9)).setText("Pos.Y: ?");
            ((JLabel) leftPanel.getComponent(10)).setText("Pos.T: ?");
            ((JLabel) leftPanel.getComponent(11)).setText("Target.X: ?");
            ((JLabel) leftPanel.getComponent(12)).setText("Target.Y: ?");
            ((JLabel) leftPanel.getComponent(13)).setText("Shot.X: ?");
            ((JLabel) leftPanel.getComponent(14)).setText("Shot.Y: ?");
            ((JLabel) leftPanel.getComponent(15)).setText("BallRel.X: ?");
            ((JLabel) leftPanel.getComponent(16)).setText("BallRel.Y: ?");
            ((JLabel) leftPanel.getComponent(17)).setText("BallVel.X: ?");
            ((JLabel) leftPanel.getComponent(18)).setText("BallVel.Y: ?");
            ((JLabel) leftPanel.getComponent(19)).setText("BallAge: ?");
            ((JLabel) leftPanel.getComponent(21)).setText("Additional data: ?");
        }
    }
}
