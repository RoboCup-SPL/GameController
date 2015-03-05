package teamcomm.gui;

import data.Teams;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import teamcomm.Main;
import teamcomm.data.RobotData;
import teamcomm.data.RobotState;

/**
 * @author Felix Thielke
 */
public class RobotView extends JFrame implements Runnable {

    private static final long serialVersionUID = 6549981924840180076L;

    private static final int ROBOTPANEL_W = 150;
    private static final int ROBOTPANEL_H = 105;

    private static final Map<Integer, ImageIcon> logos = new HashMap<Integer, ImageIcon>();

    private final FieldView fieldView = new FieldView();
    private final JPanel[] teamPanels = new JPanel[]{new JPanel(), new JPanel(), new JPanel()};
    private final JLabel[] teamLogos = new JLabel[]{new JLabel(), new JLabel()};
    private final Map<String, JPanel> robotPanels = new HashMap<String, JPanel>();
    private final Map<String, JFrame> robotDetailPanels = new HashMap<String, JFrame>();

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

        // Setup team panels
        teamPanels[0].setLayout(new BoxLayout(teamPanels[0], BoxLayout.Y_AXIS));
        teamPanels[1].setLayout(new BoxLayout(teamPanels[1], BoxLayout.Y_AXIS));
        teamPanels[2].setLayout(new BoxLayout(teamPanels[2], BoxLayout.X_AXIS));
        /*teamPanels[0].setLayout(new GridLayout(6, 1, 0, 5));
         teamPanels[1].setLayout(new GridLayout(6, 1, 0, 5));*/

        // Setup team logos
        /*final JPanel logoLeft = new JPanel();
         logoLeft.setPreferredSize(new Dimension(ROBOTPANEL_W, ROBOTPANEL_H));
         logoLeft.setMinimumSize(new Dimension(ROBOTPANEL_W, ROBOTPANEL_H));
         logoLeft.setMaximumSize(new Dimension(ROBOTPANEL_W, ROBOTPANEL_H));
         logoLeft.setSize(ROBOTPANEL_W, ROBOTPANEL_H);
         logoLeft.add(teamLogos[0]);*/
        teamPanels[0].add(teamLogos[0]);
        /*final JPanel logoRight = new JPanel();
         logoRight.setPreferredSize(new Dimension(ROBOTPANEL_W, ROBOTPANEL_H));
         logoRight.setMinimumSize(new Dimension(ROBOTPANEL_W, ROBOTPANEL_H));
         logoRight.setMaximumSize(new Dimension(ROBOTPANEL_W, ROBOTPANEL_H));
         logoRight.add(teamLogos[1]);*/
        teamPanels[1].add(teamLogos[1]);

        // Setup content pane
        final JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(teamPanels[0], BorderLayout.WEST);
        contentPane.add(teamPanels[1], BorderLayout.EAST);
        contentPane.add(teamPanels[2], BorderLayout.SOUTH);
        contentPane.add(fieldView.getCanvas(), BorderLayout.CENTER);
        setContentPane(contentPane);

        // Display window
        setPreferredSize(new Dimension(800, 600));
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

        // Terminate 3D rendering
        fieldView.terminate();
    }

    private void updateView() {
        final int[] teamNumbers = RobotData.getInstance().getTeamNumbers();

        for (int team = 0; team < 3; team++) {
            final Iterator<RobotState> robots;
            if (team < 2) {
                if (teamNumbers == null) {
                    teamLogos[team].setIcon(null);
                } else {
                    teamLogos[team].setIcon(getTeamIcon(teamNumbers[team]));
                }
                robots = RobotData.getInstance().getRobotsForTeam(team);
            } else {
                robots = RobotData.getInstance().getOtherRobots();
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

                JFrame detailPanel = robotDetailPanels.get(robot.getAddress());
                if (detailPanel == null) {
                    detailPanel = createRobotDetailPanel(robot, panel);
                    robotDetailPanels.put(robot.getAddress(), detailPanel);
                } else {
                    updateRobotDetailPanel(detailPanel, robot);
                }

                if (teamPanels[team].getComponentCount() <= i + (team < 2 ? 1 : 0)) {
                    teamPanels[team].add(panel);
                    panel.revalidate();
                } else if (panel != teamPanels[team].getComponent(i + (team < 2 ? 1 : 0))) {
                    teamPanels[team].remove(panel);
                    teamPanels[team].add(panel, i + (team < 2 ? 1 : 0));
                    panel.revalidate();
                }

                i++;
            }
        }

        repaint();
    }

    private ImageIcon getTeamIcon(final int team) {
        ImageIcon icon = logos.get(team);
        if (icon != null) {
            return icon;
        }

        icon = new ImageIcon(Teams.getIcon(team));
        float scaleFactor;
        if (icon.getImage().getWidth(null) > icon.getImage().getHeight(null)) {
            scaleFactor = (float) (ROBOTPANEL_W) / icon.getImage().getWidth(null);
        } else {
            scaleFactor = (float) (ROBOTPANEL_H) / icon.getImage().getHeight(null);
        }

        // getScaledInstance/SCALE_SMOOTH does not work with all color models, so we need to convert image
        BufferedImage image = (BufferedImage) icon.getImage();
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            BufferedImage temp = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = temp.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            image = temp;
        }

        icon.setImage(image.getScaledInstance(
                (int) (icon.getImage().getWidth(null) * scaleFactor),
                (int) (icon.getImage().getHeight(null) * scaleFactor),
                Image.SCALE_SMOOTH));

        logos.put(team, icon);

        return icon;
    }

    private JPanel createRobotPanel(final RobotState robot) {
        final DecimalFormat df = new DecimalFormat("#.#####");
        final JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), robot.getAddress(), TitledBorder.CENTER, TitledBorder.TOP));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setMinimumSize(new Dimension(ROBOTPANEL_W, ROBOTPANEL_H));
        panel.setMaximumSize(new Dimension(ROBOTPANEL_W, ROBOTPANEL_H));
        panel.setPreferredSize(new Dimension(ROBOTPANEL_W, ROBOTPANEL_H));

        panel.add(new JLabel("Player no: " + (robot.getLastMessage() == null ? "?" : robot.getLastMessage().playerNum), JLabel.LEFT));
        panel.add(new JLabel("Messages: " + robot.getMessageCount(), JLabel.LEFT));
        panel.add(new JLabel("Current mps: " + df.format(robot.getRecentMessageCount()), JLabel.LEFT));
        panel.add(new JLabel("Average mps: " + df.format(robot.getMessagesPerSecond()), JLabel.LEFT));
        panel.add(new JLabel("Illegal: " + robot.getIllegalMessageCount() + " (" + (int) (robot.getIllegalMessageRatio() * 100) + "%)", JLabel.LEFT));

        return panel;
    }

    private void updateRobotPanel(final JPanel panel, final RobotState robot) {
        if (robot.getLastMessage() == null) {
            return;
        }

        final DecimalFormat df = new DecimalFormat("#.#####");
        ((JLabel) panel.getComponent(0)).setText("Player no: " + robot.getLastMessage().playerNum);
        ((JLabel) panel.getComponent(1)).setText("Messages: " + robot.getMessageCount());
        ((JLabel) panel.getComponent(2)).setText("Current mps: " + df.format(robot.getRecentMessageCount()));
        ((JLabel) panel.getComponent(3)).setText("Average mps: " + df.format(robot.getMessagesPerSecond()));
        ((JLabel) panel.getComponent(4)).setText("Illegal: " + robot.getIllegalMessageCount() + " (" + (int) (robot.getIllegalMessageRatio() * 100) + "%)");
    }

    private JFrame createRobotDetailPanel(final RobotState robot, final JPanel anchor) {
        final DecimalFormat df = new DecimalFormat("#.#####");
        final JPanel panel = new JPanel();
        final JFrame frame = new JFrame(robot.getAddress());
        frame.setLocationByPlatform(true);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setContentPane(panel);
        anchor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    frame.setVisible(true);
                }
            }
        });

        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), robot.getAddress(), TitledBorder.CENTER, TitledBorder.TOP));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel((robot.getLastMessage() == null ? "Unknown" : (robot.getLastMessage().teamColor == 0 ? "Blue" : "Red")) + " Team", JLabel.LEFT));
        panel.add(new JLabel("Player no: " + (robot.getLastMessage() == null ? "?" : robot.getLastMessage().playerNum), JLabel.LEFT));
        panel.add(new JLabel("Messages: " + robot.getMessageCount(), JLabel.LEFT));
        panel.add(new JLabel("Per second: " + df.format(robot.getMessagesPerSecond()), JLabel.LEFT));
        panel.add(new JLabel("Illegal: " + robot.getIllegalMessageCount() + " (" + (int) (robot.getIllegalMessageRatio() * 100) + "%)", JLabel.LEFT));
        panel.add(new JLabel(" ", JLabel.LEFT));
        panel.add(new JLabel(robot.getLastMessage() == null ? "?" : (robot.getLastMessage().fallen ? "fallen" : "upright"), JLabel.LEFT));
        panel.add(new JLabel("Pos.X: " + (robot.getLastMessage() == null ? "?" : df.format(robot.getLastMessage().pose[0])), JLabel.LEFT));
        panel.add(new JLabel("Pos.Y: " + (robot.getLastMessage() == null ? "?" : df.format(robot.getLastMessage().pose[1])), JLabel.LEFT));
        panel.add(new JLabel("Pos.T: " + (robot.getLastMessage() == null ? "?" : df.format(robot.getLastMessage().pose[2])), JLabel.LEFT));
        panel.add(new JLabel("Target.X: " + (robot.getLastMessage() == null ? "?" : df.format(robot.getLastMessage().walkingTo[0])), JLabel.LEFT));
        panel.add(new JLabel("Target.Y: " + (robot.getLastMessage() == null ? "?" : df.format(robot.getLastMessage().walkingTo[1])), JLabel.LEFT));
        panel.add(new JLabel("Shot.X: " + (robot.getLastMessage() == null ? "?" : df.format(robot.getLastMessage().shootingTo[0])), JLabel.LEFT));
        panel.add(new JLabel("Shot.Y: " + (robot.getLastMessage() == null ? "?" : df.format(robot.getLastMessage().shootingTo[1])), JLabel.LEFT));
        panel.add(new JLabel("BallRel.X: " + (robot.getLastMessage() == null ? "?" : df.format(robot.getLastMessage().ball[0])), JLabel.LEFT));
        panel.add(new JLabel("BallRel.Y: " + (robot.getLastMessage() == null ? "?" : df.format(robot.getLastMessage().ball[1])), JLabel.LEFT));
        panel.add(new JLabel("BallVel.X: " + (robot.getLastMessage() == null ? "?" : df.format(robot.getLastMessage().ballVel[0])), JLabel.LEFT));
        panel.add(new JLabel("BallVel.Y: " + (robot.getLastMessage() == null ? "?" : df.format(robot.getLastMessage().ballVel[1])), JLabel.LEFT));
        panel.add(new JLabel("BallAge: " + (robot.getLastMessage() == null ? "?" : robot.getLastMessage().ballAge), JLabel.LEFT));
        panel.add(new JLabel(" ", JLabel.LEFT));
        panel.add(new JLabel("Additional data: " + (robot.getLastMessage() == null ? "?" : robot.getLastMessage().data.length + "B"), JLabel.LEFT));

        frame.pack();
        frame.setResizable(false);
        return frame;
    }

    private void updateRobotDetailPanel(final JFrame frame, final RobotState robot) {
        if (robot.getLastMessage() == null) {
            return;
        }

        final JPanel panel = (JPanel) frame.getContentPane();
        final DecimalFormat df = new DecimalFormat("#.#####");
        ((JLabel) panel.getComponent(0)).setText((robot.getLastMessage().teamColor == 0 ? "Blue" : "Red") + " Team");
        ((JLabel) panel.getComponent(1)).setText("Player no: " + robot.getLastMessage().playerNum);
        ((JLabel) panel.getComponent(2)).setText("Messages: " + robot.getMessageCount());
        ((JLabel) panel.getComponent(3)).setText("Per second: " + df.format(robot.getMessagesPerSecond()));
        ((JLabel) panel.getComponent(4)).setText("Illegal: " + robot.getIllegalMessageCount() + " (" + (int) (robot.getIllegalMessageRatio() * 100) + "%)");
        ((JLabel) panel.getComponent(6)).setText(robot.getLastMessage().fallen ? "fallen" : "upright");
        ((JLabel) panel.getComponent(7)).setText("Pos.X: " + df.format(robot.getLastMessage().pose[0]));
        ((JLabel) panel.getComponent(8)).setText("Pos.Y: " + df.format(robot.getLastMessage().pose[1]));
        ((JLabel) panel.getComponent(9)).setText("Pos.T: " + df.format(robot.getLastMessage().pose[2]));
        ((JLabel) panel.getComponent(10)).setText("Target.X: " + df.format(robot.getLastMessage().walkingTo[0]));
        ((JLabel) panel.getComponent(11)).setText("Target.Y: " + df.format(robot.getLastMessage().walkingTo[1]));
        ((JLabel) panel.getComponent(12)).setText("Shot.X: " + df.format(robot.getLastMessage().shootingTo[0]));
        ((JLabel) panel.getComponent(13)).setText("Shot.Y: " + df.format(robot.getLastMessage().shootingTo[1]));
        ((JLabel) panel.getComponent(14)).setText("BallRel.X: " + df.format(robot.getLastMessage().ball[0]));
        ((JLabel) panel.getComponent(15)).setText("BallRel.Y: " + df.format(robot.getLastMessage().ball[1]));
        ((JLabel) panel.getComponent(16)).setText("BallVel.X: " + df.format(robot.getLastMessage().ballVel[0]));
        ((JLabel) panel.getComponent(17)).setText("BallVel.Y: " + df.format(robot.getLastMessage().ballVel[1]));
        ((JLabel) panel.getComponent(18)).setText("BallAge: " + robot.getLastMessage().ballAge);
        ((JLabel) panel.getComponent(20)).setText("Additional data: " + robot.getLastMessage().data.length + "B");
    }

}
