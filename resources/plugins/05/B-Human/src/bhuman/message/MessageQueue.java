package bhuman.message;

import data.SPLStandardMessage;
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

    private final String robotIdentifier;

    private final long timestamp;
    private final long ballTimeWhenLastSeen;
    private final long ballTimeWhenDisappeared;
    private final short ballLastPerceptX;
    private final short ballLastPerceptY;
    private final float[] ballCovariance;
    private final float robotPoseDeviation;
    private final float[] robotPoseCovariance;
    private final short robotPoseValidity;

    private final long usedSize;
    private final int numberOfMessages;

    private static final Map<String, Map<Class<?>, Message<? extends Message>>> cachedMessages = new HashMap<>();

    private final Map<Class<?>, Message<? extends Message>> messages = new HashMap<>();

    public MessageQueue(final SPLStandardMessage origin, final ByteBuffer buf) {
        robotIdentifier = origin.teamNum + "," + origin.playerNum;

        buf.rewind();
        buf.order(ByteOrder.LITTLE_ENDIAN);

        timestamp = Unsigned.toUnsigned(buf.getInt());
        ballTimeWhenLastSeen = Unsigned.toUnsigned(buf.getInt());
        ballTimeWhenDisappeared = Unsigned.toUnsigned(buf.getInt());
        ballLastPerceptX = buf.getShort();
        ballLastPerceptY = buf.getShort();
        ballCovariance = new float[]{buf.getFloat(), buf.getFloat(), buf.getFloat()};
        robotPoseDeviation = buf.getFloat();
        robotPoseCovariance = new float[]{
            buf.getFloat(), buf.getFloat(), buf.getFloat(),
            buf.getFloat(), buf.getFloat(), buf.getFloat()};
        robotPoseValidity = Unsigned.toUnsigned(buf.get());

        usedSize = Unsigned.toUnsigned(buf.getInt());
        numberOfMessages = buf.getInt();

        while (buf.hasRemaining()) {
            final short idIndex = Unsigned.toUnsigned(buf.get());
            final MessageID id = MessageID.values()[idIndex];
            final int size = Unsigned.toUnsigned(buf.get()) | (Unsigned.toUnsigned(buf.get()) << 8) | (Unsigned.toUnsigned(buf.get()) << 16);
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

    public float[] getBallCovariance() {
        return ballCovariance;
    }

    public float getRobotPoseDeviation() {
        return robotPoseDeviation;
    }

    public float[] getRobotPoseCovariance() {
        return robotPoseCovariance;
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

    public <T extends Message<T>> T getCachedMessage(final Class<T> cls) {
        Map<Class<?>, Message<? extends Message>> cache = cachedMessages.get(robotIdentifier);
        if (cache == null) {
            cache = new HashMap<>();
            cachedMessages.put(robotIdentifier, cache);
        }

        T message = (T) messages.get(cls);
        if (message != null) {
            cache.put(cls, message);
        } else {
            message = (T) cache.get(cls);
        }

        return message;
    }
}
