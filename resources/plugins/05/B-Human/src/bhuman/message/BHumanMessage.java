package bhuman.message;

import bhuman.message.messages.BehaviorStatus;
import bhuman.message.messages.NetworkThumbnail;
import bhuman.message.messages.RobotHealth;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import teamcomm.data.AdvancedMessage;
import util.Unsigned;

/**
 * Custom message class for team B-Human.
 *
 * @author Felix Thielke
 */
public class BHumanMessage extends AdvancedMessage {

    private static final long serialVersionUID = 8509144227967224852L;

    /**
     * The MessageQueue transferred in this message.
     */
    public MessageQueue queue;

    @Override
    public String[] display() {
        final List<String> display = new LinkedList<>();
        if (valid) {
            final RobotHealth health = queue.getCachedMessage(RobotHealth.class);
            final BehaviorStatus status = queue.getCachedMessage(BehaviorStatus.class);

            if (health != null) {
                display.add(health.robotName);
                display.add("Location: " + health.location);
                display.add("Configuration: " + health.configuration);
                display.add("Magic: " + Unsigned.toUnsigned(queue.getMagicNumber()));
                display.add("Battery: " + health.batteryLevel + "%");
                if (health.jointWithMaxTemperature != null) {
                    display.add("Hottest joint: " + health.jointWithMaxTemperature.toString() + " (" + health.maxJointTemperature + "°C)");
                }
                display.add("");
            }
            if (status != null) {
                display.add("Role: " + status.role);
                display.add("Activity: " + status.activity);
            }
        }

        return display.toArray(new String[0]);
    }

    @Override
    public void init() {
        queue = new MessageQueue(this, ByteBuffer.wrap(data));

        // Update cached RobotHealth and BehaviorStatus
        queue.getCachedMessage(RobotHealth.class);
        queue.getCachedMessage(BehaviorStatus.class);

        // Update thumbnail image
        final NetworkThumbnail msg = queue.getMessage(NetworkThumbnail.class);
        if (msg != null) {
            Thumbnail.getInstance(teamNum + "," + playerNum).handleMessage(msg);
        }
    }

}
