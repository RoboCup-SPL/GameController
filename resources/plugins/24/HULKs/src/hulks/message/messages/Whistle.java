package hulks.message.messages;

import hulks.message.Message;
import hulks.message.data.StreamedObject;
import hulks.message.data.Timestamp;

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
