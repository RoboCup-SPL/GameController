package teamcomm.data.event;

import java.util.EventListener;

/**
 *
 * @author Felix Thielke
 */
public interface TeamEventListener extends EventListener {

    public void teamChanged(TeamEvent e);
}
