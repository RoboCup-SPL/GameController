package bhuman.message.messages;

import bhuman.message.Message;
import bhuman.message.data.StreamedObject;

/**
 * Class for the BehaviourStatus message.
 *
 * @author Felix Thielke
 */
public class BehaviorStatus extends StreamedObject<BehaviorStatus> implements Message<BehaviorStatus> {

    public static enum Activity {

        unknown,
        blocking,
        duel,
        dribble,
        dribbleDuel,
        searchForBall,
        goToBall,
        takingPosition,
        kick,
        guardGoal,
        catchBall,
        standAndWait,
        passing,
        gettingUp,
        turn,
        walkNextToKeeper,
        kickoff,
        waving,
    }

    /**
     * What is the robot doing in general?
     */
    public Activity activity;

    /**
     * The play that the robot executes.
     */
    public int play;

}
