package teamcomm.data.event;

import java.util.EventObject;

/**
 *
 * @author Felix Thielke
 */
public class RobotStateEvent extends EventObject {
    private static final long serialVersionUID = 5732929692893474554L;

    public RobotStateEvent(Object source) {
        super(source);
    }
}
