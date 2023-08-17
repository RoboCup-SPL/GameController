package bhuman.message.data;

import common.Log;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

/**
 * StreamReader class for reading fixed-size arrays of streamed objects.
 *
 * @author Felix Thielke
 * @param <T> type of objects stored in the arrays to read
 */
public class ArrayReader<T> implements ProbablySimpleStreamReader<T[]> {

    private final StreamReader<T> reader;
    private final Class<? extends StreamReader<T>> readerClass;
    private final T[] array;

    /**
     * Constructor.
     *
     * @param reader object to use to read the elements
     */
    public ArrayReader(final StreamReader<T> reader, final T[] array) {
        this.reader = reader;
        readerClass = null;
        this.array = array;
    }

    /**
     * Constructor. The given class needs to have a constructor that takes no
     * arguments.
     *
     * @param cls class of objects to use to read the elements
     */
    public ArrayReader(final Class<? extends StreamReader<T>> cls, final T[] array) {
        reader = null;
        readerClass = cls;
        this.array = array;
    }

    @Override
    public T[] read(final ByteBuffer stream) {
        for (int i = 0; i < array.length; i++) {
            if (reader != null) {
                array[i] = reader.read(stream);
            } else {
                try {
                    array[i] = readerClass.getConstructor().newInstance().read(stream);
                } catch (IllegalAccessException
                         | InstantiationException
                         | InvocationTargetException
                         | NoSuchMethodException
                         | NullPointerException ex) {
                    Log.error("Failed to instantiate reader class " + readerClass.getName());
                }
            }
        }
        return array;
    }

    @Override
    public int getStreamedSize(final ByteBuffer stream) {
        try {
            if (SimpleStreamReader.class.isInstance(reader)) {
                return array.length * ((SimpleStreamReader<T>) reader).getStreamedSize();
            } else if (readerClass != null && SimpleStreamReader.class.isAssignableFrom(readerClass)) {
                return array.length * ((SimpleStreamReader<T>) readerClass.getConstructor().newInstance()).getStreamedSize();
            }

            final ComplexStreamReader<T> reader = (ComplexStreamReader<T>) (this.reader != null ? this.reader : readerClass.getConstructor().newInstance());
            if (ProbablySimpleStreamReader.class.isInstance(reader) && ProbablySimpleStreamReader.class.cast(reader).isSimpleStreamReader()) {
                return array.length * reader.getStreamedSize(stream);
            }

            final int position = stream.position();
            int size = 0;
            for (int i = 0; i < array.length; i++) {
                final int elemSize = reader.getStreamedSize(stream);
                size += elemSize;
                if (i < array.length - 1) {
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

    @Override
    public boolean isSimpleStreamReader() {
        final StreamReader<T> reader;
        try {
            reader = this.reader != null ? this.reader : readerClass.getConstructor().newInstance();
        } catch (IllegalAccessException
                 | InstantiationException
                 | InvocationTargetException
                 | NoSuchMethodException
                 | NullPointerException ex) {
            Log.error("Cannot instantiate reader class " + readerClass.getName());
            return false;
        }

        return SimpleStreamReader.class.isInstance(reader)
                || (ProbablySimpleStreamReader.class.isInstance(reader) && ProbablySimpleStreamReader.class.cast(reader).isSimpleStreamReader());
    }

}
