package bhuman.message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import util.Unsigned;

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

    private final Map<Class<?>, Message<? extends Message>> messages = new HashMap<Class<?>, Message<? extends Message>>();

    public MessageQueue(final ByteBuffer buf) {
        buf.rewind();
        buf.order(ByteOrder.LITTLE_ENDIAN);

        timestamp = Unsigned.toUnsigned(buf.getInt());
        ballTimeWhenLastSeen = Unsigned.toUnsigned(buf.getInt());
        ballTimeWhenDisappeared = Unsigned.toUnsigned(buf.getInt());
        ballLastPerceptX = buf.getShort();
        ballLastPerceptY = buf.getShort();
        robotPoseDeviation = buf.getFloat();
        robotPoseValidity = Unsigned.toUnsigned(buf.get());

        usedSize = Unsigned.toUnsigned(buf.getInt());
        numberOfMessages = buf.getInt();

        while (buf.hasRemaining()) {
            final MessageID id = MessageID.values()[Unsigned.toUnsigned(buf.get())];
            final int size = buf.get() | (buf.get() << 8) | (buf.get() << 16);
            final byte[] data = new byte[size];
            buf.get(data);
            final Message<? extends Message> msg = Message.factory(id, ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN));
            if (msg != null) {
                messages.put(msg.getClass(), msg);
            }
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

    public <T extends Message<T>> T getMessage(final Class<T> cls) {
        return (T) messages.get(cls);
    }
}
