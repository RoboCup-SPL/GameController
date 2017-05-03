package bhuman.message.messages;

import bhuman.message.Message;
import bhuman.message.data.StreamedObject;
import java.util.List;

/**
 * Class for the BehaviourStatus message.
 *
 * @author Felix Thielke
 */
public class SideConfidence extends StreamedObject<SideConfidence> implements Message<SideConfidence> {

    public static enum ConfidenceState {

        CONFIDENT,
        ALMOST_CONFIDENT,
        UNSURE,
        CONFUSED
    }

    /**
     * Am I mirrored because of field symmetry (0 = no idea, 1 = absolute sure I
     * am right).
     */
    public float sideConfidence;
    /**
     * Indicates whether ball model of others is mirrored to own ball model.
     */
    public boolean mirror;
    /**
     * The state of confidence
     */
    public ConfidenceState confidenceState;
    /**
     * The robot numbers of the robots the agree with me regarding the side
     */
    public List<Integer> agreeMates;

}
