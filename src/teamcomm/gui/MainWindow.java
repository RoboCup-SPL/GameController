package teamcomm.gui;

import common.Log;
import data.Teams;
import java.awt.BorderLayout;
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
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import teamcomm.Main;
import teamcomm.data.RobotData;
import teamcomm.data.RobotState;
import teamcomm.net.SPLStandardMessageReceiver;

/**
 * Class for the main window of the application.
 *
 * @author Felix Thielke
 */
public class MainWindow extends JFrame implements ActionListener {

    private static final long serialVersionUID = 6549981924840180076L;

    private static final Map<Integer, ImageIcon> logos = new HashMap<Integer, ImageIcon>();

    private final Timer timer = new Timer(250, this);

    private final View3D fieldView = new View3D();
    private final JPanel[] teamPanels = new JPanel[]{new JPanel(), new JPanel(), new JPanel()};
    private final JLabel[] teamLogos = new JLabel[]{new JLabel((Icon) null, SwingConstants.CENTER), new JLabel((Icon) null, SwingConstants.CENTER)};
    private final Map<String, RobotPanel> robotPanels = new HashMap<String, RobotPanel>();

    private final JMenuItem[] logMenuItems = new JMenuItem[3];

    /**
     * Constructor.
     */
    public MainWindow() {
        super("TeamCommunicationMonitor");
    }

    private void initialize() {
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
        final Box left = new Box(BoxLayout.Y_AXIS);
        teamPanels[0].setLayout(new GridLayout(10, 1, 0, 5));
        left.add(teamPanels[0]);
        left.add(new Box.Filler(new Dimension(RobotPanel.PANEL_WIDTH, RobotPanel.PANEL_HEIGHT), new Dimension(RobotPanel.PANEL_WIDTH, RobotPanel.PANEL_HEIGHT), new Dimension(RobotPanel.PANEL_WIDTH, 1000)));
        final Box right = new Box(BoxLayout.Y_AXIS);
        teamPanels[1].setLayout(new GridLayout(10, 1, 0, 5));
        right.add(teamPanels[1]);
        right.add(new Box.Filler(new Dimension(RobotPanel.PANEL_WIDTH, RobotPanel.PANEL_HEIGHT), new Dimension(RobotPanel.PANEL_WIDTH, RobotPanel.PANEL_HEIGHT), new Dimension(RobotPanel.PANEL_WIDTH, 1000)));
        final Box bottom = new Box(BoxLayout.X_AXIS);
        bottom.setBorder(new EmptyBorder(0, RobotPanel.PANEL_WIDTH, 0, RobotPanel.PANEL_WIDTH));
        teamPanels[2].setLayout(new BoxLayout(teamPanels[2], BoxLayout.LINE_AXIS));
        bottom.add(teamPanels[2]);

        // Setup team logos
        teamPanels[0].add(teamLogos[0]);
        teamPanels[1].add(teamLogos[1]);

        // Setup content pane
        final JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(left, BorderLayout.WEST);
        contentPane.add(right, BorderLayout.EAST);
        contentPane.add(bottom, BorderLayout.SOUTH);
        contentPane.add(fieldView.getCanvas(), BorderLayout.CENTER);
        setContentPane(contentPane);

        // Add menu bar
        final JMenuBar mb = new JMenuBar();
        mb.add(createFileMenu());
        mb.add(createLogMenu());
        mb.add(createViewMenu());
        setJMenuBar(mb);

        // Display window
        setPreferredSize(new Dimension(1442, 720));
        pack();
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

    public void start() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    // Initialize
                    initialize();

                    // Start Swing timer
                    timer.setRepeats(true);
                    timer.start();
                }
            });
        } catch (InterruptedException ex) {
            Log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        } catch (InvocationTargetException ex) {
            Log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    public void terminate() {
        timer.stop();
        fieldView.terminate();
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (!logMenuItems[0].isEnabled() && !SPLStandardMessageReceiver.getInstance().isReplaying()) {
            logMenuItems[0].setEnabled(true);
            logMenuItems[1].setEnabled(false);
            logMenuItems[2].setEnabled(false);
        }

        RobotData.getInstance().removeInactiveRobots();
        updateView();
    }

    private void updateView() {
        final int[] teamNumbers = RobotData.getInstance().getTeamNumbers();
        final Set<String> robotAddresses = new LinkedHashSet<String>(robotPanels.keySet());

        for (int team = 0; team < 3; team++) {
            final Collection<RobotState> robots;
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
            for (final RobotState robot : robots) {
                robotAddresses.remove(robot.getAddress());

                RobotPanel panel = robotPanels.get(robot.getAddress());
                if (panel == null) {
                    panel = new RobotPanel(robot);
                    robotPanels.put(robot.getAddress(), panel);
                }

                synchronized (teamPanels[team].getTreeLock()) {
                    if (teamPanels[team].getComponentCount() <= i + (team < 2 ? 1 : 0)) {
                        teamPanels[team].add(panel);
                        panel.revalidate();
                    } else if (panel != teamPanels[team].getComponent(i + (team < 2 ? 1 : 0))) {
                        teamPanels[team].remove(panel);
                        teamPanels[team].add(panel, i + (team < 2 ? 1 : 0));
                        panel.revalidate();
                    }
                }

                i++;
            }
        }

        // Remove unused JPanels
        for (final String addr : robotAddresses) {
            final RobotPanel p = robotPanels.remove(addr);
            for (int i = 0; i < 3; i++) {
                synchronized (teamPanels[i].getTreeLock()) {
                    teamPanels[i].remove(p);
                }
            }
            p.dispose();
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
        final float scaleFactor = Math.min((float) (RobotPanel.PANEL_WIDTH) / icon.getImage().getWidth(null), (float) (RobotPanel.PANEL_HEIGHT) / icon.getImage().getHeight(null));

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
}
