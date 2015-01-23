package teamcomm.gui;

import data.Teams;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import teamcomm.Main;
import teamcomm.data.RobotData;
import teamcomm.data.RobotState;

/**
 * @author Felix Thielke
 */
public class RobotView extends JFrame implements Runnable {

    private static final long serialVersionUID = 6549981924840180076L;
    private final JPanel emptyPane = new JPanel(new BorderLayout());
    private final JScrollPane singlePane;
    private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
    private final JPanel[] teamPanels = new JPanel[]{new JPanel(), new JPanel(), new JPanel()};
    private final Map<String, JPanel> robotPanels = new HashMap<String, JPanel>();

    public RobotView() {
        super("Robots");

        // Setup window
        setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                Main.shutdown();
            }
        });
        setMinimumSize(new Dimension(600, 500));
        setMaximumSize(new Dimension(2000, 500));

        // Display default text
        emptyPane.add(new JLabel("No Robots found", JLabel.CENTER));
        setContentPane(emptyPane);

        // Setup the panels to display
        tabbedPane.add(new JScrollPane(teamPanels[0], JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        tabbedPane.add(new JScrollPane(teamPanels[1], JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        for (int i = 0; i < 3; i++) {
            teamPanels[i].setLayout(new BoxLayout(teamPanels[i], BoxLayout.X_AXIS));
        }
        singlePane = new JScrollPane(teamPanels[2], JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Display window
        pack();
        setVisible(true);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            RobotData.getInstance().lockForReading();
            updateView();
            RobotData.getInstance().unlockForReading();
            try {
                Thread.sleep(1000 / 4);
            } catch (InterruptedException ex) {
            }
        }
    }

    private void updateView() {
        final int[] teamNumbers = RobotData.getInstance().getTeamNumbers();
        final Iterator<RobotState> otherRobots = RobotData.getInstance().getOtherRobots();
        if (teamNumbers == null) {
            if (otherRobots.hasNext()) {
                if (getContentPane() != singlePane) {
                    setContentPane(singlePane);
                    teamPanels[2].revalidate();
                }
            } else {
                if (getContentPane() != emptyPane) {
                    setContentPane(emptyPane);
                    emptyPane.revalidate();
                }
            }
        } else {
            if (getContentPane() != tabbedPane) {
                setContentPane(tabbedPane);
                tabbedPane.setTitleAt(0, Teams.getNames(false)[teamNumbers[0]]);
                tabbedPane.setTitleAt(1, Teams.getNames(false)[teamNumbers[1]]);
                if (tabbedPane.getTabCount() == 2 && otherRobots.hasNext()) {
                    tabbedPane.addTab("Other Robots", teamPanels[2]);
                } else if (tabbedPane.getTabCount() == 3 && !otherRobots.hasNext()) {
                    tabbedPane.remove(2);
                }
            }
        }

        for (int team = 0; team < 3; team++) {
            final Iterator<RobotState> robots;
            if (team < 2) {
                if (teamNumbers == null) {
                    continue;
                }
                robots = RobotData.getInstance().getRobotsForTeam(team);
            } else {
                robots = otherRobots;
            }

            int i = 0;
            while (robots.hasNext()) {
                final RobotState robot = robots.next();

                JPanel panel = robotPanels.get(robot.getAddress());
                if (panel == null) {
                    panel = createRobotPanel(robot);
                    robotPanels.put(robot.getAddress(), panel);
                } else {
                    updateRobotPanel(panel, robot);
                }

                if (teamPanels[team].getComponentCount() <= i) {
                    teamPanels[team].add(panel);
                    panel.revalidate();
                } else if (panel != teamPanels[team].getComponent(i)) {
                    teamPanels[team].remove(panel);
                    teamPanels[team].add(panel, i);
                    panel.revalidate();
                }

                i++;
            }
        }

        repaint();
    }

    private JPanel createRobotPanel(final RobotState robot) {
        final DecimalFormat df = new DecimalFormat("#.#####");
        final JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createLineBorder(Color.black));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(150, 400));
        panel.setMinimumSize(new Dimension(150, 400));
        panel.setMaximumSize(new Dimension(150, 400));

        panel.add(new JLabel("Player no: " + robot.getLastMessage().playerNum, JLabel.LEFT));
        panel.add(new JLabel("IP: " + robot.getAddress(), JLabel.LEFT));
        panel.add(new JLabel("Messages: " + robot.getMessageCount(), JLabel.LEFT));
        panel.add(new JLabel("Per second: " + df.format(robot.getMessagesPerSecond()), JLabel.LEFT));
        panel.add(new JLabel("Illegal: " + robot.getIllegalMessageCount(), JLabel.LEFT));
        panel.add(new JLabel(" ", JLabel.LEFT));
        panel.add(new JLabel(robot.getLastMessage().fallen ? "fallen" : "upright", JLabel.LEFT));
        panel.add(new JLabel("Pos.X: " + df.format(robot.getLastMessage().pose[0]), JLabel.LEFT));
        panel.add(new JLabel("Pos.Y: " + df.format(robot.getLastMessage().pose[1]), JLabel.LEFT));
        panel.add(new JLabel("Pos.T: " + df.format(robot.getLastMessage().pose[2]), JLabel.LEFT));
        panel.add(new JLabel("Target.X: " + df.format(robot.getLastMessage().walkingTo[0]), JLabel.LEFT));
        panel.add(new JLabel("Target.Y: " + df.format(robot.getLastMessage().walkingTo[1]), JLabel.LEFT));
        panel.add(new JLabel("Shot.X: " + df.format(robot.getLastMessage().shootingTo[0]), JLabel.LEFT));
        panel.add(new JLabel("Shot.Y: " + df.format(robot.getLastMessage().shootingTo[1]), JLabel.LEFT));
        panel.add(new JLabel("BallRel.X: " + df.format(robot.getLastMessage().ball[0]), JLabel.LEFT));
        panel.add(new JLabel("BallRel.Y: " + df.format(robot.getLastMessage().ball[1]), JLabel.LEFT));
        panel.add(new JLabel("BallVel.X: " + df.format(robot.getLastMessage().ballVel[0]), JLabel.LEFT));
        panel.add(new JLabel("BallVel.Y: " + df.format(robot.getLastMessage().ballVel[1]), JLabel.LEFT));
        panel.add(new JLabel("BallAge: " + robot.getLastMessage().ballAge, JLabel.LEFT));

        return panel;
    }

    private void updateRobotPanel(final JPanel panel, final RobotState robot) {
        final DecimalFormat df = new DecimalFormat("#.#####");
        ((JLabel)panel.getComponent(0)).setText("Player no: " + robot.getLastMessage().playerNum);
        ((JLabel)panel.getComponent(1)).setText("IP: " + robot.getAddress());
        ((JLabel)panel.getComponent(2)).setText("Messages: " + robot.getMessageCount());
        ((JLabel)panel.getComponent(3)).setText("Per second: " + df.format(robot.getMessagesPerSecond()));
        ((JLabel)panel.getComponent(4)).setText("Illegal: " + robot.getIllegalMessageCount());
        ((JLabel)panel.getComponent(6)).setText(robot.getLastMessage().fallen ? "fallen" : "upright");
        ((JLabel)panel.getComponent(7)).setText("Pos.X: " + df.format(robot.getLastMessage().pose[0]));
        ((JLabel)panel.getComponent(8)).setText("Pos.Y: " + df.format(robot.getLastMessage().pose[1]));
        ((JLabel)panel.getComponent(9)).setText("Pos.T: " + df.format(robot.getLastMessage().pose[2]));
        ((JLabel)panel.getComponent(10)).setText("Target.X: " + df.format(robot.getLastMessage().walkingTo[0]));
        ((JLabel)panel.getComponent(11)).setText("Target.Y: " + df.format(robot.getLastMessage().walkingTo[1]));
        ((JLabel)panel.getComponent(12)).setText("Shot.X: " + df.format(robot.getLastMessage().shootingTo[0]));
        ((JLabel)panel.getComponent(13)).setText("Shot.Y: " + df.format(robot.getLastMessage().shootingTo[1]));
        ((JLabel)panel.getComponent(14)).setText("BallRel.X: " + df.format(robot.getLastMessage().ball[0]));
        ((JLabel)panel.getComponent(15)).setText("BallRel.Y: " + df.format(robot.getLastMessage().ball[1]));
        ((JLabel)panel.getComponent(16)).setText("BallVel.X: " + df.format(robot.getLastMessage().ballVel[0]));
        ((JLabel)panel.getComponent(17)).setText("BallVel.Y: " + df.format(robot.getLastMessage().ballVel[1]));
        ((JLabel)panel.getComponent(18)).setText("BallAge: " + robot.getLastMessage().ballAge);
    }

}
