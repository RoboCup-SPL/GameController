package bhuman.message.data;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import util.Unsigned;

/**
 * Abstract class containing StreamReader classes and instances for native C++
 * types.
 *
 * @author Felix Thielke
 */
public abstract class NativeReaders {

    /**
     * SimpleStreamReader for bools.
     */
    public static final SimpleStreamReader<Boolean> boolReader = new BoolReader();

    /**
     * SimpleStreamReader for chars (read as Java chars).
     */
    public static final SimpleStreamReader<Character> charReader = new CharReader();

    /**
     * SimpleStreamReader for signed chars (read as Java bytes).
     */
    public static final SimpleStreamReader<Byte> scharReader = new SCharReader();

    /**
     * SimpleStreamReader for unsigned chars.
     */
    public static final SimpleStreamReader<Short> ucharReader = new UCharReader();

    /**
     * SimpleStreamReader for shorts.
     */
    public static final SimpleStreamReader<Short> shortReader = new ShortReader();

    /**
     * SimpleStreamReader for unsigned shorts.
     */
    public static final SimpleStreamReader<Integer> ushortReader = new UShortReader();

    /**
     * SimpleStreamReader for ints.
     */
    public static final SimpleStreamReader<Integer> intReader = new IntReader();

    /**
     * SimpleStreamReader for unsigned ints.
     */
    public static final SimpleStreamReader<Long> uintReader = new UIntReader();

    /**
     * SimpleStreamReader for floats.
     */
    public static final SimpleStreamReader<Float> floatReader = new FloatReader();

    /**
     * SimpleStreamReader for doubles.
     */
    public static final SimpleStreamReader<Double> doubleReader = new DoubleReader();

    /**
     * StreamReader for strings.
     */
    public static final StringReader stringReader = new StringReader();

    private static class BoolReader implements SimpleStreamReader<Boolean> {

        @Override
        public int getStreamedSize() {
            return 1;
        }

        @Override
        public Boolean read(final ByteBuffer stream) {
            return stream.get() != 0;
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

    /**
     * StreamReader for strings.
     */
    public static class StringReader implements StreamReader<String> {

        public int getStreamedSize() {
            return -1;
        }

        public int getStreamedSize(final ByteBuffer stream) {
            return 4 + getLength(stream);
        }

        public int getLength(final ByteBuffer stream) {
            return stream.getInt(stream.position());
        }

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

    /**
     * SimpleStreamReader for strings of a fixed size.
     */
    public static class SimpleStringReader implements SimpleStreamReader<String> {

        private final int count;

        public SimpleStringReader(final int count) {
            this.count = count;
        }

        @Override
        public int getStreamedSize() {
            return count;
        }

        @Override
        public String read(final ByteBuffer stream) {
            final byte[] bytes = new byte[count];
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

    /**
     * SimpleStreamReader for bool arrays of a fixed size.
     */
    public static class BoolArrayReader implements SimpleStreamReader<boolean[]> {

        public final int count;

        public BoolArrayReader(final int count) {
            this.count = count;
        }

        @Override
        public int getStreamedSize() {
            return count;
        }

        @Override
        public boolean[] read(ByteBuffer stream) {
            final boolean[] arr = new boolean[count];
            for (int i = 0; i < count; i++) {
                arr[i] = stream.get() != 0;
            }
            return arr;
        }
    }

    /**
     * SimpleStreamReader for char arrays of a fixed size.
     */
    public static class CharArrayReader implements SimpleStreamReader<char[]> {

        public final int count;

        public CharArrayReader(final int count) {
            this.count = count;
        }

        @Override
        public int getStreamedSize() {
            return count;
        }

        @Override
        public char[] read(ByteBuffer stream) {
            final char[] arr = new char[count];
            for (int i = 0; i < count; i++) {
                arr[i] = (char) stream.get();
            }
            return arr;
        }
    }

    /**
     * SimpleStreamReader for signed char arrays of a fixed size.
     */
    public static class SCharArrayReader implements SimpleStreamReader<byte[]> {

        public final int count;

        public SCharArrayReader(final int count) {
            this.count = count;
        }

        @Override
        public int getStreamedSize() {
            return count;
        }

        @Override
        public byte[] read(ByteBuffer stream) {
            final byte[] arr = new byte[count];
            for (int i = 0; i < count; i++) {
                arr[i] = stream.get();
            }
            return arr;
        }
    }

    /**
     * SimpleStreamReader for unsigned char arrays of a fixed size.
     */
    public static class UCharArrayReader implements SimpleStreamReader<short[]> {

        public final int count;

        public UCharArrayReader(final int count) {
            this.count = count;
        }

        @Override
        public int getStreamedSize() {
            return count;
        }

        @Override
        public short[] read(ByteBuffer stream) {
            final short[] arr = new short[count];
            for (int i = 0; i < count; i++) {
                arr[i] = Unsigned.toUnsigned(stream.get());
            }
            return arr;
        }
    }

    /**
     * SimpleStreamReader for short arrays of a fixed size.
     */
    public static class ShortArrayReader implements SimpleStreamReader<short[]> {

        public final int count;

        public ShortArrayReader(final int count) {
            this.count = count;
        }

        @Override
        public int getStreamedSize() {
            return count * 2;
        }

        @Override
        public short[] read(ByteBuffer stream) {
            final short[] arr = new short[count];
            for (int i = 0; i < count; i++) {
                arr[i] = stream.getShort();
            }
            return arr;
        }
    }

    /**
     * SimpleStreamReader for unsigned short arrays of a fixed size.
     */
    public static class UShortArrayReader implements SimpleStreamReader<int[]> {

        public final int count;

        public UShortArrayReader(final int count) {
            this.count = count;
        }

        @Override
        public int getStreamedSize() {
            return count * 2;
        }

        @Override
        public int[] read(ByteBuffer stream) {
            final int[] arr = new int[count];
            for (int i = 0; i < count; i++) {
                arr[i] = Unsigned.toUnsigned(stream.getShort());
            }
            return arr;
        }
    }

    /**
     * SimpleStreamReader for int arrays of a fixed size.
     */
    public static class IntArrayReader implements SimpleStreamReader<int[]> {

        public final int count;

        public IntArrayReader(final int count) {
            this.count = count;
        }

        @Override
        public int getStreamedSize() {
            return count * 4;
        }

        @Override
        public int[] read(ByteBuffer stream) {
            final int[] arr = new int[count];
            for (int i = 0; i < count; i++) {
                arr[i] = stream.getInt();
            }
            return arr;
        }
    }

    /**
     * SimpleStreamReader for unsigned int arrays of a fixed size.
     */
    public static class UIntArrayReader implements SimpleStreamReader<long[]> {

        public final int count;

        public UIntArrayReader(final int count) {
            this.count = count;
        }

        @Override
        public int getStreamedSize() {
            return count * 4;
        }

        @Override
        public long[] read(ByteBuffer stream) {
            final long[] arr = new long[count];
            for (int i = 0; i < count; i++) {
                arr[i] = Unsigned.toUnsigned(stream.getInt());
            }
            return arr;
        }
    }

    /**
     * SimpleStreamReader for float arrays of a fixed size.
     */
    public static class FloatArrayReader implements SimpleStreamReader<float[]> {

        public final int count;

        public FloatArrayReader(final int count) {
            this.count = count;
        }

        @Override
        public int getStreamedSize() {
            return count * 4;
        }

        @Override
        public float[] read(ByteBuffer stream) {
            final float[] arr = new float[count];
            for (int i = 0; i < count; i++) {
                arr[i] = stream.getFloat();
            }
            return arr;
        }
    }

    /**
     * SimpleStreamReader for double arrays of a fixed size.
     */
    public static class DoubleArrayReader implements SimpleStreamReader<double[]> {

        public final int count;

        public DoubleArrayReader(final int count) {
            this.count = count;
        }

        @Override
        public int getStreamedSize() {
            return count * 8;
        }

        @Override
        public double[] read(ByteBuffer stream) {
            final double[] arr = new double[count];
            for (int i = 0; i < count; i++) {
                arr[i] = stream.getDouble();
            }
            return arr;
        }
    }

}
