package bhuman.message;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import teamcomm.data.AdvancedMessage;

/**
 * Custom message class for team B-Human.
 *
 * @author Felix Thielke
 */
public class BHumanMessage extends AdvancedMessage {

    private static final long serialVersionUID = 8509144227967224852L;

    /**
     * The B-Human message parts transferred in this message.
     */
    public BHumanMessageParts message;

    @Override
    public String[] display() {
        final List<String> display = new LinkedList<>();
        if (valid) {
            final DecimalFormat df = new DecimalFormat("#.00");

            if (message.bhuman != null) {
                display.add("Magic: " + message.bhuman.magicNumber);
                display.add("Activity: " + message.bhuman.theBehaviorStatus.activity);
                display.add("TimeSinceLastJumped: " + (message.bhuman.timestamp - message.bhuman.theRobotPose.timestampLastJump.timestamp) + "ms");
                display.add("HearingConfidence: " + message.bhuman.theWhistle.confidenceOfLastWhistleDetection);
                display.add("");
            }
        }

        return display.toArray(new String[0]);
    }

    @Override
    public void init() {
        message = new BHumanMessageParts(this, ByteBuffer.wrap(data));
    }

}
