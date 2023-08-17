package data;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This class is what robots send to the GameController.
 * It just represents this data, reads and writes between C-structure and
 * Java, nothing more.
 *
 * @author Michel Bartsch
 */
public class GameControlReturnData implements Serializable {

    private static final long serialVersionUID = -6438146236177558310L;

    public static final int GAMECONTROLLER_RETURNDATA_PORT = 3939; // port to receive return-packets on
    public static final int GAMECONTROLLER_RETURNDATA_FORWARD_PORT = GAMECONTROLLER_RETURNDATA_PORT + 1; // port on which return-packets are forwarded (from GC to TCMs)

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
    public String header;   // header to identify the structure
    public byte version;    // version of the data structure
    public byte playerNum;  // player number
    public byte teamNum;    // unique team number
    public boolean fallen;  // whether the robot is fallen

    // position and orientation of robot
    // coordinates in millimeters
    // 0,0 is in center of field
    // +ve x-axis points towards the goal we are attempting to score on
    // +ve y-axis is 90 degrees counter clockwise from the +ve x-axis
    // angle in radians, 0 along the +x axis, increasing counter clockwise
    public final float[] pose = new float[3];      // x,y,theta

    // ball information
    public float ballAge;   // seconds since this robot last saw the ball. -1.f if we haven't seen it

    // position of ball relative to the robot
    // coordinates in millimeters
    // 0,0 is in centre of the robot
    // +ve x-axis points forward from the robot
    // +ve y-axis is 90 degrees counter clockwise from the +ve x-axis
    public final float[] ball = new float[2];

    public boolean valid = false;
    public boolean headerValid = false;
    public boolean versionValid = false;
    public boolean playerNumValid = false;
    public boolean teamNumValid = false;
    public boolean fallenValid = false;
    public boolean poseValid = false;
    public boolean ballValid = false;

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
            buffer.get(header);
            this.header = new String(header);
            if (this.header.equals(GAMECONTROLLER_RETURN_STRUCT_HEADER)) {
                headerValid = true;

                version = buffer.get();
                if (version == GAMECONTROLLER_RETURN_STRUCT_VERSION) {
                    versionValid = true;

                    playerNum = buffer.get();
                    playerNumValid = playerNum >= 1 && playerNum <= TeamInfo.MAX_NUM_PLAYERS;

                    teamNum = buffer.get();
                    teamNumValid = teamNum > 0;

                    final byte fallenState = buffer.get();
                    fallen = fallenState != 0;
                    fallenValid = fallenState == 0 || fallenState == 1;

                    pose[0] = buffer.getFloat();
                    pose[1] = buffer.getFloat();
                    pose[2] = buffer.getFloat();
                    if (!Float.isNaN(pose[0]) && !Float.isNaN(pose[1]) && !Float.isNaN(pose[2])) {
                        poseValid = true;
                    }

                    ballAge = buffer.getFloat();

                    ball[0] = buffer.getFloat();
                    ball[1] = buffer.getFloat();
                    if (!Float.isNaN(ballAge) && !Float.isNaN(ball[0]) && !Float.isNaN(ball[1])) {
                        ballValid = true;
                    }
                }
            }
        } catch (RuntimeException e) {
            valid = false;
            return false;
        }

        valid = headerValid && versionValid && playerNumValid && teamNumValid && fallenValid && poseValid && ballValid;

        return valid;
    }
}
