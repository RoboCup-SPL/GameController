package bhuman.message.data;

import java.nio.ByteBuffer;

/**
 * Interface for classes whose instances can read objects from a stream.
 *
 * @author Felix Thielke
 * @param <T> type of the objects that can be read
 */
public interface StreamReader<T> {

    /**
     * Reads an object from the given stream.
     *
     * @param stream stream to read from
     * @return object
     */
    public T read(final ByteBuffer stream);
}
