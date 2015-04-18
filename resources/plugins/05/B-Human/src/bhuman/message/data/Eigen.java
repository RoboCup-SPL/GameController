package bhuman.message.data;

import java.nio.ByteBuffer;

/**
 *
 * @author Felix Thielke
 */
public abstract class Eigen {

    public static class Vector2i implements SimpleStreamReader<Vector2i> {

        public int x;
        public int y;

        @Override
        public int getStreamedSize() {
            return 8;
        }

        @Override
        public Vector2i read(final ByteBuffer stream) {
            x = stream.getInt();
            y = stream.getInt();
            return this;
        }

    }

    public static class Vector2f implements SimpleStreamReader<Vector2f> {

        public float x;
        public float y;

        @Override
        public int getStreamedSize() {
            return 8;
        }

        @Override
        public Vector2f read(final ByteBuffer stream) {
            x = stream.getFloat();
            y = stream.getFloat();
            return this;
        }

    }
}
