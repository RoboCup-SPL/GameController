package bhuman.message.data;

import java.nio.ByteBuffer;

/**
 *
 * @author Felix Thielke
 */
public interface StreamReader<T> {

    public T read(final ByteBuffer stream);
}
