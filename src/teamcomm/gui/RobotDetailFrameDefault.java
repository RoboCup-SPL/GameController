package teamcomm.gui;

import common.Log;
import data.GameControlReturnData;
import data.SPLTeamMessage;
import java.text.DecimalFormat;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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

/**
 * Default class for the windows showing detailed information about robots.
 *
 * @author Felix Thielke
 */
public class RobotDetailFrameDefault extends RobotDetailFrame {

    private static final long serialVersionUID = -5895955615794161798L;

    private final JLabel topLabel = new JLabel("", SwingConstants.CENTER);
    private final JPanel infoContainer = new JPanel();
    private final Border labelBorder = new EmptyBorder(0, 4, 4, 4);

    /**
     * Constructor.
     *
     * @param robot robot to create the frame for
     * @param anchor panel which triggers the frame on doubleclick
     */
    public RobotDetailFrameDefault(final RobotState robot, final JPanel anchor) {
        super(robot, anchor);
    }

    @Override
    protected void init(final RobotState robot) {
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

        update(robot);

        setResizable(false);
    }

    @Override
    public void robotStateChanged(final RobotStateEvent e) {
        if (isVisible()) {
            SwingUtilities.invokeLater(() -> {
                update((RobotState) e.getSource());
                repaint();
            });
        }
    }

    /**
     * Updates the frame with information of the given robot.
     */
    private void update(final RobotState robot) {
        final GameControlReturnData returnMessage = robot.getLastGCRDMessage();
        final SPLTeamMessage teamMessage = robot.getLastTeamMessage();
        if (returnMessage == null && teamMessage == null) {
            return;
        }

        final DecimalFormat df = new DecimalFormat("#.00000");

        // Top label
        final StringBuilder sb = new StringBuilder("<html>");
        if (returnMessage != null && (!returnMessage.teamNumValid || returnMessage.teamNum != robot.getTeamNumber())) {
            sb.append("<font color='red'>Invalid team no: ").append(returnMessage.teamNum).append(" on \"port\" ").append(robot.getTeamNumber()).append("</font>");
        } else {
            sb.append(GameState.getInstance().getTeamName(robot.getTeamNumber(), true, true));
        }
        sb.append("<br/>");
        if (robot.getPlayerNumber() == null || (returnMessage != null && !returnMessage.playerNumValid)) {
            sb.append("<font color='red'>Player no: ").append(returnMessage != null ? returnMessage.playerNum : "unknown").append("</font>");
        } else {
            sb.append("Player no: ").append(robot.getPlayerNumber());
        }
        sb.append("<br/>")
                .append("Team Messages: ").append(robot.getTeamMessageCount())
                .append("<br/>");
        sb.append("Per second: ").append(df.format(robot.getTeamMessagesPerSecond()));
        sb.append("<br/>");
        if (teamMessage != null && !teamMessage.valid) {
            sb.append("<font color='red'>");
        }
        sb.append("Illegal: ").append(robot.getIllegalTeamMessageCount()).append(" (").append(Math.round(robot.getIllegalTeamMessageRatio() * 100.0)).append("%)");
        if (teamMessage != null && !teamMessage.valid) {
            sb.append("</font>");
        }
        sb.append("<br/>")
                .append("GameController Return Messages: ").append(robot.getGCRDMessageCount())
                .append("<br/>");
        sb.append("Per second: ").append(df.format(robot.getGCRDMessagesPerSecond()));
        sb.append("<br/>");
        if (returnMessage != null && !returnMessage.valid) {
            sb.append("<font color='red'>");
        }
        sb.append("Illegal: ").append(robot.getIllegalGCRDMessageCount()).append(" (").append(Math.round(robot.getIllegalGCRDMessageRatio() * 100.0)).append("%)");
        if (returnMessage != null && !returnMessage.valid) {
            sb.append("</font>");
        }
        sb.append("</html>");
        topLabel.setText(sb.toString());

        // Left label
        sb.setLength(6);

        if (teamMessage != null) {
            if (teamMessage.valid) {
                sb.append("Additional data: ").append(teamMessage.data.length).append("B (").append(teamMessage.data.length * 100 / SPLTeamMessage.MAX_SIZE).append("%)");
            } else {
                sb.append("<font color='red'>Additional data: ").append(teamMessage.data.length).append("B</font>");
            }
        }

        boolean additionalLine = false;

        if (returnMessage != null) {
            if (!returnMessage.headerValid) {
                if (!additionalLine) {
                    sb.append("<br/>");
                    additionalLine = true;
                }
                sb.append("<br/><font color='red'>Invalid header: ").append(returnMessage.header).append("</font>");
            }

            if (!returnMessage.versionValid) {
                if (!additionalLine) {
                    sb.append("<br/>");
                    additionalLine = true;
                }
                sb.append("<br/><font color='red'>Invalid version: ").append(returnMessage.version).append("</font>");
            }

            if (!returnMessage.fallenValid) {
                if (!additionalLine) {
                    sb.append("<br/>");
                    additionalLine = true;
                }
                sb.append("<br/><font color='red'>Invalid fallen state</font>");
            }

            if (!returnMessage.poseValid) {
                if (!additionalLine) {
                    sb.append("<br/>");
                    additionalLine = true;
                }
                sb.append("<br/><font color='red'>Invalid pose: x").append(df.format(returnMessage.pose[0])).append(" y").append(df.format(returnMessage.pose[1])).append(" t").append(df.format(returnMessage.pose[2])).append("</font>");
            }

            if (!returnMessage.ballValid) {
                if (!additionalLine) {
                    sb.append("<br/>");
                }
                sb.append("<br/><font color='red'>Invalid ball data: age ").append(returnMessage.ballAge).append(" x").append(returnMessage.ball[0]).append(" y").append(returnMessage.ball[1]).append("</font>");
            }
        }
        sb.append("</html>");
        ((JLabel) infoContainer.getComponent(0)).setText(sb.toString());

        final String[] data;
        if (teamMessage instanceof AdvancedMessage) {
            try {
                data = ((AdvancedMessage) teamMessage).display();
            } catch (final Throwable e) {
                Log.error(e.getClass().getSimpleName() + " was thrown while displaying custom message data from " + teamMessage.getClass().getSimpleName() + ": " + e.getMessage());
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
                } else if (!row.isEmpty()) {
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
        // Do nothing
    }
}
