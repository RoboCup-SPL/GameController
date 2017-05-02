package bhuman.message.messages;

import bhuman.message.Message;
import bhuman.message.data.StreamedObject;
import bhuman.message.data.Timestamp;

/**
 * Class for the BehaviourStatus message.
 *
 * @author Felix Thielke
 */
public class Whistle extends StreamedObject<Whistle> implements Message<Whistle> {

    /**
     * The last point of time when the robot received audio data.
     */
    public Timestamp lastTimeOfIncomingSound;

    /**
     * Name of the last detected whistle.
     */
    public String whistleName;
}
