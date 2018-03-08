package teamcomm.data.event;

import java.util.EventObject;

/**
 * Class for events being sent when no data from the GameController was received
 * for some time.
 *
 * @author Felix Thielke
 */
public class GameControlDataTimeoutEvent extends EventObject {

    private static final long serialVersionUID = 133951243822340672L;

    /**
     * Constructor.
     *
     * @param source source of this event
     */
    public GameControlDataTimeoutEvent(final Object source) {
        super(source);
    }
}
