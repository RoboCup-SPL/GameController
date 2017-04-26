package bhuman.message;

import bhuman.message.messages.BehaviorStatus;
import bhuman.message.messages.RobotHealth;
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
            final RobotHealth health = message.queue == null ? null : message.queue.getCachedMessage(RobotHealth.class);
            final BehaviorStatus status = message.queue == null ? null : message.queue.getCachedMessage(BehaviorStatus.class);

            if (health != null) {
                display.add(health.robotName);
                display.add("Location: " + String.valueOf(health.location));
                display.add("Configuration: " + health.configuration);
                display.add("Battery: " + health.batteryLevel + "%");
                if (health.jointWithMaxTemperature != null) {
                    display.add("Hottest joint: " + health.jointWithMaxTemperature + " (" + health.maxJointTemperature + "°C)");
                }
                display.add("Avg. motion time: " + df.format(health.avgMotionTime));
                display.add("Cognition fps: " + df.format(health.cognitionFrameRate));
                display.add("");
            }

            if (message.bhuman != null) {
                display.add("Magic: " + message.bhuman.magicNumber);
                display.add("");
            }

            if (message.bhulks != null) {
                display.add("Role: " + message.bhulks.currentlyPerfomingRole);
                display.add("TimeToReachBall: " + (message.bhulks.timeWhenReachBall - message.bhulks.timestamp) + "ms");
                display.add("TimeTillQueenReachesBall: " + (message.bhulks.timeWhenReachBallQueen - message.bhulks.timestamp) + "ms");
                display.add("TimeSinceLastJumped: " + (message.bhulks.timestamp - message.bhulks.timestampLastJumped) + "ms");
                display.add("HearingConfidence: " + message.bhulks.confidenceOfLastWhistleDetection);
            }
            if (status != null) {
                display.add("Activity: " + status.activity);
            }

            if (message.queue != null) {
                display.add(DISPLAY_NEXT_COLUMN);
                display.add("MessageQueue:");
                for (final String name : message.queue.getMessageNames()) {
                    display.add("* " + name);
                }
            }
        }

        return display.toArray(new String[0]);
    }

    @Override
    public void init() {
        message = new BHumanMessageParts(this, ByteBuffer.wrap(data));
    }

}
