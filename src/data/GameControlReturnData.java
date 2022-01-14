package data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * This class is what robots send to the GameController.
 * It just represents this data, reads and writes between C-structure and
 * Java, nothing more.
 *
 * @author Michel Bartsch
 */
public class GameControlReturnData
{
    /** The header to identify the structure. */
    public static final String GAMECONTROLLER_RETURN_STRUCT_HEADER = "RGrt";
    /** The version of the data structure. */
    public static final byte GAMECONTROLLER_RETURN_STRUCT_VERSION = 4;


    /** The size in bytes this class has packed. */
    public static final int SIZE =
            4 + // header
            1 + // version
            1 + // playerNum
            1 + // teamNum
            1 + // fallen
            12 + // pose
            4 + // ball age
            8; // ball position

    //this is streamed
    String header;          // header to identify the structure
    byte version;           // version of the data structure
    public byte playerNum;  // player number
    public byte teamNum;    // unique team number
    public boolean fallen;  // whether the robot is fallen

    // position and orientation of robot
    // coordinates in millimeters
    // 0,0 is in center of field
    // +ve x-axis points towards the goal we are attempting to score on
    // +ve y-axis is 90 degrees counter clockwise from the +ve x-axis
    // angle in radians, 0 along the +x axis, increasing counter clockwise
    public float[] pose = new float[3];      // x,y,theta

    // ball information
    public float ballAge;   // seconds since this robot last saw the ball. -1.f if we haven't seen it

    // position of ball relative to the robot
    // coordinates in millimeters
    // 0,0 is in centre of the robot
    // +ve x-axis points forward from the robot
    // +ve y-axis is 90 degrees counter clockwise from the +ve x-axis
    public float[] ball = new float[2];

    /**
     * Packing this Java class to the C-structure to be send.
     * @return Byte array representing the C-structure.
     */
    public byte[] toByteArray()
    {
        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.put(header.getBytes());
        buffer.put(version);
        buffer.put(playerNum);
        buffer.put(teamNum);
        buffer.put(fallen ? (byte) 1 : (byte) 0);
        buffer.putFloat(pose[0]);
        buffer.putFloat(pose[1]);
        buffer.putFloat(pose[2]);
        buffer.putFloat(ballAge);
        buffer.putFloat(ball[0]);
        buffer.putFloat(ball[1]);
        return buffer.array();
    }

    /**
     * Changes the state of this object to the state of the given byte-stream.
     *
     * @param buffer    the byte-stream to parse
     * @return          returns true if and only if the state of the object could be changed, false otherwise
     */
    public boolean fromByteArray(ByteBuffer buffer)
    {
        try {
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            byte[] header = new byte[4];
            buffer.get(header, 0, 4);
            this.header = new String(header);
            if (!this.header.equals(GAMECONTROLLER_RETURN_STRUCT_HEADER)) {
                return false;
            } else {
                version = buffer.get();
                switch (version) {
                case GAMECONTROLLER_RETURN_STRUCT_VERSION:
                    playerNum = buffer.get();
                    teamNum = buffer.get();
                    fallen = buffer.get() != 0;
                    pose[0] = buffer.getFloat();
                    pose[1] = buffer.getFloat();
                    pose[2] = buffer.getFloat();
                    ballAge = buffer.getFloat();
                    ball[0] = buffer.getFloat();
                    ball[1] = buffer.getFloat();
                    return true;

                default:
                    break;
                }

                return false;
            }
        } catch (RuntimeException e) {
            return false;
        }
    }
}
