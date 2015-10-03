package bhuman.message.data;

/**
 * Interface for classes whose instances can read fixed-size objects from a
 * stream.
 *
 * @author Felix Thielke
 * @param <T> type of the objects that can be read
 */
public interface SimpleStreamReader<T> extends StreamReader<T> {

    /**
     * Returns the size in bytes of objects that are read by this object.
     *
     * @return size in bytes
     */
    public int getStreamedSize();
}
