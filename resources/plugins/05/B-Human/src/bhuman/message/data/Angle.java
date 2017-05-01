package bhuman.message.data;

import java.nio.ByteBuffer;

/**
 * Class for angles.
 *
 * @author Felix Thielke
 */
public class Angle implements SimpleStreamReader<Angle> {

    /**
     * Angle in radians.
     */
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

    /**
     * Returns the value of this angle in degrees.
     *
     * @return value of this angle in degrees
     */
    public float toDegrees() {
        return (float) ((double) radians * 180.0 / Math.PI);
    }

    public static Angle fromDegrees(final double degrees) {
        final Angle a = new Angle();
        a.radians = (float) (degrees * Math.PI / 180.0);
        return a;
    }

    @Override
    public String toString() {
        return toDegrees() + "°";
    }

}
