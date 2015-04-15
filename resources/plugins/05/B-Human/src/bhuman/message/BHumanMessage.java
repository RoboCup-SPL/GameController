package bhuman.message;

import java.nio.ByteBuffer;
import teamcomm.data.messages.AdvancedMessage;

/**
 *
 * @author Felix Thielke
 */
public class BHumanMessage extends AdvancedMessage {
    private static final long serialVersionUID = 8509144227967224852L;

    @Override
    public String[] display() {
        return null;
    }

    @Override
    public void init() {
        System.out.println("test");
        MessageQueue queue = new MessageQueue(ByteBuffer.wrap(data));
    }
    
}
