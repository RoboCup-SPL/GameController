package teamcomm.gui;

import data.Teams;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
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
import javax.swing.Box;
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
import teamcomm.net.SPLStandardMessageReceiver;

/**
 * Class for the main window of the application.
 *
 * @author Felix Thielke
 */
public class MainWindow extends JFrame implements Runnable {

    private static final long serialVersionUID = 6549981924840180076L;

    private static final int ROBOTPANEL_W = 175;
    private static final int ROBOTPANEL_H = 105;

    private static final Map<Integer, ImageIcon> logos = new HashMap<Integer, ImageIcon>();

    private final View3D fieldView = new View3D();
    private final JPanel[] teamPanels = new JPanel[]{new JPanel(), new JPanel(), new JPanel()};
    private final JLabel[] teamLogos = new JLabel[]{new JLabel(), new JLabel()};
    private final Map<String, JPanel> robotPanels = new HashMap<String, JPanel>();
    private final Map<String, RobotDetailPanel> robotDetailPanels = new HashMap<String, RobotDetailPanel>();

    private final JMenuItem[] logMenuItems = new JMenuItem[3];

    /**
     * Constructor.
     */
    public MainWindow() {
        super("TeamCommunicationMonitor");

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
        teamPanels[0].setLayout(new GridLayout(7, 1, 0, 5));
        final Box left = new Box(BoxLayout.Y_AXIS);
        left.add(teamPanels[0]);
        left.add(new Box.Filler(new Dimension(0, ROBOTPANEL_H), new Dimension(0, ROBOTPANEL_H), new Dimension(0, 1000)));
        teamPanels[1].setLayout(new BoxLayout(teamPanels[1], BoxLayout.Y_AXIS));
        final Box right = new Box(BoxLayout.Y_AXIS);
        right.add(teamPanels[1]);
        right.add(new Box.Filler(new Dimension(0, ROBOTPANEL_H), new Dimension(0, ROBOTPANEL_H), new Dimension(0, 1000)));
        teamPanels[2].setLayout(new BoxLayout(teamPanels[2], BoxLayout.X_AXIS));

        // Setup team logos
        teamPanels[0].add(teamLogos[0]);
        teamPanels[1].add(teamLogos[1]);

        // Setup content pane
        final JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(left, BorderLayout.WEST);
        contentPane.add(right, BorderLayout.EAST);
        contentPane.add(teamPanels[2], BorderLayout.SOUTH);
        contentPane.add(fieldView.getCanvas(), BorderLayout.CENTER);
        setContentPane(contentPane);

        // Add menu bar
        final JMenuBar mb = new JMenuBar();
        mb.add(createFileMenu());
        mb.add(createLogMenu());
        mb.add(createViewMenu());
        setJMenuBar(mb);

        // Display window
        setPreferredSize(new Dimension(800, 600));
        pack();
        try {
            Thread.sleep(100); // For compatibility with X11 (?)
        } catch (InterruptedException ex) {
        }
        setVisible(true);
    }

    private JMenu createFileMenu() {
        final JMenu fileMenu = new JMenu("File");
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
                setVisible(false);
            }
        });
        fileMenu.add(i);

        return fileMenu;
    }

    private JMenu createLogMenu() {
        final JFrame frame = this;
        final JMenu logMenu = new JMenu("Log");
        logMenuItems[0] = new JMenuItem("Replay log file");
        logMenuItems[1] = new JMenuItem("Pause replaying");
        logMenuItems[2] = new JMenuItem("Stop replaying");
        logMenuItems[0].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final JFileChooser fc = new JFileChooser(new File(new File(".").getAbsoluteFile(), "logs_teamcomm"));
                int returnVal = fc.showOpenDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        SPLStandardMessageReceiver.getInstance().replayLog(fc.getSelectedFile());
                        logMenuItems[0].setEnabled(false);
                        logMenuItems[1].setEnabled(true);
                        logMenuItems[2].setEnabled(true);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null,
                                "Error opening log file.",
                                ex.getClass().getSimpleName(),
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        logMenu.add(logMenuItems[0]);
        logMenuItems[1].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                SPLStandardMessageReceiver.getInstance().toggleReplayPaused();
                if (SPLStandardMessageReceiver.getInstance().isReplayPaused()) {
                    logMenuItems[1].setText("Continue replaying");
                } else {
                    logMenuItems[1].setText("Pause replaying");
                }
            }
        });
        logMenuItems[1].setEnabled(false);
        logMenu.add(logMenuItems[1]);
        logMenuItems[2].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                SPLStandardMessageReceiver.getInstance().stopReplaying();
            }
        });
        logMenuItems[2].setEnabled(false);
        logMenu.add(logMenuItems[2]);
        logMenuItems[0].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        logMenuItems[1].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        logMenuItems[2].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));

        return logMenu;
    }

    private JMenu createViewMenu() {
        final JMenu viewMenu = new JMenu("View");

        // Mirroring
        final JCheckBoxMenuItem mirrorOption = new JCheckBoxMenuItem("Mirror", RobotData.getInstance().isMirrored());
        mirrorOption.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                RobotData.getInstance().setMirrored(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        viewMenu.add(mirrorOption);

        // Drawings
        viewMenu.add(fieldView.getDrawingsMenu());

        return viewMenu;
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

                RobotData.getInstance().removeInactiveRobots();
                RobotData.getInstance().lockForReading();
                updateView();
                RobotData.getInstance().unlockForReading();
                try {
                    Thread.sleep(1000 / 4);
                } catch (InterruptedException ex) {
                }
            }
        } catch (Exception e) {
            // for debug purposes
            e.printStackTrace();
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

                RobotDetailPanel detailPanel = robotDetailPanels.get(robot.getAddress());
                if (detailPanel == null) {
                    detailPanel = new RobotDetailPanel(robot, panel);
                    robotDetailPanels.put(robot.getAddress(), detailPanel);
                } else {
                    detailPanel.update(robot);
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
            robotDetailPanels.remove(addr);

            final JPanel p = robotPanels.remove(addr);
            for (int i = 0; i < 3; i++) {
                teamPanels[i].remove(p);
            }
        }

        // Repaint the team panels
        for (final JPanel panel : teamPanels) {
            panel.repaint();
        }
    }

    private ImageIcon getTeamIcon(final int team) {
        ImageIcon icon = logos.get(team);
        if (icon != null) {
            return icon;
        }

        try {
            icon = new ImageIcon(Teams.getIcon(team));
        } catch (NullPointerException e) {
            return null;
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
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
        panel.add(new JLabel("Illegal: " + robot.getIllegalMessageCount() + " (" + Math.round(robot.getIllegalMessageRatio() * 100.0) + "%)", JLabel.LEFT));
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
        ((JLabel) panel.getComponent(4)).setText("Illegal: " + robot.getIllegalMessageCount() + " (" + Math.round(robot.getIllegalMessageRatio() * 100.0) + "%)");
    }
}
