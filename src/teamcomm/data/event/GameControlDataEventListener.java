package teamcomm.data.event;

import java.util.EventListener;

/**
 * Interface for listeners for events being sent when data was received from the
 * GameController.
 *
 * @author Felix Thielke
 */
public interface GameControlDataEventListener extends EventListener {

    /**
     * Called when data was received from the GameController.
     *
     * @param e event
     */
    void gameControlDataChanged(final GameControlDataEvent e);

    /**
     * Called when no data was received from the GameController for some time.
     *
     * @param e event
     */
    void gameControlDataTimeout(final GameControlDataTimeoutEvent e);
}
