package data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * @author: Michel Bartsch
 * 
 * This class is part of the data wich are send to the robots.
 * It just represents this data, reads and writes between C-structure and
 * Java, nothing more.
 */
public class PlayerInfo
{
    /** What type of penalty a player may have. */
    public static final short PENALTY_NONE = 0;
    public static final short PENALTY_SPL_BALL_HOLDING = 1;
    public static final short PENALTY_SPL_PLAYER_PUSHING = 2;
    public static final short PENALTY_SPL_OBSTRUCTION = 3;
    public static final short PENALTY_SPL_INACTIVE_PLAYER = 4;
    public static final short PENALTY_SPL_ILLEGAL_DEFENDER = 5;
    public static final short PENALTY_SPL_LEAVING_THE_FIELD = 6;
    public static final short PENALTY_SPL_PLAYING_WITH_HANDS = 7;
    public static final short PENALTY_SPL_REQUEST_FOR_PICKUP = 8;
    public static final short PENALTY_HL_BALL_MANIPULATION = 1;
    public static final short PENALTY_HL_ILLEGAL_ATTACK = 3;
    public static final short PENALTY_HL_ILLEGAL_DEFENSE = 4;
    public static final short PENALTY_HL_REQUEST_FOR_PICKUP = 5;
    public static final short PENALTY_MANUAL = 15;
    
    
    /** The size in bytes this class has packed. */
    public static final int SIZE =
            2 + // penalty
            2; // secsToUnpen
    
    //this is streamed
    public short penalty = PENALTY_NONE;    // penalty state of the player
    public short secsTillUnpenalised;       // estimate of time till unpenalised
    
    
    /**
     * Creates a new PlayerInfo.
     */
    public PlayerInfo() {}
    
    /**
     * Copy constructure.
     * 
     * @param player    Object to copy.
     */
    public PlayerInfo(PlayerInfo player)
    {
        penalty = player.penalty;
        secsTillUnpenalised = player.secsTillUnpenalised;
    }
    
    
    /**
     * Packing this Java class to the C-structure to be send.
     * @return Byte array representing the C-structure.
     */
    public byte[] toByteArray()
    {
        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort(penalty);
        buffer.putShort(secsTillUnpenalised);
        return buffer.array();
    }
    
    /**
     * Unpacking the C-structure to the Java class.
     * 
     * @param buffer    The buffered C-structure.
     */
    public void fromByteArray(ByteBuffer buffer)
    {
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        penalty = buffer.getShort();
        secsTillUnpenalised = buffer.getShort();
    }
}