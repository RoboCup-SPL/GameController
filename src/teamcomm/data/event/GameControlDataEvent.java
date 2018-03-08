package teamcomm.data.event;

import data.GameControlData;
import java.util.EventObject;

/**
 * Class for events being sent when data from the GameController was received.
 *
 * @author Felix Thielke
 */
public class GameControlDataEvent extends EventObject {

    private static final long serialVersionUID = -2485519981679815554L;
    /**
     * The data that was received from the GameController.
     */
    public final GameControlData data;

    /**
     * Constructor.
     *
     * @param source source of this event
     * @param data
     */
    public GameControlDataEvent(final Object source, final GameControlData data) {
        super(source);

        this.data = data;
    }
}
