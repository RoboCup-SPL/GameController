package bhuman.message.data;

import java.nio.ByteBuffer;

/**
 * Interface for classes whose instances can read fixed-size objects from a
 * stream.
 *
 * @author Felix Thielke
 * @param <T> type of the objects that can be read
 */
public interface ComplexStreamReader<T> extends StreamReader<T> {

    /**
     * Returns the size in bytes of objects that are read by this object.
     *
     * @param stream stream to read from
     * @return size in bytes
     */
    int getStreamedSize(final ByteBuffer stream);
}
