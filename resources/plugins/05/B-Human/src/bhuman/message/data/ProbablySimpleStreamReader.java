package bhuman.message.data;

/**
 * Interface for classes whose instances are maybe SimpleStreamReaders although
 * they implement the ComplexStreamReader interface.
 *
 * @author Felix Thielke
 * @param <T> type of the objects that can be read
 */
public interface ProbablySimpleStreamReader<T> extends ComplexStreamReader<T> {

    /**
     * Returns whether this object qualifies as a SimpleStreamReader.
     *
     * @return bool
     */
    boolean isSimpleStreamReader();
}
