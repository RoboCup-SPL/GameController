package teamcomm.net.logging;

import java.util.EventListener;

/**
 *
 * @author Felix Thielke
 */
public interface LogReplayEventListener extends EventListener {

    public void logReplayStatus(LogReplayEvent e);

    public void logReplayStarted();

    public void logReplayEnded();
}
