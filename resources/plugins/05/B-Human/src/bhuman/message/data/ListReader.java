package bhuman.message.data;

import common.Log;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import util.Unsigned;

/**
 * StreamReader class for reading lists streamable objects.
 *
 * @author Felix Thielke
 * @param <T> type of objects stored in the arrays to read
 */
public class ListReader<T> implements ComplexStreamReader<List<T>> {

    private final StreamReader<T> reader;
    private final Class<? extends StreamReader<T>> readerClass;
    private final int listCountSize;

    /**
     * Constructor.
     *
     * @param reader object to use to read the elements
     */
    public ListReader(final StreamReader<T> reader, final int listCountSize) {
        this.reader = reader;
        readerClass = null;
        this.listCountSize = listCountSize;
    }

    /**
     * Constructor. The given class needs to have a constructor that takes no
     * arguments.
     *
     * @param cls class of objects to use to read the elements
     */
    public ListReader(final Class<? extends StreamReader<T>> cls, final int listCountSize) {
        reader = null;
        readerClass = cls;
        this.listCountSize = listCountSize;
    }

    /**
     * Returns the count of elements in the given streamed array.
     *
     * @param stream stream
     * @return count
     */
    public int getElementCount(final ByteBuffer stream) {
        switch (listCountSize) {
            case 1:
                return Unsigned.toUnsigned(stream.get(stream.position()));
            case 2:
                return Unsigned.toUnsigned(stream.getShort(stream.position()));
            case 4:
                return stream.getInt(stream.position());
            default:
                Log.error("List count size " + listCountSize + " is not allowed!");
                return 0;
        }
    }

    @Override
    public List<T> read(final ByteBuffer stream) {
        final int count = getElementCount(stream);
        stream.position(stream.position() + listCountSize);
        final ArrayList<T> elems = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            if (reader != null) {
                elems.add(reader.read(stream));
            } else {
                try {
                    elems.add(readerClass.getConstructor().newInstance().read(stream));
                } catch (IllegalAccessException
                         | InstantiationException
                         | InvocationTargetException
                         | NoSuchMethodException
                         | NullPointerException ex) {
                }
            }
        }
        return elems;
    }

    @Override
    public int getStreamedSize(final ByteBuffer stream) {
        final int count = getElementCount(stream);
        try {
            if (SimpleStreamReader.class.isInstance(reader)) {
                return listCountSize + count * ((SimpleStreamReader<T>) reader).getStreamedSize();
            } else if (readerClass != null && SimpleStreamReader.class.isAssignableFrom(readerClass)) {
                return listCountSize + count * ((SimpleStreamReader<T>) readerClass.getConstructor().newInstance()).getStreamedSize();
            }

            final ComplexStreamReader<T> reader = (ComplexStreamReader<T>) (this.reader != null ? this.reader : readerClass.getConstructor().newInstance());
            if (ProbablySimpleStreamReader.class.isInstance(reader) && ProbablySimpleStreamReader.class.cast(reader).isSimpleStreamReader()) {
                return listCountSize + count * reader.getStreamedSize(stream);
            }

            final int position = stream.position();
            stream.position(stream.position() + listCountSize);
            int size = listCountSize;
            for (int i = 0; i < count; i++) {
                final int elemSize = reader.getStreamedSize(stream);
                size += elemSize;
                if (i < count - 1) {
                    if (elemSize > stream.remaining()) {
                        stream.position(position);
                        return size + elemSize;
                    }
                    stream.position(stream.position() + elemSize);
                }
            }
            stream.position(position);
            return size;
        } catch (IllegalAccessException
                 | InstantiationException
                 | InvocationTargetException
                 | NoSuchMethodException
                 | NullPointerException ex) {
            Log.error("Failed to instantiate reader class " + readerClass.getName());
            return 0;
        }
    }

}
