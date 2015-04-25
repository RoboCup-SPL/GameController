package teamcomm.net.logging;

import common.Log;
import data.GameControlData;
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import teamcomm.data.GameState;
import teamcomm.net.SPLStandardMessagePackage;
import teamcomm.net.SPLStandardMessageReceiver;

/**
 *
 * @author Felix Thielke
 */
public class LogReplayer extends Thread {

    private static LogReplayer replayer;

    private final ObjectInputStream stream;
    private long pausedTimestamp = 0;
    private long pausedOffset = 0;

    private LogReplayer(final File logfile) throws IOException {
        stream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(logfile)));
    }

    private void togglePause() {
        synchronized (this) {
            if (pausedTimestamp == 0) {
                pausedTimestamp = System.currentTimeMillis();
            } else {
                pausedOffset += System.currentTimeMillis() - pausedTimestamp;
                pausedTimestamp = 0;
            }
            notifyAll();
        }
    }

    private boolean isPaused() {
        return pausedTimestamp > 0;
    }

    @Override
    public void run() {
        try {
            final long startTimestamp = System.currentTimeMillis();
            SPLStandardMessageReceiver.getInstance().clearPackageQueue();
            Thread.sleep(100);
            GameState.getInstance().reset();
            while (!isInterrupted()) {
                final long timestamp = stream.readLong();
                while (true) {
                    while (isPaused()) {
                        synchronized (this) {
                            wait();
                        }
                    }
                    final long diff = timestamp - (System.currentTimeMillis() - (startTimestamp + pausedOffset));
                    if (diff <= 0) {
                        break;
                    }
                    Thread.sleep(diff);
                }
                readObject();
            }
        } catch (EOFException e) {
        } catch (IOException e) {
            Log.error("error while reading log file: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            Log.error("error while reading log file: " + e.getMessage());
        } catch (InterruptedException ex) {
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
            }
            SPLStandardMessageReceiver.getInstance().clearPackageQueue();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
            GameState.getInstance().reset();
            replayer = null;
        }
    }

    private void readObject() throws IOException, ClassNotFoundException {
        final boolean notNull = stream.readBoolean();
        if (notNull) {
            final Object o = stream.readObject();
            if (o instanceof SPLStandardMessagePackage) {
                SPLStandardMessageReceiver.getInstance().addToPackageQueue((SPLStandardMessagePackage) o);
            } else if (o instanceof GameControlData) {
                GameState.getInstance().updateGameData((GameControlData) o);
            }
        } else {
            switch (stream.readInt()) {
                case 1:
                    GameState.getInstance().updateGameData(null);
                    break;
            }
        }
    }

    /**
     * Starts replaying the given log file.
     *
     * @param logfile path to the file
     * @throws FileNotFoundException if the file could not be found
     * @throws IOException if an error occured while reading the file
     */
    public static void replayLog(final File logfile) throws FileNotFoundException, IOException {
        if (replayer == null) {
            replayer = new LogReplayer(logfile);
            replayer.start();
        }
    }

    /**
     * Stops replaying a log file.
     */
    public static void stopReplaying() {
        if (replayer != null) {
            replayer.interrupt();
        }
    }

    /**
     * Pauses or unpauses the replaying of a log file.
     */
    public static void toggleReplayPaused() {
        if (replayer != null) {
            replayer.togglePause();
        }
    }

    /**
     * Returns whether the replaying of a log file is paused.
     *
     * @return boolean
     */
    public static boolean isReplayPaused() {
        return replayer == null || replayer.isPaused();
    }

    /**
     * Returns whether a log file is currently being replayed.
     *
     * @return boolean
     */
    public static boolean isReplaying() {
        return replayer != null;
    }
}
