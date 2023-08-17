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
    public static final int GAMECONTROLLER_GAMEDATA_PORT = 3838; // port to send game state packets to

    public static final String GAMECONTROLLER_STRUCT_HEADER = "RGme";
    public static final String GAMECONTROLLER_TRUEGAMEDATA_STRUCT_HEADER = "RGTD";
    public static final byte GAMECONTROLLER_STRUCT_VERSION = 15;
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

    public static final byte COMPETITION_PHASE_ROUNDROBIN = 0;
    public static final byte COMPETITION_PHASE_PLAYOFF = 1;

    public static final byte COMPETITION_TYPE_NORMAL = 0;
    public static final byte COMPETITION_TYPE_DYNAMIC_BALL_HANDLING = 1;

    public static final byte GAME_PHASE_NORMAL = 0;
    public static final byte GAME_PHASE_PENALTYSHOOT = 1;
    public static final byte GAME_PHASE_OVERTIME = 2;
    public static final byte GAME_PHASE_TIMEOUT = 3;

    public static final byte STATE_INITIAL = 0;
    public static final byte STATE_READY = 1;
    public static final byte STATE_SET = 2;
    public static final byte STATE_PLAYING = 3;
    public static final byte STATE_FINISHED = 4;

    public static final byte SET_PLAY_NONE = 0;
    public static final byte SET_PLAY_GOAL_KICK = 1;
    public static final byte SET_PLAY_PUSHING_FREE_KICK = 2;
    public static final byte SET_PLAY_CORNER_KICK = 3;
    public static final byte SET_PLAY_KICK_IN = 4;
    public static final byte SET_PLAY_PENALTY_KICK = 5;

    public static final byte C_FALSE = 0;
    public static final byte C_TRUE = 1;

    /**
     * The size in bytes this class has packed.
     */
    public static final int SIZE
          = 4
            + // header
            1
            + // version
            1
            + // packet number
            1
            + // numPlayers
            1
            + // competitionPhase
            1
            + // competitionType
            1
            + // gamePhase
            1
            + // gameState
            1
            + // setPlay
            1
            + // firstHalf
            1
            + // kickingTeam
            2
            + // secsRemaining
            2
            + // secondaryTime
            2 * TeamInfo.SIZE;

    public boolean isTrueData;

    //this is streamed
    // GAMECONTROLLER_STRUCT_HEADER                              // header to identify the structure
    // GAMECONTROLLER_STRUCT_VERSION                             // version of the data structure
    public byte packetNumber = 0;
    public byte playersPerTeam = (byte) Rules.league.teamSize;   // The number of players on a team
    public byte competitionPhase = COMPETITION_PHASE_ROUNDROBIN; // phase of the game (COMPETITION_PHASE_ROUNDROBIN, COMPETITION_PHASE_PLAYOFF)
    public byte competitionType = COMPETITION_TYPE_NORMAL;       // type of the game (COMPETITION_TYPE_NORMAL, COMPETITION_TYPE_DYNAMIC_BALL_HANDLING)
    public byte gamePhase = GAME_PHASE_NORMAL;                   // Extra state information - (GAME_PHASE_NORMAL, GAME_PHASE_PENALTYSHOOT, etc)
    public byte gameState = STATE_INITIAL;                       // state of the game (STATE_READY, STATE_PLAYING, etc)
    public byte setPlay = SET_PLAY_NONE;                         // active set play (SET_PLAY_NONE, SET_PLAY_GOAL_KICK, etc)
    public byte firstHalf = C_TRUE;                              // 1 = game in first half, 0 otherwise
    public byte kickingTeam;                                     // the next team to kick off
    public short secsRemaining = (short) Rules.league.halfTime;  // estimate of number of seconds remaining in the half
    public short secondaryTime = 0;                              // sub-time (remaining in ready state etc.) in seconds
    public final TeamInfo[] team = new TeamInfo[2];

    /**
     * Creates a new GameControlData.
     */
    public GameControlData() {
        for (int i = 0; i < team.length; i++) {
            team[i] = new TeamInfo();
        }
        team[0].fieldPlayerColor = team[0].goalkeeperColor = TEAM_BLUE;
        team[1].fieldPlayerColor = team[1].goalkeeperColor = TEAM_RED;
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
        buffer.put(GAMECONTROLLER_STRUCT_VERSION);
        buffer.put(packetNumber);
        buffer.put(playersPerTeam);
        buffer.put(competitionPhase);
        buffer.put(competitionType);
        buffer.put(gamePhase);
        if (gameState == STATE_PLAYING
                && data.getSecondsSince(data.whenCurrentGameStateBegan) < Rules.league.delayedSwitchToPlaying) {
            buffer.put(STATE_SET);
        } else if (gamePhase == GAME_PHASE_NORMAL && gameState == STATE_READY
                && data.kickOffReason == AdvancedData.KICKOFF_GOAL
                && data.getSecondsSince(data.whenCurrentGameStateBegan) < Rules.league.delayedSwitchAfterGoal) {
            buffer.put(STATE_PLAYING);
        } else {
            buffer.put(gameState);
        }
        buffer.put(setPlay);
        buffer.put(firstHalf);
        if (gamePhase == GAME_PHASE_NORMAL && gameState == STATE_READY && data.kickOffReason == AdvancedData.KICKOFF_GOAL
                && data.getSecondsSince(data.whenCurrentGameStateBegan) < Rules.league.delayedSwitchAfterGoal) {
            buffer.put(data.kickingTeamBeforeGoal);
        } else {
            buffer.put(kickingTeam);
        }
        buffer.putShort(secsRemaining);
        buffer.putShort(secondaryTime);
        for (TeamInfo aTeam : team) {
            buffer.put(aTeam.toByteArray(gamePhase == GAME_PHASE_NORMAL && gameState == STATE_READY
                        && data.kickOffReason == AdvancedData.KICKOFF_GOAL
                        && data.getSecondsSince(data.whenCurrentGameStateBegan) < Rules.league.delayedSwitchAfterGoal
                        && data.kickingTeam != aTeam.teamNumber));
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
        buffer.put(GAMECONTROLLER_STRUCT_VERSION);
        buffer.put(packetNumber);
        buffer.put(playersPerTeam);
        buffer.put(competitionPhase);
        buffer.put(competitionType);
        buffer.put(gamePhase);
        buffer.put(gameState);
        buffer.put(setPlay);
        buffer.put(firstHalf);
        buffer.put(kickingTeam);
        buffer.putShort(secsRemaining);
        buffer.putShort(secondaryTime);
        for (TeamInfo aTeam : team) {
            buffer.put(aTeam.toByteArray(false));
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
        if (buffer.get() != GAMECONTROLLER_STRUCT_VERSION) {
            return false;
        }
        packetNumber = buffer.get();
        playersPerTeam = buffer.get();
        competitionPhase = buffer.get();
        competitionType = buffer.get();
        gamePhase = buffer.get();
        gameState = buffer.get();
        setPlay = buffer.get();
        firstHalf = buffer.get();
        kickingTeam = buffer.get();
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
            case COMPETITION_PHASE_ROUNDROBIN:
                temp = "round robin";
                break;
            case COMPETITION_PHASE_PLAYOFF:
                temp = "playoff";
                break;
            default:
                temp = "undefined(" + competitionPhase + ")";
                break;
        }
        out += "   competitionPhase: " + temp + "\n";
        switch(competitionType) {
            case COMPETITION_TYPE_NORMAL:
                temp = "normal";
                break;
            case COMPETITION_TYPE_DYNAMIC_BALL_HANDLING:
                temp = "dynamic ball handling";
                break;
            default:
                temp = "undefined(" + competitionType + ")";
                break;
        }
        out += "    competitionType: " + temp + "\n";
        switch (gamePhase) {
            case GAME_PHASE_NORMAL:
                temp = "normal";
                break;
            case GAME_PHASE_PENALTYSHOOT:
                temp = "penaltyshoot";
                break;
            case GAME_PHASE_OVERTIME:
                temp = "overtime";
                break;
            case GAME_PHASE_TIMEOUT:
                temp = "timeout";
                break;
            default:
                temp = "undefined(" + gamePhase + ")";
        }
        out += "          gamePhase: " + temp + "\n";
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
                temp = "undefined(" + gameState + ")";
        }
        out += "          gameState: " + temp + "\n";
        switch (setPlay) {
            case SET_PLAY_NONE:
                temp = "none";
                break;
            case SET_PLAY_GOAL_KICK:
                temp = "goal kick";
                break;
            case SET_PLAY_PUSHING_FREE_KICK:
                temp = "pushing free kick";
                break;
            case SET_PLAY_CORNER_KICK:
                temp = "corner kick";
                break;
            case SET_PLAY_KICK_IN:
                temp = "kick in";
                break;
            case SET_PLAY_PENALTY_KICK:
                temp = "penalty kick";
                break;
            default:
                temp = "undefined(" + setPlay + ")";
        }
        out += "            setPlay: " + temp + "\n";
        switch (firstHalf) {
            case C_TRUE:
                temp = "true";
                break;
            case C_FALSE:
                temp = "false";
                break;
            default:
                temp = "undefined(" + firstHalf + ")";
        }
        out += "          firstHalf: " + temp + "\n";
        out += "        kickingTeam: " + kickingTeam + "\n";
        out += "      secsRemaining: " + secsRemaining + "\n";
        out += "      secondaryTime: " + secondaryTime + "\n";
        return out;
    }
}
