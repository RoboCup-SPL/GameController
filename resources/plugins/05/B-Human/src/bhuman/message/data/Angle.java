package bhuman.message.data;

import java.nio.ByteBuffer;

/**
 *
 * @author Felix Thielke
 */
public class Angle implements SimpleStreamReader<Angle> {

    public float radians;

    @Override
    public int getStreamedSize() {
        return NativeReaders.floatReader.getStreamedSize();
    }

    @Override
    public Angle read(ByteBuffer stream) {
        radians = NativeReaders.floatReader.read(stream);
        return this;
    }

    public float toDegrees() {
        return (float) ((double) radians * 180.0 / Math.PI);
    }

}
