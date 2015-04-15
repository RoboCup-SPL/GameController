package bhuman.message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
    private final byte sizeMarker;

    private final int usedSize;
    private final int numberOfMessages;
    
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
        sizeMarker = buf.get();

        usedSize = buf.getInt();
        numberOfMessages = buf.getInt();
        
        System.out.println("timestamp: " + timestamp);
        System.out.println("ballTimeWhenLastSeen: " + ballTimeWhenLastSeen);
        System.out.println("ballTimeWhenDisappeared: " + ballTimeWhenDisappeared);
        System.out.println("ballLastPerceptX: " + ballLastPerceptX);
        System.out.println("ballLastPerceptY: " + ballLastPerceptY);
        System.out.println("robotPoseDeviation: " + robotPoseDeviation);
        System.out.println("robotPoseValidity: " + robotPoseValidity);
        System.out.println("sizeMarker: " + sizeMarker);
        System.out.println("usedSize: " + usedSize);
        System.out.println("numberOfMessages: " + numberOfMessages);
        
        while (buf.hasRemaining()) {
            final MessageID id = MessageID.values()[toUnsigned(buf.get())];

            System.out.print(id.toString());
            //if (id != MessageID.idProcessBegin && id != MessageID.idProcessFinished) {
                final int size = buf.get() | (buf.get() << 8) | (buf.get() << 16);
                System.out.print(" (" + size + " bytes)");
                buf.position(buf.position() + size);
            //}
            System.err.println();
        }
    }

    private static short toUnsigned(byte b) {
        return (short) (b < 0 ? b + (2 << Byte.SIZE) : b);
    }

    private static long toUnsigned(int i) {
        return i < 0 ? i + (1l << (long) Integer.SIZE) : i;
    }
}
