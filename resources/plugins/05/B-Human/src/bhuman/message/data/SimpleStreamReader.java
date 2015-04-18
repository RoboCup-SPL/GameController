package bhuman.message.data;

/**
 *
 * @author Felix Thielke
 */
public interface SimpleStreamReader<T> extends StreamReader<T> {

    public int getStreamedSize();
}
