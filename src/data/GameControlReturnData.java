package data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * @author: Michel Bartsch
 * 
 * This class is what robots send to the GameCOntroller.
 * It just represents this data, reads and writes between C-structure and
 * Java, nothing more.
 */
public class GameControlReturnData
{
    /** The header to identify the structure. */
    public static final String GAMECONTROLLER_RETURN_STRUCT_HEADER = "RGrt";
    /** The version of the data structure. */
    public static final int GAMECONTROLLER_RETURN_STRUCT_VERSION = 1;
    
    /** What a player may say. */
    public static final int GAMECONTROLLER_RETURN_MSG_MAN_PENALISE = 0;
    public static final int GAMECONTROLLER_RETURN_MSG_MAN_UNPENALISE = 1;
    public static final int GAMECONTROLLER_RETURN_MSG_ALIVE = 2;
    
    
    /** The size in bytes this class has packed. */
    public static final int SIZE =
            4 + // header
            4 + // version
            2 + // team
            2 + // player
            4; // message
    
    //this is streamed
    String header;          // header to identify the structure
    int version;            // version of the data structure
    public short team;      // unique team number
    public short player;    // player number
    public int message;     // what the player says
    
    
    /**
     * Packing this Java class to the C-structure to be send.
     * @return Byte array representing the C-structure.
     */
    public byte[] toByteArray()
    {
        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.put(header.getBytes());
        buffer.putInt(version);
        buffer.putShort(team);
        buffer.putShort(player);
        buffer.putInt(message);
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

            if(!this.header.equals(GAMECONTROLLER_RETURN_STRUCT_HEADER)) {
                return false;
            } else {
                version = buffer.getInt();
                team = buffer.getShort();
                player = buffer.getShort();
                message = buffer.getInt();

                return true;
            }
        } catch (RuntimeException e) {
            return false;
        }
    }
}