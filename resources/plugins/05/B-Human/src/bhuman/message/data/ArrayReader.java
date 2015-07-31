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
    private final Class<? extends SimpleStreamReader<T>> readerClass;

    public ArrayReader(final SimpleStreamReader<T> reader) {
        this.reader = reader;
        readerClass = null;
    }

    public ArrayReader(final Class<? extends SimpleStreamReader<T>> cls) {
        reader = null;
        readerClass = cls;
    }

    public int getStreamedSize(final ByteBuffer stream) {
        try {
            return 4 + getElementCount(stream) * (reader != null ? reader.getStreamedSize() : readerClass.newInstance().getStreamedSize());
        } catch (InstantiationException | IllegalAccessException ex) {
        }
        return 4 + getElementCount(stream);
    }

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
