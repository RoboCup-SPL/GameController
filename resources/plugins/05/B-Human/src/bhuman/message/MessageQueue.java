package bhuman.message;

import bhuman.message.messages.Message;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author Felix Thielke
 */
public class MessageQueue {

    private final long timestamp;
    private final long ballTimeWhenLastSeen;
    private final long ballTimeWhenDisappeared;
    private final short ballLastPerceptX;
    private final short ballLastPerceptY;
    private final float robotPoseDeviation;
    private final short robotPoseValidity;

    private final long usedSize;
    private final int numberOfMessages;

    private final Map<MessageID, Message> messages = new EnumMap<MessageID, Message>(MessageID.class);

    public MessageQueue(final ByteBuffer buf) {
        buf.rewind();
        buf.order(ByteOrder.LITTLE_ENDIAN);

        timestamp = toUnsigned(buf.getInt());
        ballTimeWhenLastSeen = toUnsigned(buf.getInt());
        ballTimeWhenDisappeared = toUnsigned(buf.getInt());
        ballLastPerceptX = buf.getShort();
        ballLastPerceptY = buf.getShort();
        robotPoseDeviation = buf.getFloat();
        robotPoseValidity = toUnsigned(buf.get());

        usedSize = toUnsigned(buf.getInt());
        numberOfMessages = buf.getInt();

        while (buf.hasRemaining()) {
            final MessageID id = MessageID.values()[toUnsigned(buf.get())];
            final int size = buf.get() | (buf.get() << 8) | (buf.get() << 16);
            final byte[] data = new byte[size];
            buf.get(data);
            messages.put(id, Message.factory(id, data));
        }
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getBallTimeWhenLastSeen() {
        return ballTimeWhenLastSeen;
    }

    public long getBallTimeWhenDisappeared() {
        return ballTimeWhenDisappeared;
    }

    public short getBallLastPerceptX() {
        return ballLastPerceptX;
    }

    public short getBallLastPerceptY() {
        return ballLastPerceptY;
    }

    public float getRobotPoseDeviation() {
        return robotPoseDeviation;
    }

    public short getRobotPoseValidity() {
        return robotPoseValidity;
    }

    public long getUsedSize() {
        return usedSize;
    }

    public int getNumberOfMessages() {
        return numberOfMessages;
    }

    public Message getMessage(final MessageID id) {
        return messages.get(id);
    }

    private static short toUnsigned(final byte b) {
        return (short) (b < 0 ? b + (2 << Byte.SIZE) : b);
    }

    private static long toUnsigned(final int i) {
        return i < 0 ? i + (1l << (long) Integer.SIZE) : i;
    }
}
