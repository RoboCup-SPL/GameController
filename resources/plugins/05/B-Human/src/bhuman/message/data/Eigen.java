package bhuman.message.data;

import java.nio.ByteBuffer;

/**
 *
 * @author Felix Thielke
 */
public abstract class Eigen {

    public static class Vector2i extends Vector2<Integer> {

        public Vector2i() {
            super(NativeReaders.intReader);
        }

        @Override
        public Vector2i read(ByteBuffer stream) {
            return (Vector2i) super.read(stream);
        }

    }

    public static class Vector2f extends Vector2<Float> {

        public Vector2f() {
            super(NativeReaders.floatReader);
        }

        public Vector2f(final float x, final float y) {
            super(NativeReaders.floatReader);
            this.x = x;
            this.y = y;
        }

        @Override
        public Vector2f read(ByteBuffer stream) {
            return (Vector2f) super.read(stream);
        }

        public double norm() {
            return Math.hypot(x, y);
        }

        public Vector2f diff(final Vector2f other) {
            return new Vector2f(other.x - x, other.y - y);
        }

        public Vector2f scale(final float factor) {
            return new Vector2f(x * factor, y * factor);
        }

        public Vector2f invScale(final float factor) {
            return new Vector2f(x / factor, y / factor);
        }
    }

    private static abstract class Vector2<T> implements SimpleStreamReader<Vector2<T>> {

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
    }
}
