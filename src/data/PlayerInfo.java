package data;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This class is part of the data which are sent to the robots. It just
 * represents this data, reads and writes between C-structure and Java, nothing
 * more.
 *
 * @author Michel Bartsch
 */
public class PlayerInfo implements Serializable {

    private static final long serialVersionUID = -8480462279073509072L;

    /**
     * What type of penalty a player may have.
     */
    public static final byte PENALTY_NONE = 0;

    public static final byte PENALTY_SPL_ILLEGAL_BALL_CONTACT = 1;
    public static final byte PENALTY_SPL_PLAYER_PUSHING = 2;
    public static final byte PENALTY_SPL_ILLEGAL_MOTION_IN_SET = 3;
    public static final byte PENALTY_SPL_INACTIVE_PLAYER = 4;
    public static final byte PENALTY_SPL_ILLEGAL_POSITION = 5;
    public static final byte PENALTY_SPL_LEAVING_THE_FIELD = 6;
    public static final byte PENALTY_SPL_REQUEST_FOR_PICKUP = 7;
    public static final byte PENALTY_SPL_LOCAL_GAME_STUCK = 8;
    public static final byte PENALTY_SPL_ILLEGAL_POSITION_IN_SET = 9;
    public static final byte PENALTY_SPL_PLAYER_STANCE = 10;

    public static final byte PENALTY_SUBSTITUTE = 14;
    public static final byte PENALTY_MANUAL = 15;

    /**
     * The size in bytes this class has packed.
     */
    public static final int SIZE
            = 1
            + // penalty
            1;  // secsTillUnpenalised

    //this is streamed
    public byte penalty = PENALTY_NONE; // penalty state of the player
    public byte secsTillUnpenalised;    // estimate of time till unpenalised

    /**
     * Packing this Java class to the C-structure to be send.
     *
     * @return Byte array representing the C-structure.
     */
    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(penalty);
        buffer.put(secsTillUnpenalised);
        return buffer.array();
    }

    /**
     * Unpacking the C-structure to the Java class.
     *
     * @param buffer The buffered C-structure.
     */
    public void fromByteArray(ByteBuffer buffer) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        penalty = buffer.get();
        secsTillUnpenalised = buffer.get();
    }

    public static String getPenaltyName(int penalty) {
        switch (penalty) {
            case PENALTY_NONE:
                return "none";
            case PENALTY_SPL_ILLEGAL_BALL_CONTACT:
                return "illegal ball contact";
            case PENALTY_SPL_PLAYER_PUSHING:
                return "pushing";
            case PENALTY_SPL_ILLEGAL_MOTION_IN_SET:
                return "illegal motion in set";
            case PENALTY_SPL_INACTIVE_PLAYER:
                return "inactive";
            case PENALTY_SPL_ILLEGAL_POSITION:
                return "illegal position";
            case PENALTY_SPL_LEAVING_THE_FIELD:
                return "leaving the field";
            case PENALTY_SPL_REQUEST_FOR_PICKUP:
                return "request for pickup";
            case PENALTY_SPL_LOCAL_GAME_STUCK:
                return "local game stuck";
            case PENALTY_SPL_ILLEGAL_POSITION_IN_SET:
                return "illegal position in set";
            case PENALTY_SPL_PLAYER_STANCE:
                return "player stance";
            case PENALTY_SUBSTITUTE:
                return "substitute";
            case PENALTY_MANUAL:
                return "manual";
            default:
                return "undefined(" + penalty + ")";
        }
    }

    @Override
    public String toString() {
        String out = "";
        String temp = getPenaltyName(penalty);
        out += "            penalty: " + temp + "\n";
        out += "secsTillUnpenalised: " + secsTillUnpenalised + "\n";
        return out;
    }
}
