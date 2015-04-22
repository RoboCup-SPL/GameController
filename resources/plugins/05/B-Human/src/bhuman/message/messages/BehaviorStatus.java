package bhuman.message.messages;

import bhuman.message.data.Role;
import bhuman.message.Message;
import bhuman.message.data.EnumReader;
import bhuman.message.data.NativeReaders;
import bhuman.message.data.SimpleStreamReader;
import java.nio.ByteBuffer;

/**
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

    public static enum HeadControlFlags {

        noFlag,
        checkingBall
    }

    public Role role;
    public Activity activity;
    public float estimatedTimeToReachBall;
    public int passTarget;
    public HeadControlFlags headControlFlags;

    @Override
    public int getStreamedSize() {
        return new Role().getStreamedSize()
                + new EnumReader<Activity>(Activity.class).getStreamedSize()
                + NativeReaders.floatReader.getStreamedSize()
                + NativeReaders.intReader.getStreamedSize()
                + new EnumReader<HeadControlFlags>(HeadControlFlags.class).getStreamedSize();
    }

    @Override
    public BehaviorStatus read(final ByteBuffer stream) {
        role = new Role().read(stream);
        activity = new EnumReader<Activity>(Activity.class).read(stream);
        estimatedTimeToReachBall = NativeReaders.floatReader.read(stream);
        passTarget = NativeReaders.intReader.read(stream);
        headControlFlags = new EnumReader<HeadControlFlags>(HeadControlFlags.class).read(stream);
        return this;
    }

}
