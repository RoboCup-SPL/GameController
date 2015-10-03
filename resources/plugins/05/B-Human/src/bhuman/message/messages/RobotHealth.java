package bhuman.message.messages;

import bhuman.message.Message;
import bhuman.message.data.EnumReader;
import bhuman.message.data.JointData;
import bhuman.message.data.NativeReaders;
import java.nio.ByteBuffer;

/**
 * Class for the RobotHealth message.
 *
 * @author Felix Thielke
 */
public class RobotHealth extends Message<RobotHealth> {

    /**
     * Configurations that can be deployed- Note that they must start with an
     * uppercase letter.
     */
    public static enum Configuration {

        Debug,
        Develop,
        Release
    };

    private static final EnumReader<Configuration> configurationReader = new EnumReader<Configuration>(Configuration.class);

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
     * Frames per second within process "Cognition".
     */
    public float cognitionFrameRate;
    /**
     * Current batteryLevel of robot battery in percent.
     */
    public short batteryLevel;
    /**
     * Sum of all motor currents ( as a measure for the robot's current load).
     */
    public float totalCurrent;
    /**
     * Highest temperature of a robot actuator.
     */
    public short maxJointTemperature;
    /**
     * The hottest joint.
     */
    public JointData.Joint jointWithMaxTemperature;
    /**
     * The temperature of the cpu.
     */
    public short cpuTemperature;
    /**
     * load averages.
     */
    public short[] load;
    /**
     * Percentage of used memory.
     */
    public short memoryUsage;
    /**
     * For fancier drawing :-).
     */
    public String robotName;
    /**
     * A ball percept counter used to determine ball percepts per hour.
     */
    public long ballPercepts;
    /**
     * A line percept counter used to determine line percepts per hour.
     */
    public long linePercepts;
    /**
     * A goal percept counter used to determine goal percepts per hour.
     */
    public long goalPercepts;
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
    public String hash;
    /**
     * Was the working copy clean when it was deployed?.
     */
    public boolean clean;
    /**
     * The first 3 letters of the location selected.
     */
    public String location;

    @Override
    public RobotHealth read(final ByteBuffer stream) {
        if (stream.remaining() < NativeReaders.floatReader.getStreamedSize() * 6
                + NativeReaders.ucharReader.getStreamedSize() * 4
                + new EnumReader<>(JointData.Joint.class).getStreamedSize()
                + new NativeReaders.UCharArrayReader(3).getStreamedSize()) {
            return null;
        }
        motionFrameRate = NativeReaders.floatReader.read(stream);
        avgMotionTime = NativeReaders.floatReader.read(stream);
        maxMotionTime = NativeReaders.floatReader.read(stream);
        minMotionTime = NativeReaders.floatReader.read(stream);
        cognitionFrameRate = NativeReaders.floatReader.read(stream);
        batteryLevel = NativeReaders.ucharReader.read(stream);
        totalCurrent = NativeReaders.floatReader.read(stream);
        maxJointTemperature = NativeReaders.ucharReader.read(stream);
        jointWithMaxTemperature = new EnumReader<>(JointData.Joint.class).read(stream);
        cpuTemperature = NativeReaders.ucharReader.read(stream);
        load = new NativeReaders.UCharArrayReader(3).read(stream);
        memoryUsage = NativeReaders.ucharReader.read(stream);

        if (stream.remaining() != NativeReaders.stringReader.getStreamedSize(stream)
                + NativeReaders.uintReader.getStreamedSize() * 3
                + NativeReaders.boolReader.getStreamedSize() * 2
                + configurationReader.getStreamedSize()
                + new NativeReaders.SimpleStringReader(5).getStreamedSize()
                + new NativeReaders.SimpleStringReader(3).getStreamedSize()) {
            return null;
        }
        robotName = NativeReaders.stringReader.read(stream);
        ballPercepts = NativeReaders.uintReader.read(stream);
        linePercepts = NativeReaders.uintReader.read(stream);
        goalPercepts = NativeReaders.uintReader.read(stream);
        wlan = NativeReaders.boolReader.read(stream);
        configuration = configurationReader.read(stream);
        hash = new NativeReaders.SimpleStringReader(5).read(stream);
        clean = NativeReaders.boolReader.read(stream);
        location = new NativeReaders.SimpleStringReader(3).read(stream);

        return this;
    }

}
