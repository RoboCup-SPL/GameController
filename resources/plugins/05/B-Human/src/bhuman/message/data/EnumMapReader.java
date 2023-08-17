package bhuman.message.data;

import common.Log;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.EnumMap;

/**
 *
 * @author Felix Thielke
 */
public class EnumMapReader<K extends Enum<K>, V> implements ProbablySimpleStreamReader<EnumMap<K, V>> {

    private final Class<K> enumclass;
    private final Class<? extends StreamReader<V>> readerclass;
    private final StreamReader<V> reader;

    public EnumMapReader(final Class<K> enumclass, final Class<? extends StreamReader<V>> readerclass) {
        this.enumclass = enumclass;
        this.readerclass = readerclass;
        this.reader = null;
    }

    public EnumMapReader(final Class<K> enumclass, final StreamReader<V> reader) {
        this.enumclass = enumclass;
        this.readerclass = null;
        this.reader = reader;
    }

    @Override
    public boolean isSimpleStreamReader() {
        final StreamReader<V> reader;
        try {
            reader = this.reader != null ? this.reader : readerclass.getConstructor().newInstance();
        } catch (IllegalAccessException
                 | InstantiationException
                 | InvocationTargetException
                 | NoSuchMethodException
                 | NullPointerException ex) {
            Log.error("Cannot instantiate reader class " + readerclass.getName());
            return false;
        }

        return SimpleStreamReader.class.isInstance(reader)
                || (ProbablySimpleStreamReader.class.isInstance(reader) && ProbablySimpleStreamReader.class.cast(reader).isSimpleStreamReader());
    }

    @Override
    public int getStreamedSize(final ByteBuffer stream) {
        final int count = enumclass.getEnumConstants().length;

        final StreamReader<V> reader;
        try {
            reader = this.reader != null ? this.reader : readerclass.getConstructor().newInstance();
        } catch (IllegalAccessException
                 | InstantiationException
                 | InvocationTargetException
                 | NoSuchMethodException
                 | NullPointerException ex) {
            Log.error("Cannot instantiate reader class " + readerclass.getName());
            return count;
        }

        if (SimpleStreamReader.class.isInstance(reader)) {
            return SimpleStreamReader.class.cast(reader).getStreamedSize() * count;
        } else if (ProbablySimpleStreamReader.class.isInstance(reader) && ProbablySimpleStreamReader.class.cast(reader).isSimpleStreamReader()) {
            return ProbablySimpleStreamReader.class.cast(reader).getStreamedSize(stream) * count;
        }

        final int position = stream.position();
        int size = 0;
        for (int i = 0; i < count; i++) {
            final int elemSize = ComplexStreamReader.class.cast(reader).getStreamedSize(stream);
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
    }

    @Override
    public EnumMap<K, V> read(final ByteBuffer stream) {
        final EnumMap<K, V> map = new EnumMap<>(enumclass);
        for (final K key : enumclass.getEnumConstants()) {
            if (reader != null) {
                map.put(key, reader.read(stream));
            } else {
                try {
                    map.put(key, readerclass.getConstructor().newInstance().read(stream));
                } catch (IllegalAccessException
                         | InstantiationException
                         | InvocationTargetException
                         | NoSuchMethodException
                         | NullPointerException ex) {
                    Log.error("Failed to instantiate reader class " + readerclass.getName());
                }
            }
        }
        return map;
    }

}
