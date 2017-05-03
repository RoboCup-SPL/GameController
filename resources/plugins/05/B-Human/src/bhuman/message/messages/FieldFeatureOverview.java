package bhuman.message.messages;

import bhuman.message.Message;
import bhuman.message.data.Angle;
import bhuman.message.data.Eigen;
import bhuman.message.data.SimpleStreamReader;
import java.nio.ByteBuffer;
import java.util.EnumMap;
import util.Unsigned;

/**
 * Class for the BehaviourStatus message.
 *
 * @author Felix Thielke
 */
public class FieldFeatureOverview implements SimpleStreamReader<FieldFeatureOverview>, Message<FieldFeatureOverview> {

    public static enum Feature {

        PenaltyArea,
        MidCircle,
        MidCorner,
        OuterCorner,
        GoalFeature,
        GoalFrame
    }

    public static class FieldFeatureStatus {

        public Angle rotation;
        public Eigen.Vector2f translation;
        public boolean isValid;
        public boolean isRightSided;
        public long lastSeen;
    }

    public boolean isValid;
    public long lastSeen;
    public EnumMap<Feature, FieldFeatureStatus> statuses = new EnumMap<>(Feature.class);

    @Override
    public int getStreamedSize() {
        return 1 + 4 * Feature.values().length;
    }

    @Override
    public FieldFeatureOverview read(final ByteBuffer stream) {
        isValid = false;
        lastSeen = 0;

        final short isRightSidedContainer = Unsigned.toUnsigned(stream.get());

        int runner = 1 << (Feature.values().length - 1);
        for (final Feature f : Feature.values()) {
            final FieldFeatureStatus status = new FieldFeatureStatus();
            status.isRightSided = (isRightSidedContainer & runner) != 0;
            runner >>= 1;

            status.rotation = new Angle((float) (((double) stream.get()) * Math.PI / 127.0));
            status.translation = new Eigen.Vector2f((float) (((int) stream.get()) << 6), (float) (((int) stream.get()) << 6));
            status.lastSeen = ((long) Unsigned.toUnsigned(stream.get())) << 3;
            if (status.isValid = status.lastSeen < 300) {
                isValid = true;
            }
            lastSeen = Math.max(lastSeen, status.lastSeen);

            statuses.put(f, status);
        }

        return this;
    }

}
