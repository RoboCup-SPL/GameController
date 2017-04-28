package bhuman.message.messages;

import bhuman.message.Message;
import bhuman.message.data.Angle;
import bhuman.message.data.Eigen;
import bhuman.message.data.Primitive;
import bhuman.message.data.StreamedObject;
import java.util.EnumMap;

/**
 * Class for the BehaviourStatus message.
 *
 * @author Felix Thielke
 */
public class FieldFeatureOverview extends StreamedObject<FieldFeatureOverview> implements Message<FieldFeatureOverview> {

    public static enum Feature {

        PenaltyArea,
        MidCircle,
        MidCorner,
        OuterCorner,
        GoalFeature,
        GoalFrame
    }

    public static class FieldFeatureStatus extends StreamedObject<FieldFeatureStatus> {

        public Angle rotation;
        public Eigen.Vector2f translation;
        public boolean isValid;
        @Primitive("uint")
        public long lastSeen;
    }

    public FieldFeatureStatus combinedStatus;
    public EnumMap<Feature, FieldFeatureStatus> statuses;

}
