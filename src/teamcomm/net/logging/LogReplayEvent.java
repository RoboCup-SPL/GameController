package teamcomm.net.logging;

import java.util.EventObject;

/**
 *
 * @author Felix Thielke
 */
public class LogReplayEvent extends EventObject {

    private static final long serialVersionUID = 8348108129211449571L;

    public final long timePosition;
    public final boolean paused;
    public final boolean atBeginning;
    public final boolean atEnd;

    public LogReplayEvent(final Object source, final long timePosition, final boolean paused, final boolean atBeginning, final boolean atEnd) {
        super(source);
        this.timePosition = timePosition;
        this.paused = paused;
        this.atBeginning = atBeginning;
        this.atEnd = atEnd;
    }
}
