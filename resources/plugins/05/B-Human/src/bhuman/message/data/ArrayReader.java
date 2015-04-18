package bhuman.message.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Felix Thielke
 */
public class ArrayReader<T> implements StreamReader<List<T>> {

    private final Class<? extends StreamReader<T>> cls;

    public ArrayReader(final Class<? extends StreamReader<T>> cls) {
        this.cls = cls;
    }

    public int getElementCount(final ByteBuffer stream) {
        return stream.getInt(stream.position());
    }

    @Override
    public List<T> read(final ByteBuffer stream) {
        final int count = stream.getInt();
        final ArrayList<T> elems = new ArrayList<T>(count);
        for (int i = 0; i < count; i++) {
            try {
                elems.add(cls.newInstance().read(stream));
            } catch (InstantiationException ex) {
                throw new RuntimeException(ex);
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }
        return elems;
    }

}
