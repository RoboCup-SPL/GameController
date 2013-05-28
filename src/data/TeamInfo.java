package data;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * @author: Michel Bartsch
 * 
 * This class is part of the data wich are send to the robots.
 * It just represents this data, reads and writes between C-structure and
 * Java, nothing more.
 */
public class TeamInfo implements Serializable
{
    /**
     * How many players a team may have.
     * Actually that many players in each team need to be sent, even if
     * playersPerTeam in GameControlData is less.
     */
    public static final byte MAX_NUM_PLAYERS = 11;
    
    /** The size in bytes this class has packed. */
    public static final int SIZE =
            1 + // teamNumber
            1 + // teamColor
            1 + // goalColor
            1 + // score
            MAX_NUM_PLAYERS * PlayerInfo.SIZE;
    
    //this is streamed
    public byte teamNumber;                                         // unique team number
    public byte teamColor;                                          // colour of the team
    public byte goalColor;                                          // colour of the goal
    public byte score;                                              // team's score
    public PlayerInfo[] player = new PlayerInfo[MAX_NUM_PLAYERS];   // the team's players
    
    /**
     * Creates a new TeamInfo.
     */
    public TeamInfo()
    {
        for(int i=0; i<player.length; i++) {
            player[i] = new PlayerInfo();
        }
    }
    
    /**
     * Packing this Java class to the C-structure to be send.
     * @return Byte array representing the C-structure.
     */
    public byte[] toByteArray()
    {
        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(teamNumber);
        buffer.put(teamColor);
        buffer.put(goalColor);
        buffer.put(score);
        for(int i=0; i<MAX_NUM_PLAYERS; i++) {
            buffer.put(player[i].toByteArray());
        }
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
        teamNumber = buffer.get();
        teamColor = buffer.get();
        goalColor = buffer.get();
        score = buffer.get();
        for(int i=0; i<player.length; i++) {
            player[i].fromByteArray(buffer);
        }
    }
}