package bhuman.message.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Felix Thielke
 */
public class ArrayReader<T> implements StreamReader<List<T>> {

    private final SimpleStreamReader<T> reader;

    public ArrayReader(final SimpleStreamReader<T> reader) {
        this.reader = reader;
    }

    public ArrayReader(final Class<? extends SimpleStreamReader<T>> cls) throws InstantiationException, IllegalAccessException {
        this.reader = cls.newInstance();
    }

    public int getStreamedSize(final ByteBuffer stream) {
        return 4 + getElementCount(stream) * reader.getStreamedSize();
    }

    public int getElementCount(final ByteBuffer stream) {
        return stream.getInt(stream.position());
    }

    @Override
    public List<T> read(final ByteBuffer stream) {
        final int count = stream.getInt();
        final ArrayList<T> elems = new ArrayList<T>(count);
        for (int i = 0; i < count; i++) {
            elems.add(reader.read(stream));
        }
        return elems;
    }

}
