package bhuman.message.messages;

import bhuman.message.Message;
import java.nio.ByteBuffer;

/**
 *
 * @author Felix Thielke
 */
public class GoalPercept extends Message {

    public static class GoalPost {

        public static enum Position {

            IS_UNKNOWN, IS_LEFT, IS_RIGHT
        }

        public final Position position;

        public GoalPost(final ByteBuffer data) {
            position = Message.readEnum(data, Position.class);
            data.position(data.position() + 16);
        }

    }
    
    public GoalPercept(final ByteBuffer data) {
        super(data);

    }

}
