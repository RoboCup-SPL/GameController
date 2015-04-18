package bhuman.message.data;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import util.Unsigned;

/**
 *
 * @author Felix Thielke
 */
public abstract class NativeReaders {

    public static final SimpleStreamReader<Boolean> boolReader = new BoolReader();
    public static final SimpleStreamReader<Character> charReader = new CharReader();
    public static final SimpleStreamReader<Byte> scharReader = new SCharReader();
    public static final SimpleStreamReader<Short> ucharReader = new UCharReader();
    public static final SimpleStreamReader<Short> shortReader = new ShortReader();
    public static final SimpleStreamReader<Integer> ushortReader = new UShortReader();
    public static final SimpleStreamReader<Integer> intReader = new IntReader();
    public static final SimpleStreamReader<Long> uintReader = new UIntReader();
    public static final SimpleStreamReader<Float> floatReader = new FloatReader();
    public static final SimpleStreamReader<Double> doubleReader = new DoubleReader();
    public static final StreamReader<String> stringReader = new StringReader();

    private static class BoolReader implements SimpleStreamReader<Boolean> {

        @Override
        public int getStreamedSize() {
            return 1;
        }

        @Override
        public Boolean read(final ByteBuffer stream) {
            return stream.get() == 0;
        }

    }

    private static class CharReader implements SimpleStreamReader<Character> {

        @Override
        public int getStreamedSize() {
            return 1;
        }

        @Override
        public Character read(final ByteBuffer stream) {
            return (char) stream.get();
        }

    }

    private static class SCharReader implements SimpleStreamReader<Byte> {

        @Override
        public int getStreamedSize() {
            return 1;
        }

        @Override
        public Byte read(final ByteBuffer stream) {
            return stream.get();
        }

    }

    private static class UCharReader implements SimpleStreamReader<Short> {

        @Override
        public int getStreamedSize() {
            return 1;
        }

        @Override
        public Short read(final ByteBuffer stream) {
            return Unsigned.toUnsigned(stream.get());
        }

    }

    private static class ShortReader implements SimpleStreamReader<Short> {

        @Override
        public int getStreamedSize() {
            return 2;
        }

        @Override
        public Short read(final ByteBuffer stream) {
            return stream.getShort();
        }

    }

    private static class UShortReader implements SimpleStreamReader<Integer> {

        @Override
        public int getStreamedSize() {
            return 2;
        }

        @Override
        public Integer read(final ByteBuffer stream) {
            return Unsigned.toUnsigned(stream.getShort());
        }

    }

    private static class IntReader implements SimpleStreamReader<Integer> {

        @Override
        public int getStreamedSize() {
            return 4;
        }

        @Override
        public Integer read(final ByteBuffer stream) {
            return stream.getInt();
        }

    }

    private static class UIntReader implements SimpleStreamReader<Long> {

        @Override
        public int getStreamedSize() {
            return 4;
        }

        @Override
        public Long read(final ByteBuffer stream) {
            return Unsigned.toUnsigned(stream.getInt());
        }

    }

    private static class FloatReader implements SimpleStreamReader<Float> {

        @Override
        public int getStreamedSize() {
            return 4;
        }

        @Override
        public Float read(final ByteBuffer stream) {
            return stream.getFloat();
        }

    }

    private static class DoubleReader implements SimpleStreamReader<Double> {

        @Override
        public int getStreamedSize() {
            return 8;
        }

        @Override
        public Double read(final ByteBuffer stream) {
            return stream.getDouble();
        }

    }

    private static class StringReader implements StreamReader<String> {

        @Override
        public String read(final ByteBuffer stream) {
            final int size = stream.getInt();
            final byte[] bytes = new byte[size];
            stream.get(bytes);
            try {
                return new String(bytes, "ISO-8859-1");
            } catch (UnsupportedEncodingException ex) {
                try {
                    return new String(bytes, "US-ASCII");
                } catch (UnsupportedEncodingException e) {
                    return new String(bytes);
                }
            }
        }

    }

}
