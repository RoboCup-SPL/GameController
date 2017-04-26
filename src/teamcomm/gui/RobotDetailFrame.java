package teamcomm.gui;

import common.Log;
import data.SPLStandardMessage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
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
    private final JLabel topLabel = new JLabel("", SwingConstants.CENTER);
    private final JPanel infoContainer = new JPanel();
    private final Border labelBorder = new EmptyBorder(0, 4, 4, 4);

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
                            update();
                            repaint();
                        }
                    }
                });

                final JPanel contentPane = new JPanel();
                setContentPane(contentPane);

                contentPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new JLabel("test").getForeground()), robot.getAddress(), TitledBorder.CENTER, TitledBorder.TOP));

                contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

                final JPanel topPanel = new JPanel();
                topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
                topPanel.add(topLabel);
                topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
                contentPane.add(topPanel);

                infoContainer.setLayout(new BoxLayout(infoContainer, BoxLayout.X_AXIS));
                final JLabel infoLabel = new JLabel("", SwingConstants.LEFT);
                infoLabel.setBorder(labelBorder);
                infoLabel.setAlignmentY(TOP_ALIGNMENT);
                infoContainer.add(infoLabel);
                contentPane.add(infoContainer);

                update();

                setResizable(false);

                robot.addListener(listener);
            }
        });
    }

    @Override
    public void robotStateChanged(final RobotStateEvent e) {
        if (isVisible()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    update();
                    repaint();
                }
            });
        }
    }

    /**
     * Releases resources of this frame.
     */
    public void destroy() {
        setVisible(false);
        robot.removeListener(this);
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
            return;
        }

        final DecimalFormat df = new DecimalFormat("#.00000");

        // Top label
        final StringBuilder sb = new StringBuilder("<html>");
        if (!msg.teamNumValid || msg.teamNum != robot.getTeamNumber()) {
            sb.append("<font color='red'>Invalid team no: ").append(msg.teamNum).append("</font>");
        } else {
            sb.append(GameState.getInstance().getTeamName(robot.getTeamNumber(), true, true));
        }
        sb.append("<br/>");
        if (robot.getPlayerNumber() == null || !msg.playerNumValid) {
            sb.append("<font color='red'>Player no: ").append(msg.playerNum).append("</font>");
        } else {
            sb.append("Player no: ").append(robot.getPlayerNumber());
        }
        sb.append("<br/>")
                .append("Messages: ").append(robot.getMessageCount())
                .append("<br/>");
        final double mps = robot.getMessagesPerSecond();
        if (mps > RobotPanel.MPS_LEGAL_THRESHOLD) {
            sb.append("<font color='red'>");
        }
        sb.append("Per second: ").append(df.format(mps));
        if (mps > RobotPanel.MPS_LEGAL_THRESHOLD) {
            sb.append("</font>");
        }
        sb.append("<br/>");
        if (!msg.valid) {
            sb.append("<font color='red'>");
        }
        sb.append("Illegal: ").append(robot.getIllegalMessageCount()).append(" (").append(Math.round(robot.getIllegalMessageRatio() * 100.0)).append("%)");
        if (!msg.valid) {
            sb.append("</font>");
        }
        sb.append("</html>");
        topLabel.setText(sb.toString());

        // Left label
        sb.setLength(6);
        if (msg.fallenValid) {
            sb.append(msg.fallen ? "fallen" : "upright");
        } else {
            sb.append("<font color='red'>unknown state</font>");
        }
        sb.append("<br/>");
        if (msg.intentionValid) {
            sb.append("Activity: ").append(msg.intention.toString());
        } else {
            sb.append("<font color='red'>Activity: ?</font>");
        }
        sb.append("<br/><br/>Confidence:<br/>");
        if (msg.currentPositionConfidenceValid) {
            sb.append("Position: ").append(msg.currentPositionConfidence).append("%");
        } else {
            sb.append("<font color='red'>Position: ").append(msg.currentPositionConfidence).append("%</font>");
        }
        sb.append("<br/>");
        if (msg.currentSideConfidenceValid) {
            sb.append("Side: ").append(msg.currentSideConfidence).append("%");
        } else {
            sb.append("<font color='red'>Side: ").append(msg.currentSideConfidence).append("%</font>");
        }
        sb.append("<br/><br/>");
        if (msg.averageWalkSpeedValid) {
            sb.append("Avg. walk speed: ").append(msg.averageWalkSpeed).append("mm/s");
        } else {
            sb.append("<font color='red'>Avg. walk speed: ").append(msg.averageWalkSpeed).append("mm/s</font>");
        }
        sb.append("<br/>");
        if (msg.maxKickDistanceValid) {
            sb.append("Max. kick distance: ").append(msg.maxKickDistance).append("mm");
        } else {
            sb.append("<font color='red'>Max. kick distance: ").append(msg.maxKickDistance).append("mm</font>");
        }
        sb.append("<br/><br/>");
        for (int i = 0; i < 5; i++) {
            if (msg.suggestionValid[i]) {
                sb.append("Suggestion ").append(i + 1).append(": ").append(msg.suggestion[i].toString());
            } else {
                sb.append("<font color='red'>Suggestion ").append(i + 1).append(": ?</font>");
            }
            sb.append("<br/>");
        }
        sb.append("<br/>");
        if (msg.dataValid) {
            sb.append("Additional data: ").append(msg.data.length).append("B (").append(msg.data.length * 100 / SPLStandardMessage.SPL_STANDARD_MESSAGE_DATA_SIZE).append("%)");
        } else {
            sb.append("<font color='red'>Additional data: ").append(msg.nominalDataBytes).append("B</font>");
        }

        boolean additionalLine = false;

        if (!msg.poseValid) {
            if (!additionalLine) {
                sb.append("<br/>");
                additionalLine = true;
            }
            sb.append("<br/><font color='red'>Invalid pose: x").append(df.format(msg.pose[0])).append(" y").append(df.format(msg.pose[1])).append(" t").append(df.format(msg.pose[2])).append("</font>");
        }

        if (!msg.walkingToValid) {
            if (!additionalLine) {
                sb.append("<br/>");
                additionalLine = true;
            }
            sb.append("<br/><font color='red'>Invalid walking target: x").append(df.format(msg.walkingTo[0])).append(" y").append(df.format(msg.walkingTo[1])).append("</font>");
        }

        if (!msg.shootingToValid) {
            if (!additionalLine) {
                sb.append("<br/>");
                additionalLine = true;
            }
            sb.append("<br/><font color='red'>Invalid shooting target: x").append(df.format(msg.shootingTo[0])).append(" y").append(df.format(msg.shootingTo[1])).append("</font>");
        }

        if (!msg.ballValid) {
            if (!additionalLine) {
                sb.append("<br/>");
            }
            sb.append("<br/><font color='red'>Invalid ball data: age ").append(msg.ballAge).append(" x").append(msg.ball[0]).append("/").append(msg.ballVel[0]).append(" y").append(msg.ball[1]).append("/").append(msg.ballVel[1]).append("</font>");
        }
        sb.append("</html>");
        ((JLabel) infoContainer.getComponent(0)).setText(sb.toString());

        final String[] data;
        if (msg instanceof AdvancedMessage) {
            try {
                data = ((AdvancedMessage) msg).display();
            } catch (final Throwable e) {
                Log.error(e.getClass().getSimpleName() + " was thrown while displaying custom message data from " + msg.getClass().getSimpleName() + ": " + e.getMessage());
                return;
            }
        } else {
            data = null;
        }
        if (data != null && data.length != 0) {
            int column = 1;
            boolean firstRow = true;
            boolean contentInCurrentRow = false;

            sb.setLength(6);
            for (final String row : data) {
                if (firstRow) {
                    firstRow = false;
                } else {
                    sb.append("<br/>");
                }

                if (row.equals(AdvancedMessage.DISPLAY_NEXT_COLUMN)) {
                    if (contentInCurrentRow) {
                        if (infoContainer.getComponentCount() == column) {
                            final JLabel newColumn = new JLabel("", SwingConstants.LEFT);
                            newColumn.setAlignmentY(TOP_ALIGNMENT);
                            newColumn.setBorder(labelBorder);
                            infoContainer.add(newColumn);
                        }
                        ((JLabel) infoContainer.getComponent(column)).setText(sb.toString());
                        column++;
                        firstRow = true;
                        contentInCurrentRow = false;
                        sb.setLength(6);
                    }
                } else if (row.length() > 0) {
                    sb.append(row);
                    contentInCurrentRow = true;
                }
            }
            if (contentInCurrentRow) {
                if (infoContainer.getComponentCount() == column) {
                    final JLabel newColumn = new JLabel("", JLabel.LEFT);
                    newColumn.setAlignmentY(TOP_ALIGNMENT);
                    newColumn.setBorder(labelBorder);
                    infoContainer.add(newColumn);
                }
                ((JLabel) infoContainer.getComponent(column)).setText(sb.toString());
                column++;
            }

            while (infoContainer.getComponentCount() > column) {
                infoContainer.remove(column);
            }
        } else {
            while (infoContainer.getComponentCount() > 1) {
                infoContainer.remove(1);
            }
        }

        pack();
    }

    @Override
    public void connectionStatusChanged(final RobotStateEvent e) {

    }
}
