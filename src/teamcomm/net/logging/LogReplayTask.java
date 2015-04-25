package teamcomm.net.logging;

import common.Log;
import data.GameControlData;
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.LinkedList;
import java.util.Queue;
import javax.swing.event.EventListenerList;
import teamcomm.data.GameState;
import teamcomm.net.SPLStandardMessagePackage;
import teamcomm.net.SPLStandardMessageReceiver;

/**
 *
 * @author Felix Thielke
 */
class LogReplayTask implements Runnable {

    public static final int PLAYBACK_TASK_DELAY = 50; // ms

    private static class LoggedObject {

        public final long time;
        public final Object object;
        public final int typeid;

        public LoggedObject(final long time, final Object object) {
            this.time = time;
            this.object = object;
            this.typeid = -1;
        }

        public LoggedObject(final long time, final int typeid) {
            this.time = time;
            this.object = null;
            this.typeid = typeid;
        }
    }

    private final EventListenerList listeners;

    private final Queue<LoggedObject> prevObjects = new LinkedList<LoggedObject>();
    private final Queue<LoggedObject> nextObjects = new LinkedList<LoggedObject>();
    private LoggedObject curObject;

    private ObjectInputStream stream;

    private long currentPosition = 0;
    private float playbackFactor = 0;

    public LogReplayTask(final File logfile, final EventListenerList listeners) throws IOException {
        this.listeners = listeners;
        stream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(logfile)));
        next();
    }

    public void close() {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ex) {
            }
            stream = null;
        }
    }

    public boolean isPaused() {
        synchronized (this) {
            return playbackFactor == 0;
        }
    }

    public void setPlaybackSpeed(final float factor) {
        synchronized (this) {
            playbackFactor = factor;
        }
    }

    @Override
    public void run() {
        if (curObject != null) {
            final boolean forward;
            synchronized (this) {
                if (playbackFactor == 0) {
                    return;
                } else {
                    forward = playbackFactor > 0;
                }
                currentPosition += (long) ((float) PLAYBACK_TASK_DELAY * playbackFactor);
            }

            if (forward) {
                while (currentPosition >= curObject.time) {
                    handleObject(curObject);
                    next();
                    if (curObject == null) {
                        synchronized (this) {
                            playbackFactor = 0;
                        }
                        break;
                    }
                }
            } else {
                while (currentPosition <= curObject.time) {
                    handleObject(curObject);
                    prev();
                    if (curObject == null) {
                        synchronized (this) {
                            playbackFactor = 0;
                        }
                        break;
                    }
                }
            }

            final LogReplayEvent e = new LogReplayEvent(this, currentPosition, isPaused(), prevObjects.isEmpty(), nextObjects.isEmpty());
            for (final LogReplayEventListener listener : listeners.getListeners(LogReplayEventListener.class)) {
                listener.loggingStatus(e);
            }
        }
    }

    private void prev() {
        if (curObject != null) {
            nextObjects.add(curObject);
        }
        curObject = prevObjects.poll();
    }

    private void next() {
        if (curObject != null) {
            prevObjects.add(curObject);
        }
        curObject = nextObjects.poll();
        if (curObject == null && stream != null) {
            try {
                final long time = stream.readLong();
                if (stream.readBoolean()) {
                    curObject = new LoggedObject(time, stream.readObject());
                } else {
                    curObject = new LoggedObject(time, stream.readInt());
                }
            } catch (EOFException e) {
                try {
                    stream.close();
                } catch (IOException ex) {
                }
                stream = null;
            } catch (IOException e) {
                Log.error("error while reading log file: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                try {
                    stream.close();
                } catch (IOException ex) {
                }
                stream = null;
            } catch (ClassNotFoundException e) {
                Log.error("error while reading log file: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                try {
                    stream.close();
                } catch (IOException ex) {
                }
                stream = null;
            }
        }
    }

    private void handleObject(final LoggedObject obj) {
        if (obj.object != null) {
            if (obj.object instanceof SPLStandardMessagePackage) {
                SPLStandardMessageReceiver.getInstance().addToPackageQueue((SPLStandardMessagePackage) obj.object);
            } else if (obj.object instanceof GameControlData) {
                GameState.getInstance().updateGameData((GameControlData) obj.object);
            }
        } else {
            switch (obj.typeid) {
                case 1:
                    GameState.getInstance().updateGameData(null);
                    break;
            }
        }
    }
}
