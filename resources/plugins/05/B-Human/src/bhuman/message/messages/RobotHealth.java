package bhuman.message.messages;

import bhuman.message.Message;
import bhuman.message.data.ComplexStreamReader;
import bhuman.message.data.EnumReader;
import bhuman.message.data.NativeReaders;
import bhuman.message.data.Primitive;
import bhuman.message.data.SimpleStreamReader;
import bhuman.message.data.StreamReader;
import bhuman.message.data.StreamedObject;
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
        maxJointTemperature,
        jointWithMaxTemperature,
        cpuTemperature,
        load0,
        load1,
        load2,
        memoryUsage,
        robotName,
        ballPercepts,
        linePercepts,
        wlan,
        configuration,
        hash0,
        hash1,
        hash2,
        hash3,
        hash4,
        clean,
        location0,
        location1,
        location2
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
     * < Frames per second within process "Cognition"
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
    @Primitive("uchar")
    public short maxJointTemperature;
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
     * For fancier drawing :-)
     */
    public String robotName = "";
    /**
     * A ball percept counter used to determine ball percepts per hour
     */
    @Primitive("uint")
    public long ballPercepts;
    /**
     * A line percept counter used to determine line percepts per hour
     */
    @Primitive("uint")
    public long linePercepts;
    /**
     * Status of the wlan hardware. true: wlan hardware is ok. false: wlan
     * hardware is (probably physically) broken.
     */
    public boolean wlan;
    /**
     * The configuration that was deployed.
     */
    public Configuration configuration;
    /**
     * The first 5 digits of the hash of the git HEAD that was deployed.
     */
    public char[] hash = new char[5];
    /**
     * Was the working copy clean when it was deployed?
     */
    public boolean clean;
    /**
     * The first 3 letters of the location selected.
     */
    public char[] location = new char[3];

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
            case load0:
            case load1:
            case load2:
            case hash0:
            case hash1:
            case hash2:
            case hash3:
            case hash4:
            case location0:
            case location1:
            case location2:
                size += 1;
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
            case load0:
                load[0] = NativeReaders.ucharReader.read(stream);
                break;
            case load1:
                load[1] = NativeReaders.ucharReader.read(stream);
                break;
            case load2:
                load[2] = NativeReaders.ucharReader.read(stream);
                break;
            case hash0:
                hash[0] = NativeReaders.charReader.read(stream);
                break;
            case hash1:
                hash[1] = NativeReaders.charReader.read(stream);
                break;
            case hash2:
                hash[2] = NativeReaders.charReader.read(stream);
                break;
            case hash3:
                hash[3] = NativeReaders.charReader.read(stream);
                break;
            case hash4:
                hash[4] = NativeReaders.charReader.read(stream);
                break;
            case location0:
                location[0] = NativeReaders.charReader.read(stream);
                break;
            case location1:
                location[1] = NativeReaders.charReader.read(stream);
                break;
            case location2:
                location[2] = NativeReaders.charReader.read(stream);
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
