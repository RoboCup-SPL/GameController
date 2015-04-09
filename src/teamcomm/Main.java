package teamcomm;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.media.opengl.GLProfile;
import javax.swing.JOptionPane;
import teamcomm.gui.RobotView;
import teamcomm.net.GameControlDataReceiver;
import teamcomm.net.SPLStandardMessageReceiver;

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
        SPLStandardMessageReceiver receiver = null;

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

        // Determine logfile
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-S");
        final File logDir = new File(LOG_DIRECTORY);
        final File logFile;
        if (!logDir.exists() && !logDir.mkdirs()) {
            logFile = new File("teamcomm_" + df.format(new Date(System.currentTimeMillis())) + ".log");
        } else {
            logFile = new File(logDir, "teamcomm_" + df.format(new Date(System.currentTimeMillis())) + ".log");
        }

        // Initialize listeners for robots
        try {
            receiver = new SPLStandardMessageReceiver(logFile);
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
        SPLStandardMessageReceiver.setInstance(receiver);

        // Initialize robot view part of the GUI
        final Thread robotView = new Thread(new RobotView());
        robotView.setName("GUI");

        // Start threads
        gcDataReceiver.start();
        receiver.start();
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

        // Shutdown threads
        receiver.interrupt();
        gcDataReceiver.interrupt();
        robotView.interrupt();

        try {
            gcDataReceiver.join(1000);
            robotView.join(1000);
            receiver.join(100);
        } catch (InterruptedException ex) {

        }

        if (logFile.length() <= 4) {
            logFile.delete();
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
