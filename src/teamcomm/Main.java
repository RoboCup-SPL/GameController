package teamcomm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.media.opengl.GLProfile;
import javax.swing.JOptionPane;
import teamcomm.gui.RobotView;
import teamcomm.net.GameControlDataReceiver;
import teamcomm.net.SPLStandardMessageReceiverManager;

/**
 * @author Felix Thielke
 *
 * The team communication monitor starts in this class.
 */
public class Main {

    private final static String LOG_DIRECTORY = "logs_teamcomm";
    private static boolean shutdown = false;
    private static final Object shutdownMutex = new Object();

    /**
     * Startup method of the team communication monitor.
     *
     * @param args This is ignored.
     */
    public static void main(final String[] args) {
        GameControlDataReceiver gcDataReceiver = null;
        SPLStandardMessageReceiverManager receiverManager = null;

        // Initialize the JOGL profile for 3D drawing
        GLProfile.initSingleton();

        // Initialize listener for GameController messages
        try {
            gcDataReceiver = new GameControlDataReceiver();
        } catch (SocketException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error while setting up GameController listener.",
                    "SocketException",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

        // Initialize listeners for robots
        try {
            receiverManager = new SPLStandardMessageReceiverManager();
        } catch (SocketException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error while setting up packet listeners.",
                    "SocketException",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error while setting up packet listeners.",
                    "IOException",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

        // Initialize logging
        ObjectOutputStream logStream = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-S");
        final File logDir = new File(LOG_DIRECTORY);
        try {
            if (!logDir.exists() && !logDir.mkdirs()) {
                logStream = new ObjectOutputStream(new FileOutputStream("teamcomm_" + df.format(new Date(System.currentTimeMillis())) + ".log"));
            } else {
                final File logFile = new File(logDir, "teamcomm_" + df.format(new Date(System.currentTimeMillis())) + ".log");
                logStream = new ObjectOutputStream(new FileOutputStream(logFile));
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error while opening log file.",
                    "IOException",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
        receiverManager.setLogger(logStream);

        // Initialize robot view part of the GUI
        final Thread robotView = new Thread(new RobotView());

        // Start threads
        gcDataReceiver.start();
        receiverManager.start();
        robotView.start();

        // Wait for shutdown
        try {
            synchronized (shutdownMutex) {
                while (!shutdown) {
                    shutdownMutex.wait();
                }
            }
        } catch (InterruptedException ex) {
        }

        receiverManager.setLogger(null);
        try {
            logStream.close();
        } catch (IOException ex) {

        }

        // Shutdown threads
        receiverManager.interrupt();
        gcDataReceiver.interrupt();
        robotView.interrupt();

        try {
            gcDataReceiver.join(1000);
            robotView.join(1000);
            receiverManager.join(100);
        } catch (InterruptedException ex) {

        }

        System.exit(0);
    }

    public static void shutdown() {
        synchronized (shutdownMutex) {
            shutdown = true;
            shutdownMutex.notifyAll();
        }
    }
}
