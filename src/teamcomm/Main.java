package teamcomm;

import java.net.SocketException;
import javax.swing.JOptionPane;
import teamcomm.net.GameControlDataReceiver;
import teamcomm.net.SPLStandardMessageReceiverManager;

/**
 * @author Felix Thielke
 *
 * The team communication monitor starts in this class.
 */
public class Main {

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
        }

        // Start threads
        gcDataReceiver.start();
        receiverManager.start();

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
        gcDataReceiver.interrupt();
        receiverManager.interrupt();

        try {
            gcDataReceiver.join();
            receiverManager.join();
        } catch (InterruptedException ex) {

        }
    }

    public static void shutdown() {
        synchronized (shutdownMutex) {
            shutdown = true;
            shutdownMutex.notifyAll();
        }
    }
}
