package data;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This class is part of the data wich are send to the robots. It just
 * represents this data, reads and writes between C-structure and Java, nothing
 * more.
 *
 * @author Michel Bartsch
 */
public class TeamInfo implements Serializable {

    private static final long serialVersionUID = 2795660408542807763L;

    /**
     * How many players a team may have. Actually that many players in each team
     * need to be sent, even if playersPerTeam in GameControlData is less.
     */
    public static final byte MAX_NUM_PLAYERS = 20;

    /**
     * The size in bytes this class has packed.
     */
    public static final int SIZE
            = 1
            + // teamNumber
            1
            + // fieldPlayerColor
            1
            + // goalkeeperColor
            1
            + // goalkeeper
            1
            + // score
            1
            + // penaltyShot
            2
            + // singleShots
            2
            + // messageBudget
            MAX_NUM_PLAYERS * PlayerInfo.SIZE;

    //this is streamed
    public byte teamNumber;                                         // unique team number
    public byte fieldPlayerColor;                                   // color of the field players
    public byte goalkeeperColor;                                    // color of the goalkeeper
    public byte goalkeeper = 1;                                     // player number of the goalkeeper (1-MAX_NUM_PLAYERS)
    public byte score;                                              // team's score
    public byte penaltyShot = 0;                                    // penalty shot counter
    public short singleShots = 0;                                   // bits represent penalty shot success
    public short messageBudget = 0;                                 // number of team messages the team is allowed to send for the remainder of the game
    public final PlayerInfo[] player = new PlayerInfo[MAX_NUM_PLAYERS];   // the team's players

    /**
     * Creates a new TeamInfo.
     */
    public TeamInfo() {
        for (int i = 0; i < player.length; i++) {
            player[i] = new PlayerInfo();
        }
    }

    /**
     * Packing this Java class to the C-structure to be send.
     *
     * @param decreaseScore Whether the score should be decreased by one before sending.
     *
     * @return Byte array representing the C-structure.
     */
    public byte[] toByteArray(boolean decreaseScore) {
        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(teamNumber);
        buffer.put(fieldPlayerColor);
        buffer.put(goalkeeperColor);
        buffer.put(goalkeeper);
        buffer.put(decreaseScore ? (byte) (score - 1) : score);
        buffer.put(penaltyShot);
        buffer.putShort(singleShots);
        buffer.putShort(messageBudget);
        for (int i = 0; i < MAX_NUM_PLAYERS; i++) {
            buffer.put(player[i].toByteArray());
        }

        return buffer.array();
    }

    /**
     * Unpacking the C-structure to the Java class.
     *
     * @param buffer The buffered C-structure.
     */
    public void fromByteArray(ByteBuffer buffer) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        teamNumber = buffer.get();
        fieldPlayerColor = buffer.get();
        goalkeeperColor = buffer.get();
        goalkeeper = buffer.get();
        score = buffer.get();
        penaltyShot = buffer.get();
        singleShots = buffer.getShort();
        messageBudget = buffer.getShort();
        for (PlayerInfo info : player) {
            info.fromByteArray(buffer);
        }
    }

    public static String getTeamColorName(byte teamColor) {
        switch (teamColor) {
            case GameControlData.TEAM_BLUE:
                return "blue";
            case GameControlData.TEAM_RED:
                return "red";
            case GameControlData.TEAM_YELLOW:
                return "yellow";
            case GameControlData.TEAM_BLACK:
                return "black";
            case GameControlData.TEAM_WHITE:
                return "white";
            case GameControlData.TEAM_GREEN:
                return "green";
            case GameControlData.TEAM_ORANGE:
                return "orange";
            case GameControlData.TEAM_PURPLE:
                return "purple";
            case GameControlData.TEAM_BROWN:
                return "brown";
            case GameControlData.TEAM_GRAY:
                return "gray";
            default:
                return "undefined(" + teamColor + ")";
        }
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("--------------------------------------\n");

        out.append("         teamNumber: ").append(teamNumber).append("\n");
        out.append("   fieldPlayerColor: ").append(getTeamColorName(fieldPlayerColor)).append("\n");
        out.append("    goalkeeperColor: ").append(getTeamColorName(goalkeeperColor)).append("\n");
        out.append("         goalkeeper: ").append(goalkeeper).append("\n");
        out.append("              score: ").append(score).append("\n");
        out.append("        penaltyShot: ").append(penaltyShot).append("\n");
        out.append("        singleShots: ").append(Integer.toBinaryString(singleShots)).append("\n");
        out.append("      messageBudget: ").append(messageBudget).append("\n");

        for (int i = 0; i < player.length; i++) {
            out.append("Player #").append(i + 1).append("\n").append(player[i].toString());
        }
        return out.toString();
    }
}
