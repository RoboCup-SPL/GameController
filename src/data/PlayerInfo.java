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
    public static final byte PENALTY_ILLEGAL_POSITIONING = 1;
    public static final byte PENALTY_MOTION_IN_SET = 2;
    public static final byte PENALTY_LOCAL_GAME_STUCK = 3;
    public static final byte PENALTY_INCAPABLE_ROBOT = 4;
    public static final byte PENALTY_PICK_UP = 5;
    public static final byte PENALTY_BALL_HOLDING = 6;
    public static final byte PENALTY_LEAVING_THE_FIELD = 7;
    public static final byte PENALTY_PLAYING_WITH_ARMS_HANDS = 8;
    public static final byte PENALTY_PUSHING = 9;
    public static final byte PENALTY_SENT_OFF = 10;
    public static final byte PENALTY_SUBSTITUTE = 11;

    /**
     * The size in bytes this class has packed.
     */
    public static final int SIZE
            = 1
            + // penalty
            1
            + // secsTillUnpenalised
            1
            + // warnings
            1;  // cautions

    //this is streamed
    public byte penalty = PENALTY_NONE; // penalty state of the player
    public byte secsTillUnpenalised;    // estimate of time till unpenalised
    public byte warnings;               // number of warnings
    public byte cautions;               // number of cautions (yellow cards)

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
        buffer.put(warnings);
        buffer.put(cautions);
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
        warnings = buffer.get();
        cautions = buffer.get();
    }

    public static String getPenaltyName(int penalty) {
        switch (penalty) {
            case PENALTY_NONE:
                return "none";
            case PENALTY_ILLEGAL_POSITIONING:
                return "illegal positioning";
            case PENALTY_MOTION_IN_SET:
                return "motion in set";
            case PENALTY_LOCAL_GAME_STUCK:
                return "local game stuck";
            case PENALTY_INCAPABLE_ROBOT:
                return "incapable robot";
            case PENALTY_PICK_UP:
                return "pick up";
            case PENALTY_BALL_HOLDING:
                return "ball holding";
            case PENALTY_PLAYING_WITH_ARMS_HANDS:
                return "playing with arms / hands";
            case PENALTY_LEAVING_THE_FIELD:
                return "leaving the field";
            case PENALTY_PUSHING:
                return "pushing";
            case PENALTY_SENT_OFF:
                return "sent off";
            case PENALTY_SUBSTITUTE:
                return "substitute";
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
        out += "           warnings: " + warnings + "\n";
        out += "           cautions: " + cautions + "\n";
        return out;
    }
}
