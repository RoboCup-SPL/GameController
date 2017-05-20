package teamcomm;

import com.jogamp.opengl.GLProfile;
import common.ApplicationLock;
import java.awt.HeadlessException;
import java.io.IOException;
import java.net.SocketException;
import javax.swing.JOptionPane;
import teamcomm.data.GameState;
import teamcomm.gui.MainWindow;
import teamcomm.gui.View3DGSV;
import teamcomm.net.GameControlDataReceiver;
import teamcomm.net.SPLStandardMessageReceiverTCM;
import teamcomm.net.logging.LogReplayer;
import teamcomm.net.logging.Logger;

/**
 * The team communication monitor starts in this class.
 *
 * @author Felix Thielke
 */
public class TeamCommunicationMonitor {

    private static boolean silentMode = false;
    private static boolean gsvMode = false;
    private static boolean forceWindowed = false;
    private static boolean forceEnablePlugins = false;

    private static boolean shutdown = false;
    private static final Object commandMutex = new Object();

    /**
     * Startup method of the team communication monitor.
     *
     * @param args startup arguments.
     */
    public static void main(final String[] args) {
        GameControlDataReceiver gcDataReceiver = null;
        SPLStandardMessageReceiverTCM receiver = null;

        parseArgs(args);

        // try to acquire the application lock
        final ApplicationLock applicationLock = new ApplicationLock("TeamCommunicationMonitor");
        try {
            if (!applicationLock.acquire()) {
                if (silentMode) {
                    System.out.println("An instance of TeamCommunicationMonitor already exists.");
                } else {
                    JOptionPane.showMessageDialog(null,
                            "An instance of TeamCommunicationMonitor already exists.",
                            "Multiple instances",
                            JOptionPane.WARNING_MESSAGE);
                }
                System.exit(0);
            }
        } catch (IOException | HeadlessException e) {
            if (silentMode) {
                System.out.println("Error while trying to acquire the application lock.");
            } else {
                JOptionPane.showMessageDialog(null,
                        "Error while trying to acquire the application lock.",
                        e.getClass().getSimpleName(),
                        JOptionPane.ERROR_MESSAGE);
            }
            System.exit(-1);
        }

        if (silentMode) {
            System.out.println("Team Communication Monitor was started in silent mode.\nMessages will be received and logged but not displayed.");
        }

        if (!silentMode) {
            // Initialize the JOGL profile for 3D drawing
            GLProfile.initSingleton();
        }

        // Check if the GameController is running
        try {
            final ApplicationLock lock = new ApplicationLock("GameController");
            if (!lock.acquire()) {
                // Do not log messages if a GameController is running on the same system
                Logger.getInstance().disableLogging();
            } else {
                lock.release();
            }
        } catch (IOException e) {
        }

        // Initialize listener for GameController messages
        try {
            gcDataReceiver = new GameControlDataReceiver(true);
        } catch (SocketException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error while setting up GameController listener.",
                    "SocketException",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
        gcDataReceiver.addListener(GameState.getInstance());

        // Initialize listeners for robots
        receiver = SPLStandardMessageReceiverTCM.getInstance();

        // Initialize robot view part of the GUI
        System.setProperty("newt.window.icons", "null,null");
        MainWindow robotView = silentMode || gsvMode ? null : new MainWindow();
        View3DGSV gsvView = silentMode ? null : (gsvMode ? new View3DGSV(forceWindowed) : null);

        // Set initial state for PluginLoader
        if ((silentMode || gsvMode) && !forceEnablePlugins) {
            PluginLoader.getInstance().disablePlugins();
        } else {
            PluginLoader.getInstance().enablePlugins();
        }

        // Start threads
        gcDataReceiver.start();
        receiver.start();

        // Wait for shutdown
        try {
            synchronized (commandMutex) {
                while (!shutdown) {
                    if (!silentMode) {
                        // Handle TCM/GSV mode changes
                        if (gsvMode && gsvView == null) {
                            if (robotView != null) {
                                robotView.terminate();
                                robotView = null;
                            }
                            gsvView = new View3DGSV(forceWindowed);
                            if (!forceEnablePlugins) {
                                PluginLoader.getInstance().disablePlugins();
                            }
                        } else if (!gsvMode && robotView == null) {
                            if (gsvView != null) {
                                gsvView.terminate();
                                gsvView = null;
                            }
                            robotView = new MainWindow();
                            PluginLoader.getInstance().enablePlugins();
                        }
                    }
                    commandMutex.wait();
                }
            }
        } catch (InterruptedException ex) {
        }

        // Write config file
        Config.getInstance().flush();

        // Release the application lock
        try {
            applicationLock.release();
        } catch (IOException e) {
        }

        // Shutdown threads and clean up
        GameState.getInstance().shutdown();
        receiver.interrupt();
        gcDataReceiver.interrupt();
        if (robotView != null) {
            robotView.terminate();
        }
        if (gsvView != null) {
            gsvView.terminate();
        }
        LogReplayer.getInstance().close();
        Logger.getInstance().closeLogfile();

        // Try to join receiver threads
        try {
            gcDataReceiver.join(1000);
            receiver.join(1000);
        } catch (InterruptedException ex) {
        }

        // Force exit
        System.exit(0);
    }

    private static final String ARG_HELP_SHORT = "-h";
    private static final String ARG_HELP = "--help";
    private static final String ARG_SILENT_SHORT = "-s";
    private static final String ARG_SILENT = "--silent";
    private static final String ARG_GSV = "--gsv";
    private static final String ARG_WINDOWED = "--windowed";
    private static final String ARG_WINDOWED_SHORT = "-w";
    private static final String ARG_FORCEPLUGINS = "--forceplugins";
    private static final String ARG_FORCEPLUGINS_SHORT = "-p";

    private static void parseArgs(final String[] args) {
        for (final String arg : args) {
            switch (arg) {
                case ARG_HELP_SHORT:
                case ARG_HELP:
                    System.out.println("Usage: java -jar TeamCommunicationMonitor.jar {options}"
                            + "\n  (-h | --help)                   display help"
                            + "\n  (-s | --silent)                 start in silent mode"
                            + "\n  (--gsv)                         start as GameStateVisualizer"
                            + "\n  (-w | --windowed)               GSV: force windowed mode"
                            + "\n  (-p | --forceplugins)           GVS: force usage of plugins");
                    System.exit(0);
                case ARG_SILENT_SHORT:
                case ARG_SILENT:
                    silentMode = true;
                    break;
                case ARG_WINDOWED_SHORT:
                case ARG_WINDOWED:
                    forceWindowed = true;
                    break;
                case ARG_FORCEPLUGINS_SHORT:
                case ARG_FORCEPLUGINS:
                    forceEnablePlugins = true;
                    break;
                case ARG_GSV:
                    gsvMode = true;
                    break;
            }
        }
    }

    /**
     * Shuts down the program by notifying the main thread.
     */
    public static void shutdown() {
        synchronized (commandMutex) {
            shutdown = true;
            commandMutex.notifyAll();
        }
    }

    /**
     * Switches from TCM to GSV mode by notifying the main thread.
     */
    public static void switchToGSV() {
        synchronized (commandMutex) {
            gsvMode = true;
            commandMutex.notifyAll();
        }
    }

    /**
     * Switches from GSV to TCM mode by notifying the main thread.
     */
    public static void switchToTCM() {
        synchronized (commandMutex) {
            gsvMode = false;
            commandMutex.notifyAll();
        }
    }
}
