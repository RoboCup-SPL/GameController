package bhuman.message;

import bhuman.message.messages.BehaviorStatus;
import bhuman.message.messages.RobotHealth;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import teamcomm.data.AdvancedMessage;

/**
 *
 * @author Felix Thielke
 */
public class BHumanMessage extends AdvancedMessage {

    private static final long serialVersionUID = 8509144227967224852L;

    public MessageQueue queue;

    @Override
    public String[] display() {
        final List<String> display = new LinkedList<String>();
        final RobotHealth health = queue.getCachedMessage(RobotHealth.class);
        final BehaviorStatus status = queue.getMessage(BehaviorStatus.class);

        if (health != null) {
            display.add(health.robotName);
            display.add("Location: " + health.location);
            display.add("Configuration: " + health.configuration);
            display.add("Battery: " + health.batteryLevel + "%");
            display.add("");
        }
        if (status != null) {
            display.add("Role: " + status.role);
        }

        return display.toArray(new String[0]);
    }

    @Override
    public void init() {
        queue = new MessageQueue(this, ByteBuffer.wrap(data));
        
        // Update cached RobotHealth
        queue.getCachedMessage(RobotHealth.class);
    }

}
