package bhuman.message.data;

import java.nio.ByteBuffer;

/**
 * Abstract class containing classes for Eigen types.
 *
 * @author Felix Thielke
 */
public abstract class Eigen {

    /**
     * Class for a Vector of two ints.
     */
    public static class Vector2i extends Vector2<Integer> {

        /**
         * Constructor.
         */
        public Vector2i() {
            super(NativeReaders.intReader);
        }

        @Override
        public Vector2i read(ByteBuffer stream) {
            return (Vector2i) super.read(stream);
        }
    }

    /**
     * Class for a Vector of two floats.
     */
    public static class Vector2f extends Vector2<Float> {

        /**
         * Constructor.
         */
        public Vector2f() {
            super(NativeReaders.floatReader);
        }

        /**
         * Constructor.
         *
         * @param x x value
         * @param y y value
         */
        public Vector2f(final float x, final float y) {
            super(NativeReaders.floatReader);
            this.x = x;
            this.y = y;
        }

        @Override
        public Vector2f read(ByteBuffer stream) {
            return (Vector2f) super.read(stream);
        }

        /**
         * Returns the difference of this vector and another.
         *
         * @param other other vector
         * @return difference
         */
        public Vector2f diff(final Vector2f other) {
            return new Vector2f(other.x - x, other.y - y);
        }

        /**
         * Scales this vector.
         *
         * @param factor scaling factor
         * @return resulting vector
         */
        public Vector2f scale(final float factor) {
            return new Vector2f(x * factor, y * factor);
        }

        /**
         * Inversely scales this vector.
         *
         * @param factor scaling factor
         * @return resulting factor
         */
        public Vector2f invScale(final float factor) {
            return new Vector2f(x / factor, y / factor);
        }
    }

    /**
     * Class for a Vector of two shorts.
     */
    public static class Vector2s extends Vector2<Short> {

        /**
         * Constructor.
         */
        public Vector2s() {
            super(NativeReaders.shortReader);
        }

        @Override
        public Vector2s read(ByteBuffer stream) {
            return (Vector2s) super.read(stream);
        }
    }

    private static abstract class Vector2<T extends Number> implements SimpleStreamReader<Vector2<T>> {

        public T x;
        public T y;
        private final SimpleStreamReader<T> reader;

        public Vector2(final SimpleStreamReader<T> reader) {
            this.reader = reader;
        }

        @Override
        public int getStreamedSize() {
            return reader.getStreamedSize() * 2;
        }

        @Override
        public Vector2<T> read(final ByteBuffer stream) {
            x = reader.read(stream);
            y = reader.read(stream);
            return this;
        }

        /**
         * Returns the norm of the vector.
         *
         * @return norm
         */
        public double norm() {
            return Math.hypot(x.doubleValue(), y.doubleValue());
        }
    }
}
