package bhuman.message.messages;

import bhuman.message.Message;
import bhuman.message.data.Angle;
import bhuman.message.data.StreamedObject;
import java.util.List;

/**
 * Class for the WhistleDOA message.
 *
 * @author Arne Hasselbring
 */
public class WhistleDOA extends StreamedObject<WhistleDOA> implements Message<WhistleDOA> {
    /**
     * The possible directions (relative to the robot) from which the whistle was blown.
     */
    public List<Angle> whistleDirections;

}
