package data;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SPLStandardMessage implements Serializable {

    private static final long serialVersionUID = 2204681477211322628L;

    /**
     * Some constants from the C-structure.
     */
    public static final String SPL_STANDARD_MESSAGE_STRUCT_HEADER = "SPL ";
    public static final byte SPL_STANDARD_MESSAGE_STRUCT_VERSION = 5;
    public static final short SPL_STANDARD_MESSAGE_DATA_SIZE = 800;
    public static final int SIZE = 4 // header size
            + 1 // byte for the version
            + 1 // player number
            + 1 // team color
            + 1 // fallen
            + 12 // pose
            + 8 // walking target
            + 8 // shooting target
            + 4 // ball age
            + 8 // ball position
            + 8 // ball velocity
            + 2 // intention (with padding)
            + 2 // actual size of data
            + SPL_STANDARD_MESSAGE_DATA_SIZE;

    public String header;   // header to identify the structure
    public byte version;    // version of the data structure
    public byte playerNum;  // 1-5
    public byte teamColor;  // 0 is blue, 1 is red 
    public boolean fallen;  // whether the robot is fallen

    // position and orientation of robot
    // coordinates in millimeters
    // 0,0 is in centre of field
    // +ve x-axis points towards the goal we are attempting to score on
    // +ve y-axis is 90 degrees counter clockwise from the +ve x-axis
    // angle in radians, 0 along the +x axis, increasing counter clockwise
    public float[] pose;      // x,y,theta

    // the robot's target position on the field
    // the coordinate system is the same as for the pose
    // if the robot does not have any target, this attribute should be set to the robot's position
    public float[] walkingTo;

    // the target position of the next shot (either pass or goal shot)
    // the coordinate system is the same as for the pose
    // if the robot does not intend to shoot, this attribute should be set to the robot's position
    public float[] shootingTo;

    // Ball information
    public int ballAge;        // milliseconds since this robot last saw the ball. -1 if we haven't seen it

    // position of ball relative to the robot
    // coordinates in millimeters
    // 0,0 is in centre of the robot
    // +ve x-axis points forward from the robot
    // +ve y-axis is 90 degrees counter clockwise from the +ve x-axis
    public float[] ball;

    // velocity of the ball (same coordinate system as above)
    public float[] ballVel;

    // describes what the robot intends to do
    public enum Intention {

        NOTHING, // 0 - nothing particular (default)
        KEEPER, // 1 - wants to be keeper
        DEFENSE, // 2 - wants to play defense
        PLAY_BALL, // 3 - wants to play the ball
        LOST        // 4 - robot is lost
    }
    public Intention intention;

    // buffer for arbitrary data
    public byte[] data;

    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.put(header.getBytes());
        buffer.put(version);
        buffer.put(playerNum);
        buffer.put(teamColor);
        buffer.put(fallen ? (byte) 1 : (byte) 0);
        buffer.putFloat(pose[0]);
        buffer.putFloat(pose[1]);
        buffer.putFloat(pose[2]);
        buffer.putFloat(walkingTo[0]);
        buffer.putFloat(walkingTo[1]);
        buffer.putFloat(shootingTo[0]);
        buffer.putFloat(shootingTo[1]);
        buffer.putInt(ballAge);
        buffer.putFloat(ball[0]);
        buffer.putFloat(ball[1]);
        buffer.putFloat(ballVel[0]);
        buffer.putFloat(ballVel[1]);
        buffer.putShort((short) intention.ordinal());
        buffer.putShort((short) data.length);
        buffer.put(data);

        return buffer.array();
    }

    public boolean fromByteArray(ByteBuffer buffer) {
        try {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            byte[] header = new byte[4];
            buffer.get(header);
            this.header = new String(header);
            if (!this.header.equals(SPL_STANDARD_MESSAGE_STRUCT_HEADER)) {
                return false;
            } else {
                version = buffer.get();
                if (version != SPL_STANDARD_MESSAGE_STRUCT_VERSION) {
                    return false;
                } else {
                    playerNum = buffer.get();
                    if (playerNum < 1 || playerNum > 5) {
                        return false;
                    }

                    teamColor = buffer.get();
                    if (teamColor < 0 || teamColor > 1) {
                        return false;
                    }

                    switch (buffer.get()) {
                        case 0:
                            fallen = false;
                            break;
                        case 1:
                            fallen = true;
                            break;
                        default:
                            return false;
                    }

                    pose = new float[3];
                    pose[0] = buffer.getFloat();
                    pose[1] = buffer.getFloat();
                    pose[2] = buffer.getFloat();

                    walkingTo = new float[2];
                    walkingTo[0] = buffer.getFloat();
                    walkingTo[1] = buffer.getFloat();

                    shootingTo = new float[2];
                    shootingTo[0] = buffer.getFloat();
                    shootingTo[1] = buffer.getFloat();

                    ballAge = buffer.getInt();

                    ball = new float[2];
                    ball[0] = buffer.getFloat();
                    ball[1] = buffer.getFloat();

                    ballVel = new float[2];
                    ballVel[0] = buffer.getFloat();
                    ballVel[1] = buffer.getFloat();

                    int intention = (int) buffer.get();
                    buffer.get();
                    if (intention >= Intention.values().length) {
                        return false;
                    }
                    this.intention = Intention.values()[intention];

                    int numOfDataBytes = (int) buffer.getShort();
                    if (numOfDataBytes > SPL_STANDARD_MESSAGE_DATA_SIZE) {
                        return false;
                    }
                    data = new byte[numOfDataBytes];
                    buffer.get(data, 0, numOfDataBytes);

                    return true;
                }
            }
        } catch (RuntimeException e) {
            return false;
        }
    }
}
