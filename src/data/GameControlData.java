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
public class GameControlData implements Serializable {

    private static final long serialVersionUID = 5061539348771652049L;

    /**
     * Some constants from the C-structure.
     */
    public static final int GAMECONTROLLER_RETURNDATA_PORT = 3939; // port to receive return-packets on
    public static final int GAMECONTROLLER_GAMEDATA_PORT = 3838; // port to send game state packets to

    public static final String GAMECONTROLLER_STRUCT_HEADER = "RGme";
    public static final String GAMECONTROLLER_TRUEGAMEDATA_STRUCT_HEADER = "RGTD";
    public static final byte GAMECONTROLLER_STRUCT_VERSION = 11;
    public static final byte TEAM_BLUE = 0;
    public static final byte TEAM_RED = 1;
    public static final byte TEAM_YELLOW = 2;
    public static final byte TEAM_BLACK = 3;
    public static final byte TEAM_WHITE = 4;
    public static final byte TEAM_GREEN = 5;
    public static final byte TEAM_ORANGE = 6;
    public static final byte TEAM_PURPLE = 7;
    public static final byte TEAM_BROWN = 8;
    public static final byte TEAM_GRAY = 9;
    public static final byte DROPBALL = -128;

    //max four bits!
    public static final byte GAMEPHASE_ROUNDROBIN = 0;
    public static final byte GAMEPHASE_PLAYOFF = 1;
    
    //max four bits!
    public static final byte GAMETYPE_NORMALTEAM = 0;
    public static final byte GAMETYPE_MIXEDTEAM = 1;

    public static final byte STATE_INITIAL = 0;
    public static final byte STATE_READY = 1;
    public static final byte STATE_SET = 2;
    public static final byte STATE_PLAYING = 3;
    public static final byte STATE_FINISHED = 4;

    public static final byte STATE2_NORMAL = 0;
    public static final byte STATE2_PENALTYSHOOT = 1;
    public static final byte STATE2_OVERTIME = 2;
    public static final byte STATE2_TIMEOUT = 3;
    public static final byte STATE2_GOAL_FREE_KICK = 4;
    public static final byte STATE2_PENALTY_FREE_KICK = 5;

    public static final byte C_FALSE = 0;
    public static final byte C_TRUE = 1;

    /**
     * The size in bytes this class has packed.
     */
    public static final int SIZE
            = 4
            + // header
            2
            + // version
            1
            + // packet number
            1
            + // numPlayers
            1
            + // four bit competitionPhase + four bit competitionType
            1
            + // gameState
            1
            + // firstHalf
            1
            + // kickingTeam
            1
            + // secGameState
            1
            + // dropInTeam
            2
            + // dropInTime
            2
            + // secsRemaining
            2
            + // secondaryTime
            2 * TeamInfo.SIZE;

    public boolean isTrueData;

    //this is streamed
    // GAMECONTROLLER_STRUCT_HEADER                             // header to identify the structure
    // GAMECONTROLLER_STRUCT_VERSION                            // version of the data structure
    public byte packetNumber = 0;
    public byte playersPerTeam = (byte) Rules.league.teamSize;  // The number of players on a team
    public byte competitionPhase = GAMEPHASE_ROUNDROBIN;        // phase of the game (GAMEPHASE_ROUNDROBIN, GAMEPHASE_PLAYOFF)
    public byte competitionType = GAMETYPE_NORMALTEAM;          // type of the game (GAMETYPE_NORMALTEAM, GAMETYPE_MIXEDTEAM)
    public byte gameState = STATE_INITIAL;                      // state of the game (STATE_READY, STATE_PLAYING, etc)
    public byte firstHalf = C_TRUE;                             // 1 = game in first half, 0 otherwise
    public byte kickingTeam;                                    // the next team to kick off
    public byte secGameState = STATE2_NORMAL;                   // Extra state information - (STATE2_NORMAL, STATE2_PENALTYSHOOT, etc)
    public byte dropInTeam;                                     // team that caused last drop in
    protected short dropInTime = -1;                            // number of seconds passed since the last drop in. -1 before first dropin
    public short secsRemaining = (short) Rules.league.halfTime; // estimate of number of seconds remaining in the half
    public short secondaryTime = 0;                             // sub-time (remaining in ready state etc.) in seconds
    public TeamInfo[] team = new TeamInfo[2];

    /**
     * Creates a new GameControlData.
     */
    public GameControlData() {
        for (int i = 0; i < team.length; i++) {
            team[i] = new TeamInfo();
        }
        team[0].teamColor = TEAM_BLUE;
        team[1].teamColor = TEAM_RED;
    }

    /**
     * Returns the corresponding byte-stream of the state of this object.
     *
     * @return the corresponding byte-stream of the state of this object
     */
    public ByteBuffer toByteArray() {
        AdvancedData data = (AdvancedData) this;
        ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(GAMECONTROLLER_STRUCT_HEADER.getBytes(), 0, 4);
        buffer.putShort(GAMECONTROLLER_STRUCT_VERSION);
        buffer.put(packetNumber);
        buffer.put(playersPerTeam);
        buffer.put((byte) ((competitionPhase << 4) | competitionType));
        if (secGameState == STATE2_NORMAL && gameState == STATE_PLAYING
                && data.getSecondsSince(data.whenCurrentGameStateBegan) < Rules.league.delayedSwitchToPlaying) {
            buffer.put(STATE_SET);
        } else {
            buffer.put(gameState);
        }
        buffer.put(firstHalf);
        buffer.put(kickingTeam);
        buffer.put(secGameState);
        buffer.put(dropInTeam);
        buffer.putShort(dropInTime);
        buffer.putShort(secsRemaining);
        buffer.putShort(secondaryTime);
        for (TeamInfo aTeam : team) {
            buffer.put(aTeam.toByteArray());
        }

        return buffer;
    }

    /**
     * Returns the corresponding byte-stream of the real state of this object.
     *
     * @return the corresponding byte-stream of the real state of this object
     */
    public ByteBuffer getTrueDataAsByteArray() {
        final ByteBuffer buffer = ByteBuffer.allocate(SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(GAMECONTROLLER_TRUEGAMEDATA_STRUCT_HEADER.getBytes(), 0, 4);
        buffer.putShort(GAMECONTROLLER_STRUCT_VERSION);
        buffer.put(packetNumber);
        buffer.put(playersPerTeam);
        buffer.put((byte) ((competitionPhase << 4) | competitionType));
        buffer.put(gameState);
        buffer.put(firstHalf);
        buffer.put(kickingTeam);
        buffer.put(secGameState);
        buffer.put(dropInTeam);
        buffer.putShort(dropInTime);
        buffer.putShort(secsRemaining);
        buffer.putShort(secondaryTime);
        for (TeamInfo aTeam : team) {
            buffer.put(aTeam.toByteArray());
        }

        return buffer;
    }

    /**
     * Unpacking the C-structure to the Java class.
     *
     * @param buffer The buffered C-structure.
     * @return Whether the structure was well formed. That is, it must have the
     * proper {@link #GAMECONTROLLER_STRUCT_VERSION} set.
     */
    public boolean fromByteArray(ByteBuffer buffer) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        byte[] header = new byte[4];
        buffer.get(header, 0, 4);
        isTrueData = new String(header).equals(GAMECONTROLLER_TRUEGAMEDATA_STRUCT_HEADER);
        if (buffer.getShort() != GAMECONTROLLER_STRUCT_VERSION) {
            return false;
        }
        packetNumber = buffer.get();
        playersPerTeam = buffer.get();
        byte gameType = buffer.get();
        competitionPhase = (byte) (gameType >> 4);
        competitionType = (byte) (gameType & 0b1111);
        gameState = buffer.get();
        firstHalf = buffer.get();
        kickingTeam = buffer.get();
        secGameState = buffer.get();
        dropInTeam = buffer.get();
        dropInTime = buffer.getShort();
        secsRemaining = buffer.getShort();
        secondaryTime = buffer.getShort();
        for (final TeamInfo t : team) {
            t.fromByteArray(buffer);
        }

        return true;
    }

    @Override
    public String toString() {
        String out = "";
        String temp;

        out += "             Header: " + GAMECONTROLLER_STRUCT_HEADER + "\n";
        out += "            Version: " + GAMECONTROLLER_STRUCT_VERSION + "\n";
        out += "      Packet Number: " + (packetNumber & 0xFF) + "\n";
        out += "   Players per Team: " + playersPerTeam + "\n";
        switch(competitionPhase) {
            case GAMEPHASE_ROUNDROBIN:
                temp = "round robin";
                break;
            case GAMEPHASE_PLAYOFF:
                temp = "playoff";
                break;
            default:
                temp = "undefined(" + competitionPhase + ")";
                break;            
        }
        out += "   competitionPhase: " + temp + "\n";
        switch(competitionType) {
            case GAMETYPE_NORMALTEAM:
                temp = "normal team";
                break;
            case GAMETYPE_MIXEDTEAM:
                temp = "mixed team";
                break;
            default:
                temp = "undefined(" + competitionType + ")";
                break;            
        }
        out += "    competitionType: " + temp + "\n";
        switch (gameState) {
            case STATE_INITIAL:
                temp = "initial";
                break;
            case STATE_READY:
                temp = "ready";
                break;
            case STATE_SET:
                temp = "set";
                break;
            case STATE_PLAYING:
                temp = "playing";
                break;
            case STATE_FINISHED:
                temp = "finish";
                break;
            default:
                temp = "undefinied(" + gameState + ")";
        }
        out += "          gameState: " + temp + "\n";
        switch (firstHalf) {
            case C_TRUE:
                temp = "true";
                break;
            case C_FALSE:
                temp = "false";
                break;
            default:
                temp = "undefinied(" + firstHalf + ")";
        }
        out += "          firstHalf: " + temp + "\n";
        out += "        kickingTeam: " + kickingTeam + "\n";
        switch (secGameState) {
            case STATE2_NORMAL:
                temp = "normal";
                break;
            case STATE2_PENALTYSHOOT:
                temp = "penaltyshoot";
                break;
            case STATE2_OVERTIME:
                temp = "overtime";
                break;
            case STATE2_TIMEOUT:
                temp = "timeout";
                break;
            case STATE2_GOAL_FREE_KICK:
            	temp = "goal free kick";
            	break;
            case STATE2_PENALTY_FREE_KICK:
            	temp = "penalty free kick";
            	break;
            default:
                temp = "undefinied(" + secGameState + ")";
        }
        out += "       secGameState: " + temp + "\n";
        out += "         dropInTeam: " + dropInTeam + "\n";
        out += "         dropInTime: " + dropInTime + "\n";
        out += "      secsRemaining: " + secsRemaining + "\n";
        out += "      secondaryTime: " + secondaryTime + "\n";
        return out;
    }
}
