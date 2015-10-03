package bhuman.message.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * StreamReader class for reading arrays (lists) of fixed-size types.
 *
 * @author Felix Thielke
 * @param <T> type of objects stored in the arrays to read
 */
public class ArrayReader<T> implements StreamReader<List<T>> {

    private final SimpleStreamReader<T> reader;
    private final Class<? extends SimpleStreamReader<T>> readerClass;

    /**
     * Constructor.
     *
     * @param reader object to use to read the elements
     */
    public ArrayReader(final SimpleStreamReader<T> reader) {
        this.reader = reader;
        readerClass = null;
    }

    /**
     * Constructor. The given class needs to have a constructor that takes no
     * arguments.
     *
     * @param cls class of objects to use to read the elements
     */
    public ArrayReader(final Class<? extends SimpleStreamReader<T>> cls) {
        reader = null;
        readerClass = cls;
    }

    /**
     * Returns the streamed size of the array in bytes.
     *
     * @param stream stream
     * @return size in bytes
     */
    public int getStreamedSize(final ByteBuffer stream) {
        try {
            return 4 + getElementCount(stream) * (reader != null ? reader.getStreamedSize() : readerClass.newInstance().getStreamedSize());
        } catch (InstantiationException | IllegalAccessException ex) {
        }
        return 4 + getElementCount(stream);
    }

    /**
     * Returns the count of elements in the given streamed array.
     *
     * @param stream stream
     * @return count
     */
    public int getElementCount(final ByteBuffer stream) {
        return stream.getInt(stream.position());
    }

    @Override
    public List<T> read(final ByteBuffer stream) {
        final int count = stream.getInt();
        final ArrayList<T> elems = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            if (reader != null) {
                elems.add(reader.read(stream));
            } else {
                try {
                    elems.add(readerClass.newInstance().read(stream));
                } catch (InstantiationException | IllegalAccessException ex) {
                }
            }
        }
        return elems;
    }

}
