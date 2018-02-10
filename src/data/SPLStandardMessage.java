package data;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;

public class SPLStandardMessage implements Serializable {

    private static final long serialVersionUID = 2204681477211322628L;

    /**
     * Some constants from the C-structure.
     */
    public static final String SPL_STANDARD_MESSAGE_STRUCT_HEADER = "SPL ";
    public static final byte SPL_STANDARD_MESSAGE_STRUCT_VERSION = 7;
    public static final short SPL_STANDARD_MESSAGE_DATA_SIZE = 474;
    public static final int SIZE = 4 // header size
            + 1 // byte for the version
            + 1 // player number
            + 1 // team number
            + 1 // fallen
            + 12 // pose
            + 4 // ball age
            + 8 // ball position
            + 2 // actual size of data
            + SPL_STANDARD_MESSAGE_DATA_SIZE; // data

    public String header;   // header to identify the structure
    public byte version;    // version of the data structure
    public byte playerNum;  // 1-6
    public byte teamNum;    // the number of the team (as provided by the organizers)
    public boolean fallen;  // whether the robot is fallen

    // position and orientation of robot
    // coordinates in millimeters
    // 0,0 is in center of field
    // +ve x-axis points towards the goal we are attempting to score on
    // +ve y-axis is 90 degrees counter clockwise from the +ve x-axis
    // angle in radians, 0 along the +x axis, increasing counter clockwise
    public float[] pose = new float[3];      // x,y,theta

    // ball information
    public float ballAge;        // seconds since this robot last saw the ball. -1.f if we haven't seen it

    // position of ball relative to the robot
    // coordinates in millimeters
    // 0,0 is in centre of the robot
    // +ve x-axis points forward from the robot
    // +ve y-axis is 90 degrees counter clockwise from the +ve x-axis
    public float[] ball = new float[2];

    // buffer for arbitrary data
    public int nominalDataBytes;
    public byte[] data;

    public boolean valid = false;
    public boolean headerValid = false;
    public boolean versionValid = false;
    public boolean playerNumValid = false;
    public boolean teamNumValid = false;
    public boolean fallenValid = false;
    public boolean poseValid = false;
    public boolean ballValid = false;
    public boolean dataValid = false;

    public List<String> errors = new LinkedList<>();

    public static SPLStandardMessage createFrom(final SPLStandardMessage message) {
        final SPLStandardMessage m = new SPLStandardMessage();
        m.header = message.header;
        m.version = message.version;
        m.playerNum = message.playerNum;
        m.teamNum = message.teamNum;
        m.fallen = message.fallen;
        m.pose = message.pose;
        m.ballAge = message.ballAge;
        m.ball = message.ball;
        m.nominalDataBytes = message.nominalDataBytes;
        m.data = message.data;
        m.valid = message.valid;
        m.headerValid = message.headerValid;
        m.versionValid = message.versionValid;
        m.playerNumValid = message.playerNumValid;
        m.teamNumValid = message.teamNumValid;
        m.fallenValid = message.fallenValid;
        m.poseValid = message.poseValid;
        m.ballValid = message.ballValid;
        m.dataValid = message.dataValid;
        return m;
    }

    public byte[] toByteArray() {
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
                errors.add("wrong header; expected " + SPL_STANDARD_MESSAGE_STRUCT_HEADER + ", is: " + this.header);
            } else {
                headerValid = true;

                version = buffer.get();
                if (version != SPL_STANDARD_MESSAGE_STRUCT_VERSION) {
                    errors.add("wrong version; expected " + SPL_STANDARD_MESSAGE_STRUCT_VERSION + ", is: " + version);
                } else {
                    versionValid = true;

                    playerNum = buffer.get();
                    if (playerNum < 1 || playerNum > 6) {
                        errors.add("player number not within [1,6]; is: " + playerNum);
                    } else {
                        playerNumValid = true;
                    }

                    teamNum = buffer.get();
                    if (teamNum < 0) {
                        errors.add("team number not set");
                    } else {
                        teamNumValid = true;
                    }

                    final byte fallenState = buffer.get();
                    switch (fallenState) {
                        case 0:
                            fallen = false;
                            fallenValid = true;
                            break;
                        case 1:
                            fallen = true;
                            fallenValid = true;
                            break;
                        default:
                            errors.add("invalid fallen state; expected 0 or 1, is: " + fallenState);
                    }

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

                    nominalDataBytes = buffer.getShort();
                    boolean dValid = true;
                    if (nominalDataBytes > SPL_STANDARD_MESSAGE_DATA_SIZE) {
                        errors.add("custom data size too large; allowed up to " + SPL_STANDARD_MESSAGE_DATA_SIZE + ", is: " + nominalDataBytes);
                        dValid = false;
                    }
                    if (buffer.remaining() < nominalDataBytes) {
                        errors.add("custom data size is smaller than named: " + buffer.remaining() + " instead of " + nominalDataBytes);
                        dValid = false;
                    }
                    data = new byte[nominalDataBytes];
                    buffer.get(data, 0, nominalDataBytes);
                    dataValid = dValid;
                }
            }
        } catch (RuntimeException e) {
            errors.add("error while reading message: " + e.getClass().getSimpleName() + e.getMessage());
        }

        valid = headerValid && versionValid && playerNumValid && teamNumValid && fallenValid && poseValid && ballValid && dataValid;

        return valid;
    }
}
