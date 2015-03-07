package teamcomm.gui;

import data.Teams;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
import teamcomm.Main;
import teamcomm.data.RobotData;
import teamcomm.data.RobotState;
import teamcomm.gui.drawings.Drawing;
import teamcomm.net.SPLStandardMessageReceiver;

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

    private final JMenuItem[] logMenuItems;

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

        // Setup team logos
        teamPanels[0].add(teamLogos[0]);
        teamPanels[1].add(teamLogos[1]);

        // Setup content pane
        final JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(teamPanels[0], BorderLayout.WEST);
        contentPane.add(teamPanels[1], BorderLayout.EAST);
        contentPane.add(teamPanels[2], BorderLayout.SOUTH);
        contentPane.add(fieldView.getCanvas(), BorderLayout.CENTER);
        setContentPane(contentPane);

        // Add menu bar
        final JMenuBar mb = new JMenuBar();

        // File menu
        final JMenu fileMenu = new JMenu("File");
        final JFrame frame = this;
        JMenuItem i = new JMenuItem("Reset");
        i.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RobotData.getInstance().reset();
            }
        });
        i.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        fileMenu.add(i);
        i = new JMenuItem("Exit");
        i.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.shutdown();
                frame.setVisible(false);
            }
        });
        fileMenu.add(i);
        mb.add(fileMenu);

        // Log menu
        final JMenu logMenu = new JMenu("Log");
        final JMenuItem replayOption = new JMenuItem("Replay log file");
        final JMenuItem pauseOption = new JMenuItem("Pause replaying");
        final JMenuItem stopOption = new JMenuItem("Stop replaying");
        replayOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final JFileChooser fc = new JFileChooser(new File(new File(".").getAbsoluteFile(), "logs_teamcomm"));
                int returnVal = fc.showOpenDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        SPLStandardMessageReceiver.getInstance().replayLog(fc.getSelectedFile());
                        replayOption.setEnabled(false);
                        pauseOption.setEnabled(true);
                        stopOption.setEnabled(true);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null,
                                "Error opening log file.",
                                ex.getClass().getSimpleName(),
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        logMenu.add(replayOption);
        pauseOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                SPLStandardMessageReceiver.getInstance().toggleReplayPaused();
                if (SPLStandardMessageReceiver.getInstance().isReplayPaused()) {
                    pauseOption.setText("Continue replaying");
                } else {
                    pauseOption.setText("Pause replaying");
                }
            }
        });
        pauseOption.setEnabled(false);
        logMenu.add(pauseOption);
        stopOption.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                SPLStandardMessageReceiver.getInstance().stopReplaying();
            }
        });
        stopOption.setEnabled(false);
        logMenu.add(stopOption);
        replayOption.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        pauseOption.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        stopOption.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        mb.add(logMenu);
        logMenuItems = new JMenuItem[]{replayOption, pauseOption, stopOption};

        final JMenu drawingsMenu = new JMenu("Drawings");
        for (final Drawing d : fieldView.getDrawings()) {
            final JCheckBoxMenuItem m = new JCheckBoxMenuItem(d.getClass().getSimpleName(), d.isActive());
            m.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(final ItemEvent e) {
                    d.setActive(e.getStateChange() == ItemEvent.SELECTED);
                }
            });
            drawingsMenu.add(m);
        }
        mb.add(drawingsMenu);
        setJMenuBar(mb);

        // Display window
        setPreferredSize(new Dimension(800, 600));
        pack();
        setVisible(true);
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                if (!logMenuItems[0].isEnabled() && !SPLStandardMessageReceiver.getInstance().isReplaying()) {
                    logMenuItems[0].setEnabled(true);
                    logMenuItems[1].setEnabled(false);
                    logMenuItems[2].setEnabled(false);
                }

                RobotData.getInstance().lockForReading();
                updateView();
                RobotData.getInstance().unlockForReading();
                try {
                    Thread.sleep(1000 / 4);
                } catch (InterruptedException ex) {
                }
            }
        } finally {
            // Terminate 3D rendering
            fieldView.terminate();
        }
    }

    private void updateView() {
        final int[] teamNumbers = RobotData.getInstance().getTeamNumbers();

        final Set<String> robotAddresses = new LinkedHashSet<String>(robotPanels.keySet());

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

                robotAddresses.remove(robot.getAddress());

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

        // Remove unused JPanels
        for (final String addr : robotAddresses) {
            robotDetailPanels.remove(addr).dispose();

            final JPanel p = robotPanels.remove(addr);
            for (int i = 0; i < 3; i++) {
                teamPanels[i].remove(p);
            }
        }
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
        panel.add(new JLabel("Activity: " + (robot.getLastMessage() == null ? "?" : robot.getLastMessage().intention.toString()), JLabel.LEFT));
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
        ((JLabel) panel.getComponent(7)).setText("Activity: " + (robot.getLastMessage() == null ? "?" : robot.getLastMessage().intention.toString()));
        ((JLabel) panel.getComponent(8)).setText("Pos.X: " + df.format(robot.getLastMessage().pose[0]));
        ((JLabel) panel.getComponent(9)).setText("Pos.Y: " + df.format(robot.getLastMessage().pose[1]));
        ((JLabel) panel.getComponent(10)).setText("Pos.T: " + df.format(robot.getLastMessage().pose[2]));
        ((JLabel) panel.getComponent(11)).setText("Target.X: " + df.format(robot.getLastMessage().walkingTo[0]));
        ((JLabel) panel.getComponent(12)).setText("Target.Y: " + df.format(robot.getLastMessage().walkingTo[1]));
        ((JLabel) panel.getComponent(13)).setText("Shot.X: " + df.format(robot.getLastMessage().shootingTo[0]));
        ((JLabel) panel.getComponent(14)).setText("Shot.Y: " + df.format(robot.getLastMessage().shootingTo[1]));
        ((JLabel) panel.getComponent(15)).setText("BallRel.X: " + df.format(robot.getLastMessage().ball[0]));
        ((JLabel) panel.getComponent(16)).setText("BallRel.Y: " + df.format(robot.getLastMessage().ball[1]));
        ((JLabel) panel.getComponent(17)).setText("BallVel.X: " + df.format(robot.getLastMessage().ballVel[0]));
        ((JLabel) panel.getComponent(18)).setText("BallVel.Y: " + df.format(robot.getLastMessage().ballVel[1]));
        ((JLabel) panel.getComponent(19)).setText("BallAge: " + robot.getLastMessage().ballAge);
        ((JLabel) panel.getComponent(21)).setText("Additional data: " + robot.getLastMessage().data.length + "B");
    }

}
