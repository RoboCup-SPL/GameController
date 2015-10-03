package bhuman.message.data;

import java.nio.ByteBuffer;

/**
 *
 * @author Felix Thielke
 */
public class EnumReader<E extends Enum<E>> implements SimpleStreamReader<E> {

    private final Class<E> enumClass;

    public EnumReader(final Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public int getStreamedSize() {
        try {
            if (enumClass.getEnumConstants().length >= (1 << 8)) {
                return NativeReaders.uintReader.getStreamedSize();
            }
        } catch (final SecurityException | IllegalArgumentException ex) {
        }
        return NativeReaders.ucharReader.getStreamedSize();
    }

    @Override
    public E read(final ByteBuffer stream) {
        try {
            final E[] values = enumClass.getEnumConstants();
            final int val;
            if (values.length >= (1 << 8)) {
                val = NativeReaders.intReader.read(stream);
            } else {
                val = NativeReaders.ucharReader.read(stream);
            }
            if (val < values.length) {
                return values[val];
            }
        } catch (final SecurityException | IllegalArgumentException ex) {
        }

        return null;
    }

}
