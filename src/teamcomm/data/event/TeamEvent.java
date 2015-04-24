package teamcomm.data.event;

import java.util.Collection;
import java.util.EventObject;
import teamcomm.data.RobotState;

/**
 *
 * @author Felix Thielke
 */
public class TeamEvent extends EventObject {

    private static final long serialVersionUID = 6644539300556793797L;

    public final int side;
    public final int teamNumber;
    public final Collection<RobotState> players;

    public TeamEvent(final Object source, final int side, final int teamNumber, final Collection<RobotState> players) {
        super(source);

        this.side = side;
        this.teamNumber = teamNumber;
        this.players = players;
    }
}
