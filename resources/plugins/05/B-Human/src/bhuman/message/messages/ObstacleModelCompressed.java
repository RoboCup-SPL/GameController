package bhuman.message.messages;

import bhuman.message.Message;
import bhuman.message.data.ArrayReader;
import bhuman.message.data.Eigen;
import bhuman.message.data.EnumReader;
import bhuman.message.data.NativeReaders;
import bhuman.message.data.SimpleStreamReader;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Class for the ObstacleModelCompressed message.
 *
 * @author Felix Thielke
 */
public class ObstacleModelCompressed extends Message<ObstacleModelCompressed> {

    public static class Obstacle implements SimpleStreamReader<Obstacle> {

        @Override
        public int getStreamedSize() {
            return NativeReaders.floatReader.getStreamedSize() * 3
                    + new Eigen.Vector2s().getStreamedSize() * 3
                    + NativeReaders.uintReader.getStreamedSize()
                    + new EnumReader<>(Type.class).getStreamedSize();
        }

        /**
         * The type of an obstacle.
         */
        public static enum Type {

            GOALPOST, UNKNOWN, SOMEROBOT, OPPONENT, TEAMMATE, FALLENSOMEROBOT, FALLENOPPONENT, FALLENTEAMMATE
        }

        /**
         * Covariance matrix of an obstacle.
         */
        public float covXX, covYY, covXY;
        /**
         * Center point of an obstacle.
         */
        public Eigen.Vector2s center;
        /**
         * Left point of an obstacle.
         */
        public Eigen.Vector2s left;
        /**
         * Right point of an obstacle.
         */
        public Eigen.Vector2s right;
        /**
         * Classified type (see enum above).
         */
        public Type type;
        /**
         * Timestamp of the last measurement of an obstacle.
         */
        public long lastSeen;

        @Override
        public Obstacle read(ByteBuffer stream) {
            covXX = NativeReaders.floatReader.read(stream);
            covYY = NativeReaders.floatReader.read(stream);
            covXY = NativeReaders.floatReader.read(stream);

            center = new Eigen.Vector2s().read(stream);
            left = new Eigen.Vector2s().read(stream);
            right = new Eigen.Vector2s().read(stream);

            lastSeen = NativeReaders.uintReader.read(stream);

            type = new EnumReader<>(Type.class).read(stream);
            if (type == null) {
                return null;
            }

            return this;
        }
    }

    /**
     * List of obstacles (all entries are somewhat valid obstacles).
     */
    public List<Obstacle> obstacles;

    @Override
    public ObstacleModelCompressed read(ByteBuffer stream) {
        final ArrayReader<Obstacle> reader = new ArrayReader<>(Obstacle.class);
        if (stream.remaining() != reader.getStreamedSize(stream)) {
            return null;
        }
        obstacles = reader.read(stream);
        obstacles.remove(null);
        return this;
    }
}
