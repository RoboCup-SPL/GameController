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

        @Override
        public Vector2f read(ByteBuffer stream) {
            return (Vector2f) super.read(stream);
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
