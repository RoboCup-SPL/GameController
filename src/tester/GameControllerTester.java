package tester;

import common.Log;
import java.net.SocketException;
import teamcomm.net.GameControlDataReceiver;

/**
 * Main class of the GameController tester.
 *
 * @author Felix Thielke
 */
public class GameControllerTester {

    private static boolean shutdown = false;
    private static final Object commandMutex = new Object();

    /**
     * Startup method of the GameController tester.
     *
     * @param args startup arguments; ignored for now.
     */
    public static void main(final String[] args) {
        final MainWindow window = new MainWindow();
        GameControlDataReceiver receiver = null;
        try {
            receiver = new GameControlDataReceiver();
        } catch (SocketException ex) {
            Log.error("Could not setup receiver!");
            System.exit(-1);
        }
        receiver.addListener(window);

        receiver.start();

        // Wait for shutdown
        try {
            synchronized (commandMutex) {
                while (!shutdown) {
                    commandMutex.wait();
                }
            }
        } catch (InterruptedException ex) {
        }

        // Shutdown
        receiver.interrupt();
        try {
            receiver.join(1000);
        } catch (InterruptedException ex) {
        }
        System.exit(0);
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
}
