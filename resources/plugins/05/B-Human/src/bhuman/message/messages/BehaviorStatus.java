package bhuman.message.messages;

import bhuman.message.data.Role;
import bhuman.message.Message;
import bhuman.message.data.EnumReader;
import bhuman.message.data.NativeReaders;
import bhuman.message.data.SimpleStreamReader;
import java.nio.ByteBuffer;

/**
 * Class for the BehaviourStatus message.
 *
 * @author Felix Thielke
 */
public class BehaviorStatus extends Message<BehaviorStatus> implements SimpleStreamReader<BehaviorStatus> {

    public static enum Activity {

        unknown,
        duel,
        dribble,
        dribbleDuel,
        searchForBall,
        goToBall,
        takingPosition,
        kick,
        guardGoal,
        catchBall,
        waitingForPass,
        standAndWait,
        passing,
        gettingUp,
        turn,
        zeroValidityTurn,
        checkMirroredBall,
        walkNextToKeeper
    }

    public Role role;
    /**
     * What is the robot doing in general?
     */
    public Activity activity;
    public float estimatedTimeToReachBall;
    public int passTarget;

    @Override
    public int getStreamedSize() {
        return new Role().getStreamedSize()
                + new EnumReader<>(Activity.class).getStreamedSize()
                + NativeReaders.floatReader.getStreamedSize()
                + NativeReaders.intReader.getStreamedSize();
    }

    @Override
    public BehaviorStatus read(final ByteBuffer stream) {
        role = new Role().read(stream);
        activity = new EnumReader<>(Activity.class).read(stream);
        estimatedTimeToReachBall = NativeReaders.floatReader.read(stream);
        passTarget = NativeReaders.intReader.read(stream);
        return this;
    }

}
