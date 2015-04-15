package bhuman.message;

import java.nio.ByteBuffer;
import teamcomm.data.messages.AdvancedMessage;

/**
 *
 * @author Felix Thielke
 */
public class BHumanMessage extends AdvancedMessage {

    private static final long serialVersionUID = 8509144227967224852L;

    public MessageQueue queue;

    @Override
    public String[] display() {
        return null;
    }

    @Override
    public void init() {
        queue = new MessageQueue(ByteBuffer.wrap(data));
    }

}
