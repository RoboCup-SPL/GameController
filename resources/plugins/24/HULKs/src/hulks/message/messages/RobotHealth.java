package hulks.message.messages;

import hulks.message.Message;
import hulks.message.data.ComplexStreamReader;
import hulks.message.data.EnumReader;
import hulks.message.data.NativeReaders;
import hulks.message.data.Primitive;
import hulks.message.data.SimpleStreamReader;
import hulks.message.data.StreamReader;
import hulks.message.data.StreamedObject;
import common.Log;
import java.nio.ByteBuffer;

/**
 *
 * @author Felix Thielke
 */
public class RobotHealth extends StreamedObject<RobotHealth> implements Message<RobotHealth> {

    public static enum MessageIds {
        motionFrameRate,
        avgMotionTime,
        maxMotionTime,
        minMotionTime,
        cognitionFrameRate,
        batteryLevel,
        totalCurrent,
        maxJointTemperatureStatus,
        jointWithMaxTemperature,
        cpuTemperature,
        load,
        memoryUsage,
        wlan,
        robotName,
        configuration,
        location,
        scenario
    }

    public static enum TemperatureStatus {
        regular,
        hot,
        veryHot,
        criticallyHot
    }

    public static enum Joint {
        headYaw,
        headPitch,
        lShoulderPitch,
        lShoulderRoll,
        lElbowYaw,
        lElbowRoll,
        lWristYaw,
        lHand, //< not an Angle, instead %

        rShoulderPitch,
        rShoulderRoll,
        rElbowYaw,
        rElbowRoll,
        rWristYaw,
        rHand, //< not an Angle, instead %

        lHipYawPitch,
        lHipRoll,
        lHipPitch,
        lKneePitch,
        lAnklePitch,
        lAnkleRoll,
        rHipYawPitch, //< not a joint in the real nao
        rHipRoll,
        rHipPitch,
        rKneePitch,
        rAnklePitch,
        rAnkleRoll,
    }

    public static enum Configuration {
        Debug,
        Develop,
        Release
    }

    /**
     * Frames per second within process "Motion".
     */
    public float motionFrameRate;
    /**
     * average execution time.
     */
    public float avgMotionTime;
    /**
     * Maximum execution time.
     */
    public float maxMotionTime;
    /**
     * Minimum execution time.
     */
    public float minMotionTime;

    /**
     * Frames per second within process "Cognition"
     */
    public float cognitionFrameRate;
    /**
     * Current batteryLevel of robot battery in percent
     */
    @Primitive("uchar")
    public short batteryLevel;
    /**
     * Sum of all motor currents ( as a measure for the robot's current load)
     */
    public float totalCurrent;
    /**
     * Highest temperature of a robot actuator
     */
    public TemperatureStatus maxJointTemperatureStatus = TemperatureStatus.regular;
    /**
     * The hottest joint.
     */
    public Joint jointWithMaxTemperature;
    /**
     * The temperature of the cpu
     */
    @Primitive("uchar")
    public short cpuTemperature;
    /**
     * load averages
     */
    public short[] load = new short[3];
    /**
     * Percentage of used memory
     */
    @Primitive("uchar")
    public short memoryUsage;
    /**
     * Status of the wlan hardware. true: wlan hardware is ok. false: wlan
     * hardware is (probably physically) broken.
     */
    public boolean wlan;
    /**
     * For fancier drawing :-)
     */
    public String robotName = "";
    /**
     * The configuration that was deployed.
     */
    public Configuration configuration;
    /**
     * The location selected.
     */
    public String location = "";
    /**
     * The scenario selected.
     */
    public String scenario = "";

    @Override
    public int getStreamedSize(final ByteBuffer stream) {
        final EnumReader<MessageIds> msgIdReader = new EnumReader<>(MessageIds.class);
        if (msgIdReader.getStreamedSize() > stream.remaining()) {
            return msgIdReader.getStreamedSize();
        }

        final int startPosition = stream.position();
        final MessageIds fieldToUpdate = msgIdReader.read(stream);
        int size = msgIdReader.getStreamedSize();
        switch (fieldToUpdate) {
            case load:
                size += 3;
                break;
            default: {
                try {
                    final StreamReader<?> reader = getFieldReader(getClass().getField(fieldToUpdate.name()));
                    size += SimpleStreamReader.class.isInstance(reader) ? SimpleStreamReader.class.cast(reader).getStreamedSize() : ComplexStreamReader.class.cast(reader).getStreamedSize(stream);
                } catch (NoSuchFieldException | SecurityException ex) {
                    Log.error("Could not get size of RobotHealth field " + fieldToUpdate.name());
                }
            }
        }

        stream.position(startPosition);
        return size;
    }

    @Override
    public RobotHealth read(final ByteBuffer stream) {
        final MessageIds fieldToUpdate = new EnumReader<>(MessageIds.class).read(stream);
        switch (fieldToUpdate) {
            case load:
                load = new NativeReaders.UCharArrayReader(3).read(stream);
                break;
            default: {
                try {
                    readField(stream, getClass().getField(fieldToUpdate.name()));
                } catch (NoSuchFieldException | SecurityException ex) {
                    Log.error("Could not set RobotHealth field " + fieldToUpdate.name());
                }
            }
        }

        return this;
    }

}
