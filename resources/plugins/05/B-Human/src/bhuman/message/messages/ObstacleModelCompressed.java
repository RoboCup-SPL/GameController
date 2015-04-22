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
 *
 * @author Felix Thielke
 */
public class ObstacleModelCompressed extends Message<ObstacleModelCompressed> {
    
    public static class Obstacle implements SimpleStreamReader<Obstacle> {
        
        @Override
        public int getStreamedSize() {
            return NativeReaders.floatReader.getStreamedSize() * 3
                    + new Eigen.Vector2f().getStreamedSize() * 3
                    + new EnumReader<Type>(Type.class).getStreamedSize();
        }
        
        public static enum Type {
            
            ULTRASOUND, GOALPOST, UNKNOWN, SOMEROBOT, OPPONENT, TEAMMATE, FALLENSOMEROBOT, FALLENOPPONENT, FALLENTEAMMATE
        }
        
        public float covXX, covYY, covXY;
        public Eigen.Vector2f center, left, right;
        public Type type;
        
        @Override
        public Obstacle read(ByteBuffer stream) {
            covXX = NativeReaders.floatReader.read(stream);
            covYY = NativeReaders.floatReader.read(stream);
            covXY = NativeReaders.floatReader.read(stream);
            
            center = new Eigen.Vector2f().read(stream);
            left = new Eigen.Vector2f().read(stream);
            right = new Eigen.Vector2f().read(stream);
            
            type = new EnumReader<Type>(Type.class).read(stream);
            
            return this;
        }
    }
    
    public List<Obstacle> obstacles;
    
    @Override
    public ObstacleModelCompressed read(ByteBuffer stream) {
        final ArrayReader<Obstacle> reader = new ArrayReader<Obstacle>(Obstacle.class);
        if (stream.remaining() != reader.getStreamedSize(stream)) {
            return null;
        }
        obstacles = reader.read(stream);
        return this;
    }
}
