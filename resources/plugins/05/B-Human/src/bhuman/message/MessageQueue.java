package bhuman.message;

import data.SPLStandardMessage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import util.Unsigned;

/**
 * Class for reading from the B-Human MessageQueue.
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
    private final byte magicNumber;

    private final long usedSize;
    private final int numberOfMessages;

    private static final Map<String, Map<Class<?>, Message<? extends Message>>> cachedMessages = new HashMap<>();

    private final Map<Class<?>, Message<? extends Message>> messages = new HashMap<>();

    /**
     * Constructor.
     *
     * @param origin SPLStandardMessage containing this MessageQueue
     * @param buf raw data of the transmitted MessageQueue
     */
    public MessageQueue(final SPLStandardMessage origin, final ByteBuffer buf) {
        robotIdentifier = Unsigned.toUnsigned(origin.teamNum) + "," + origin.playerNum;

        buf.rewind();
        buf.order(ByteOrder.LITTLE_ENDIAN);

        // read header
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
        magicNumber = buf.get();

        usedSize = Unsigned.toUnsigned(buf.getInt());
        numberOfMessages = buf.getInt();

        // read messages
        while (buf.hasRemaining()) {
            final short idIndex = Unsigned.toUnsigned(buf.get());
            final int size = Unsigned.toUnsigned(buf.get()) | (Unsigned.toUnsigned(buf.get()) << 8) | (Unsigned.toUnsigned(buf.get()) << 16);
            final byte[] data = new byte[size];
            buf.get(data);
            final Message<? extends Message> msg = Message.factory(idIndex, Unsigned.toUnsigned(origin.teamNum), ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN));
            if (msg != null) {
                messages.put(msg.getClass(), msg);
            }
        }
    }

    /**
     * Returns the timestamp value from the MessageQueue header.
     *
     * @return timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the ballTimeWhenLastSeen value from the MessageQueue header.
     *
     * @return ballTimeWhenLastSeen
     */
    public long getBallTimeWhenLastSeen() {
        return ballTimeWhenLastSeen;
    }

    /**
     * Returns the ballTimeWhenDisappeared value from the MessageQueue header.
     *
     * @return ballTimeWhenDisappeared
     */
    public long getBallTimeWhenDisappeared() {
        return ballTimeWhenDisappeared;
    }

    /**
     * Returns the ballLastPerceptX value from the MessageQueue header.
     *
     * @return ballLastPerceptX
     */
    public short getBallLastPerceptX() {
        return ballLastPerceptX;
    }

    /**
     * Returns the ballLastPerceptY value from the MessageQueue header.
     *
     * @return ballLastPerceptY
     */
    public short getBallLastPerceptY() {
        return ballLastPerceptY;
    }

    /**
     * Returns the ballCovariance value from the MessageQueue header.
     *
     * @return ballCovariance
     */
    public float[] getBallCovariance() {
        return ballCovariance;
    }

    /**
     * Returns the robotPoseDeviation value from the MessageQueue header.
     *
     * @return robotPoseDeviation
     */
    public float getRobotPoseDeviation() {
        return robotPoseDeviation;
    }

    /**
     * Returns the robotPoseCovariance value from the MessageQueue header.
     *
     * @return robotPoseCovariance
     */
    public float[] getRobotPoseCovariance() {
        return robotPoseCovariance;
    }

    /**
     * Returns the robotPoseValidity value from the MessageQueue header.
     *
     * @return robotPoseValidity
     */
    public short getRobotPoseValidity() {
        return robotPoseValidity;
    }

    /**
     * Returns the magicNumber value from the MessageQueue header.
     *
     * @return magicNumber
     */
    public byte getMagicNumber() {
        return magicNumber;
    }

    /**
     * Returns the size of the MessageQueue in bytes.
     *
     * @return size of the MessageQueue in bytes
     */
    public long getUsedSize() {
        return usedSize;
    }

    /**
     * Returns the number of messages in the MessageQueue.
     *
     * @return number of messages
     */
    public int getNumberOfMessages() {
        return numberOfMessages;
    }

    /**
     * Gets a message of the given class from the MessageQueue.
     *
     * @param <T> class type of the message
     * @param cls class of the message
     * @return message or null if there is none
     */
    public <T extends Message<T>> T getMessage(final Class<T> cls) {
        return cls.cast(messages.get(cls));
    }

    /**
     * Gets an optionally cached message of the given class from the
     * MessageQueue. If no compatible message is found in the MessageQueue, the
     * last compatible message that was returned by a call to this method on
     * another MessageQueue sent by the same robot is returned.
     *
     * @param <T> class type of the message
     * @param cls class of the message
     * @return message or null if there is none
     */
    public <T extends Message<T>> T getCachedMessage(final Class<T> cls) {
        Map<Class<?>, Message<? extends Message>> cache = cachedMessages.get(robotIdentifier);
        if (cache == null) {
            cache = new HashMap<>();
            cachedMessages.put(robotIdentifier, cache);
        }

        T message = cls.cast(messages.get(cls));
        if (message != null) {
            cache.put(cls, message);
        } else {
            message = cls.cast(cache.get(cls));
        }

        return message;
    }
}
