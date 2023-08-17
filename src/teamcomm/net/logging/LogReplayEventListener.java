package teamcomm.net.logging;

import java.util.EventListener;

/**
 * Interface for listeners for events being sent when the status of replaying a
 * log file changes.
 *
 * @author Felix Thielke
 */
public interface LogReplayEventListener extends EventListener {

    /**
     * Called when the status of replaying a log file changes.
     *
     * @param e event
     */
    void logReplayStatus(LogReplayEvent e);

    /**
     * Called when a log file was opened.
     */
    void logReplayStarted();

    /**
     * Called when a log file was closed.
     */
    void logReplayEnded();
}
