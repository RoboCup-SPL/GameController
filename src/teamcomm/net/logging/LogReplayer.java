package teamcomm.net.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.event.EventListenerList;
import teamcomm.data.GameState;
import teamcomm.net.SPLStandardMessageReceiver;

/**
 *
 * @author Felix Thielke
 */
public class LogReplayer {

    private static final LogReplayer instance = new LogReplayer();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> taskHandle;

    private LogReplayTask task;

    private final EventListenerList listeners = new EventListenerList();

    public static LogReplayer getInstance() {
        return instance;
    }

    public void open(final File logfile) throws FileNotFoundException, IOException {
        // Close currently opened log
        if (task != null && taskHandle != null) {
            taskHandle.cancel(false);
            task.close();
            for (final LogReplayEventListener listener : listeners.getListeners(LogReplayEventListener.class)) {
                listener.logReplayEnded();
            }
        }

        // Drain package queue of SPLStandardMessageReceiver
        SPLStandardMessageReceiver.getInstance().clearPackageQueue();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
        }

        // Reset GameState
        GameState.getInstance().reset();

        // Open new log
        task = new LogReplayTask(logfile, listeners);
        taskHandle = scheduler.scheduleAtFixedRate(task, LogReplayTask.PLAYBACK_TASK_DELAY, LogReplayTask.PLAYBACK_TASK_DELAY, TimeUnit.MILLISECONDS);
        for (final LogReplayEventListener listener : listeners.getListeners(LogReplayEventListener.class)) {
            listener.logReplayStarted();
        }
    }

    public boolean isReplaying() {
        return task != null;
    }

    public boolean isPaused() {
        return task == null || task.isPaused();
    }

    public void setPlaybackSpeed(final float factor) {
        if (task != null) {
            task.setPlaybackSpeed(factor);
        }
    }

    public void close() {
        if (task != null) {
            // Close currently opened log
            taskHandle.cancel(false);
            task.close();
            task = null;
            taskHandle = null;

            // Send close event
            for (final LogReplayEventListener listener : listeners.getListeners(LogReplayEventListener.class)) {
                listener.logReplayEnded();
            }

            // Drain package queue of SPLStandardMessageReceiver
            SPLStandardMessageReceiver.getInstance().clearPackageQueue();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }

            // Reset GameState
            GameState.getInstance().reset();
        }
    }

    public void addListener(final LogReplayEventListener listener) {
        listeners.add(LogReplayEventListener.class, listener);
    }

    public void removeListener(final LogReplayEventListener listener) {
        listeners.remove(LogReplayEventListener.class, listener);
    }
}
