package bhuman.message;

import bhuman.message.messages.BehaviorStatus;
import java.nio.ByteBuffer;
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
        final BehaviorStatus status = queue.getMessage(BehaviorStatus.class);

        return new String[]{
            "Messages: " + queue.getNumberOfMessages(),
            status == null ? null : "Role: " + status.role
        };
    }

    @Override
    public void init() {
        queue = new MessageQueue(ByteBuffer.wrap(data));
    }

}
