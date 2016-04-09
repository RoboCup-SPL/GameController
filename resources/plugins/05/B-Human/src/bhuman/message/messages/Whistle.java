package bhuman.message.messages;

import bhuman.message.Message;
import bhuman.message.data.NativeReaders;
import bhuman.message.data.SimpleStreamReader;
import java.nio.ByteBuffer;

/**
 * Class for the Whistle message.
 *
 * @author Felix Thielke
 */
public class Whistle extends Message<Whistle> implements SimpleStreamReader<Whistle> {

    /**
     * Confidence based on hearing capability.
     */
    public byte confidenceOfLastWhistleDetection;
    /**
     * Timestamp.
     */
    public long lastTimeWhistleDetected;
    /**
     * The last point of time when the robot received audio data.
     */
    public long lastTimeOfIncomingSound;
    /**
     * Name of the last detected whistle.
     */
    public String whistleName;

    @Override
    public Whistle read(final ByteBuffer stream) {
        confidenceOfLastWhistleDetection = NativeReaders.scharReader.read(stream);
        lastTimeWhistleDetected = NativeReaders.uintReader.read(stream);
        lastTimeOfIncomingSound = NativeReaders.uintReader.read(stream);
        whistleName = NativeReaders.stringReader.read(stream);

        return this;
    }

    @Override
    public int getStreamedSize() {
        return -1; // Size unknown because of whistle's name
    }

}
