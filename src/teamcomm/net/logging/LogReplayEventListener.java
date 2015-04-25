package teamcomm.net.logging;

import java.util.EventListener;

/**
 *
 * @author Felix Thielke
 */
public interface LogReplayEventListener extends EventListener {

    public void loggingStatus(LogReplayEvent e);
}
