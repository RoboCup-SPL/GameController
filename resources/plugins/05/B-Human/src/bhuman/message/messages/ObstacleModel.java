package bhuman.message.messages;

import bhuman.message.Message;
import bhuman.message.data.ComplexStreamReader;
import bhuman.message.data.Eigen;
import bhuman.message.data.StreamedObject;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Class for the ObstacleModel message.
 *
 * @author Felix Thielke
 */
public class ObstacleModel implements ComplexStreamReader<ObstacleModel>, Message<ObstacleModel> {

    public static class Obstacle extends StreamedObject<Obstacle> {

        public float covXX;
        public float covXY;
        public float covYY;
        public Eigen.Vector2f left;
        public Eigen.Vector2f right;
    }

    public List<Obstacle> obstacles = new LinkedList<>();

    @Override
    public int getStreamedSize(final ByteBuffer stream) {
        return stream.remaining() - (stream.remaining() % new Obstacle().getStreamedSize(stream));
    }

    @Override
    public ObstacleModel read(final ByteBuffer stream) {
        obstacles.clear();
        while (true) {
            final Obstacle o = new Obstacle();
            if (stream.remaining() < o.getStreamedSize(stream)) {
                return this;
            }
            o.read(stream);
            obstacles.add(o);
        }
    }
}
