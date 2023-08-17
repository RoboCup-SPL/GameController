package data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

/**
 * This class extends the GameControlData that is send to the robots. It
 * contains all the additional information the GameController needs to represent
 * a state of the game, for example time in millis.
 *
 * There are no synchronized get and set methods because in this architecture
 * only actions in their perform method are allowed to write into this and they
 * are all in the same thread. Look in the EventHandler for more information.
 *
 * @author Michel Bartsch
 * @author Dennis Schürholz (bhuman@dennisschuerholz.de)
 */
public class AdvancedData extends GameControlData implements Cloneable {

    private static final long serialVersionUID = 2720243434306304319L;

    /**
     * This message is set when the data is put into the timeline
     */
    public String message = "";

    /**
     * How much time summed up before the current state? (ms)
     */
    public long timeBeforeCurrentGameState;

    /**
     * How much time summed up before the current stoppage of play? (ms)
     */
    public long timeBeforeStoppageOfPlay;

    /**
     * When was switched to the current state? (ms)
     */
    public long whenCurrentGameStateBegan;

    /**
     * When was the current set play activated? (ms)
     */
    public long whenCurrentSetPlayBegan;

    /**
     * How long ago started the current game state? (ms) Only set when written
     * to log!
     */
    public long timeSinceCurrentGameStateBegan;

    /**
     * How long ago started the current set play? (ms) Only set when written
     * to log and there is an active set play!
     */
    public long timeSinceCurrentSetPlayBegan;

    /**
     * When was each player penalized last (ms, 0 = never)?
     */
    public final long[][] whenPenalized = new long[2][Rules.league.teamSize];

    /**
     * How often was each team penalized?
     */
    public final int[] penaltyCount = new int[2];

    /**
     * How often was each team penalized at before the robot got penalized?
     */
    public final int[][] robotPenaltyCount = new int[2][Rules.league.teamSize];

    /**
     * How many hardware penalties can each robot get until it is ejected?
     */
    public final int[][] robotHardwarePenaltyBudget = new int[2][Rules.league.teamSize];

    /**
     * Which players are already ejected?
     */
    public final boolean[][] ejected = new boolean[2][Rules.league.teamSize];

    /**
     * Did the team send too many messages so that its score is set to 0 forever?
     */
    public final boolean[] sentIllegalMessages = {false, false};

    /**
     * If true, the referee set a timeout
     */
    public final boolean refereeTimeout = false;

    /**
     * If true, this team is currently taking a timeOut, 0:left side, 1:right
     * side.
     */
    public final boolean[] timeOutActive = {false, false};

    /**
     * TimeOut counters for each team, 0:left side, 1:right side.
     */
    public boolean[] timeOutTaken = {false, false};

    /**
     * If true, left side has the kickoff.
     */
    public boolean leftSideKickoff = true;

    /**
     * If true, the testmode has been activated.
     */
    public boolean testmode = false;

    /**
     * If true, the clock has manually been paused in the testmode.
     */
    public final boolean manPause = false;

    /**
     * If true, the clock has manually been started in the testmode.
     */
    public final boolean manPlay = false;

    /**
     * When was the last manual intervention to the clock?
     */
    public long manWhenClockChanged;

    /**
     * Time offset resulting from manually stopping the clock.
     */
    public long manTimeOffset;

    /**
     * Time offset resulting from starting the clock when it should be stopped.
     */
    public long manRemainingGameTimeOffset;

    /**
     * Used to back up the game phase during a timeout.
     */
    public final byte previousGamePhase = GAME_PHASE_NORMAL;

    /**
     * The kicking team before the last goal.
     */
    public final byte kickingTeamBeforeGoal = 0;

    public static final byte KICKOFF_HALF = 0;
    public static final byte KICKOFF_TIMEOUT = 1;
    public static final byte KICKOFF_GAMESTUCK = 2;
    public static final byte KICKOFF_PENALTYSHOOT = 3;
    public static final byte KICKOFF_GOAL = 4;
    public byte kickOffReason = KICKOFF_HALF;

    /**
     * Saves the selected penalty taker and keeper of both teams. First index is
     * team-number, second index is taker (0) or keeper (1)
     */
    public int[][] penaltyShootOutPlayers = new int[][]{{-1, -1}, {-1, -1}};

    /**
     * Creates a new AdvancedData.
     */
    public AdvancedData() {
        if (Rules.league.startWithPenalty) {
            gamePhase = GAME_PHASE_PENALTYSHOOT;
            kickOffReason = KICKOFF_PENALTYSHOOT;
        }
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < team[i].player.length; j++) {
                if (j >= Rules.league.robotsPlaying) {
                    team[i].player[j].penalty = PlayerInfo.PENALTY_SUBSTITUTE;
                }
                if (j < robotHardwarePenaltyBudget[i].length) {
                    robotHardwarePenaltyBudget[i][j] = Rules.league.allowedHardwarePenaltiesPerHalf;
                }
            }
        }
    }

    /**
     * Generically clone this object. Everything referenced must be
     * Serializable.
     *
     * @return A deep copy of this object.
     */
    public Object clone() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            new ObjectOutputStream(out).writeObject(this);
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            return new ObjectInputStream(in).readObject();
        } catch (ClassNotFoundException | IOException e) {
            System.out.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return null; // Should never be reached
    }

    /**
     * Returns the side on which a team plays. The team should be playing via
     * this GameController.
     *
     * @param teamNumber The unique teamNumber.
     *
     * @return The side of the team, 0:left side, 1:right side.
     */
    public int getSide(short teamNumber) {
        return teamNumber == team[0].teamNumber ? 0 : 1;
    }

    /**
     * Returns the current time. Can be stopped in test mode.
     *
     * @return The current time in ms. May become incompatible to the time
     * delivered by System.currentTimeMillis().
     */
    public long getTime() {
        return manPause ? manWhenClockChanged : System.currentTimeMillis() + manTimeOffset;
    }

    /**
     * Returns the number of seconds since a certain timestamp.
     *
     * @param millis The timestamp in ms.
     * @return The number of seconds since the timestamp.
     */
    public int getSecondsSince(long millis) {
        return millis == 0 ? 100000 : (int) (getTime() - millis) / 1000;
    }

    /**
     * The number of seconds until a certain duration is over. The time already
     * passed is specified as a timestamp when it began.
     *
     * @param millis The timestamp in ms.
     * @param durationInSeconds The full duration in s.
     * @return The number of seconds that still remain from the duration. Can be
     * negative.
     */
    public int getRemainingSeconds(long millis, int durationInSeconds) {
        return durationInSeconds - getSecondsSince(millis);
    }

    /**
     * Update all durations in the GameControlData packet.
     *
     * @param real If true, secsRemaining and secondaryTime will contain the real times
     * instead of the ones that are sent to the robots (to hide the fact that the playing
     * state might have started).
     */
    public void updateTimes(boolean real) {
        secsRemaining = (short) getRemainingGameTime(real);
        Integer subT = getSecondaryTime(real);

        if (subT == null) {
            secondaryTime = 0;
        } else {
            secondaryTime = (short) (int) subT;
        }
        for (int side = 0; side < team.length; ++side) {
            for (int number = 0; number < team[side].player.length; ++number) {
                PlayerInfo player = team[side].player[number];
                player.secsTillUnpenalised = player.penalty == PlayerInfo.PENALTY_NONE
                        ? 0 : (byte) (number < ejected[side].length && ejected[side][number] ? 255 : Math.min(255, getRemainingPenaltyTime(side, number, real)));
            }
        }
    }

    /**
     * Add the time passed in the current game state to the time that already
     * passed before. Is usually called during changes of the game state.
     */
    public void addTimeInCurrentState() {
        timeBeforeCurrentGameState += getTime() - whenCurrentGameStateBegan;
    }

    /**
     * Shifts the start times of penalties by the duration of the state
     * (or the already elapsed penalty time, whichever is shorter) into the future.
     * This is used to implement the rule that the penalty countdown pauses in Set.
     */
    public void addTimeInCurrentStateToPenalties() {
        for (int side = 0; side < team.length; side++) {
            for (int number = 0; number < whenPenalized[side].length; number++) {
                if (team[side].player[number].penalty != PlayerInfo.PENALTY_NONE && whenPenalized[side][number] != 0) {
                    whenPenalized[side][number] += getTime() - Math.max(whenCurrentGameStateBegan, whenPenalized[side][number]);
                }
            }
        }
    }

    /**
     * Calculates the remaining game time in the current phase of the game. This
     * is what the primary clock will show.
     *
     * @param real If true, the real time will be returned. If false, the first
     * number of seconds in the playing state or after a goal will not be updated.
     * @return The remaining number of seconds.
     */
    public int getRemainingGameTime(boolean real) {
        int duration = gamePhase == GAME_PHASE_TIMEOUT
                ? (previousGamePhase == GAME_PHASE_NORMAL ? Rules.league.halfTime
                        : previousGamePhase == GAME_PHASE_OVERTIME ? Rules.league.overtimeTime
                                : Rules.league.penaltyShotTime)
                : (gamePhase == GAME_PHASE_NORMAL) ? Rules.league.halfTime
                        : gamePhase == GAME_PHASE_OVERTIME ? Rules.league.overtimeTime
                                : Math.max(team[0].penaltyShot, team[1].penaltyShot) > Rules.league.numberOfPenaltyShots
                                ? Rules.league.penaltyShotTimeSuddenDeath
                                : Rules.league.penaltyShotTime;
        int timePlayed = gameState == STATE_INITIAL // during timeouts
                || ((gameState == STATE_READY || gameState == STATE_SET)
                    && (competitionPhase == COMPETITION_PHASE_PLAYOFF && Rules.league.playOffTimeStop
                        && (real || gamePhase != GAME_PHASE_NORMAL || gameState != STATE_READY || kickOffReason != KICKOFF_GOAL
                            || getSecondsSince(whenCurrentGameStateBegan) >= Rules.league.delayedSwitchAfterGoal)
                        || timeBeforeCurrentGameState == 0))
                || gameState == STATE_FINISHED
                        ? (int) ((timeBeforeCurrentGameState + manRemainingGameTimeOffset + (manPlay ? System.currentTimeMillis() - manWhenClockChanged : 0)) / 1000)
                        : real || (competitionPhase != COMPETITION_PHASE_PLAYOFF && timeBeforeCurrentGameState > 0) || gameState != STATE_PLAYING
                        || getSecondsSince(whenCurrentGameStateBegan) >= Rules.league.delayedSwitchToPlaying
                        ? getSecondsSince(whenCurrentGameStateBegan - timeBeforeCurrentGameState - manRemainingGameTimeOffset)
                        : (int) ((timeBeforeCurrentGameState - manRemainingGameTimeOffset) / 1000);
        return duration - timePlayed;
    }

    /**
     * The method returns the remaining pause time.
     *
     * @return The remaining number of seconds of the game pause or null if
     * there currently is no pause.
     */
    public Integer getRemainingPauseTime() {
        if (gamePhase == GAME_PHASE_NORMAL && competitionType != COMPETITION_TYPE_DYNAMIC_BALL_HANDLING
                && (gameState == STATE_INITIAL && firstHalf != C_TRUE && !timeOutActive[0] && !timeOutActive[1]
                || gameState == STATE_FINISHED && firstHalf == C_TRUE)) {
            return getRemainingSeconds(whenCurrentGameStateBegan, Rules.league.pauseTime);
        } else if (Rules.league.pausePenaltyShootOutTime != 0 && competitionPhase == COMPETITION_PHASE_PLAYOFF && team[0].score == team[1].score
                && (gameState == STATE_INITIAL && gamePhase == GAME_PHASE_PENALTYSHOOT && !timeOutActive[0] && !timeOutActive[1]
                || gameState == STATE_FINISHED && firstHalf != C_TRUE)) {
            return getRemainingSeconds(whenCurrentGameStateBegan, Rules.league.pausePenaltyShootOutTime);
        } else {
            return null;
        }
    }

    /**
     * Resets the penalize time of all players to 0. This does not unpenalize
     * them.
     */
    public void resetPenaltyTimes() {
        for (long[] players : whenPenalized) {
            Arrays.fill(players, 0);
        }
    }

    /**
     * Resets all penalties.
     */
    public void resetPenalties() {
        for (int i = 0; i < team.length; ++i) {
            for (int j = 0; j < Rules.league.teamSize; j++) {
                if (team[i].player[j].penalty != PlayerInfo.PENALTY_SUBSTITUTE && !ejected[i][j]) {
                    team[i].player[j].penalty = PlayerInfo.PENALTY_NONE;
                }
                if (Rules.league.resetPenaltyCountOnHalftime) {
                    robotPenaltyCount[i][j] = 0;
                }
                robotHardwarePenaltyBudget[i][j] = Math.min(Rules.league.allowedHardwarePenaltiesPerHalf,
                        Rules.league.allowedHardwarePenaltiesPerGame - (Rules.league.allowedHardwarePenaltiesPerHalf - robotHardwarePenaltyBudget[i][j]));
            }
            if (Rules.league.resetPenaltyCountOnHalftime) {
                penaltyCount[i] = 0;
            }
        }
        resetPenaltyTimes();
    }

    /**
     * Calculates the remaining time a certain robot has to stay penalized.
     *
     * @param side 0 or 1 depending on whether the robot's team is shown left or
     * right.
     * @param number The robot's number starting with 0.
     * @param real If true, the real time will be returned. If false, the first
     * number of seconds in the playing state the time that it was during set
     * will be returned.
     * @return The number of seconds the robot has to stay penalized.
     */
    public int getRemainingPenaltyTime(int side, int number, boolean real) {
        int penalty = team[side].player[number].penalty;
        int penaltyTime = getPenaltyDuration(side, number);
        if (penaltyTime == -1) {
            return 0;
        }
        assert penalty != PlayerInfo.PENALTY_MANUAL && penalty != PlayerInfo.PENALTY_SUBSTITUTE;
        long start = whenPenalized[side][number];
        if (start != 0 && (gameState == STATE_SET || (!real
                    && gameState == STATE_PLAYING
                    && getSecondsSince(whenCurrentGameStateBegan) < Rules.league.delayedSwitchToPlaying))) {
            start += getTime() - Math.max(whenCurrentGameStateBegan, whenPenalized[side][number]);
        }
        return Math.max(0, getRemainingSeconds(start, penaltyTime));
    }

    /**
     * Calculates the total duration of the current penalty of a robot.
     *
     * @param side 0 or 1 depending on whether the robot's team is shown left or
     * right.
     * @param number The robot's number starting with 0.
     * @return The total duration in seconds of the current penalty of a robot.
     */
    public int getPenaltyDuration(int side, int number) {
        int penalty = team[side].player[number].penalty;
        int penaltyTime = -1;
        if (penalty != PlayerInfo.PENALTY_MANUAL && penalty != PlayerInfo.PENALTY_SUBSTITUTE) {
            penaltyTime = Rules.league.penaltyTime[penalty] + Rules.league.penaltyIncreaseTime * robotPenaltyCount[side][number];
        }
        assert penalty == PlayerInfo.PENALTY_MANUAL || penalty == PlayerInfo.PENALTY_SUBSTITUTE || penaltyTime != -1;
        return penaltyTime;
    }

    /**
     * Calculates the Number of robots in play (not substitute) on one side
     *
     * @param side 0 or 1 depending on whether the team is shown left or right.
     * @return The number of robots without substitute penalty on the side
     */
    public int getNumberOfRobotsInPlay(int side) {
        int count = 0;
        for (int i = 0; i < team[side].player.length; i++) {
            if (team[side].player[i].penalty != PlayerInfo.PENALTY_SUBSTITUTE) {
                count++;
            }
        }
        return count;
    }

    /**
     * Determines the secondary time. Although this is a GUI feature, the
     * secondary time will also be encoded in the network packet.
     *
     * @param real If true, the real time will be returned. If false, the first
     * number of seconds in the playing state or after a goal there will be no
     * secondary time (otherwise the start of the game could be inferred by the
     * decreasing secondary time until the ball is free / ready ends).
     * @return The secondary time in seconds or null if there currently is none.
     */
    public Integer getSecondaryTime(boolean real) {
        if (!real && (gameState == STATE_PLAYING
                    && getSecondsSince(whenCurrentGameStateBegan) < Rules.league.delayedSwitchToPlaying
                    || gamePhase == GAME_PHASE_NORMAL && gameState == STATE_READY && kickOffReason == KICKOFF_GOAL
                    && getSecondsSince(whenCurrentGameStateBegan) < Rules.league.delayedSwitchAfterGoal)) {
            return null;
        }
        int timeKickOffBlocked = getRemainingSeconds(whenCurrentGameStateBegan, Rules.league.kickoffTime);
        if (gameState == STATE_INITIAL && (timeOutActive[0] || timeOutActive[1])) {
            return getRemainingSeconds(whenCurrentGameStateBegan, Rules.league.timeOutTime);
        } else if (gameState == STATE_INITIAL && (refereeTimeout)) {
            return getRemainingSeconds(whenCurrentGameStateBegan, Rules.league.refereeTimeout);
        } else if (gameState == STATE_READY) {
            return getRemainingSeconds(whenCurrentGameStateBegan, setPlay == SET_PLAY_PENALTY_KICK ? Rules.league.penaltyKickReadyTime : Rules.league.readyTime);
        } else if (gameState == STATE_PLAYING && gamePhase != GAME_PHASE_PENALTYSHOOT
                && (setPlay == SET_PLAY_GOAL_KICK || setPlay == SET_PLAY_PUSHING_FREE_KICK
                    || setPlay == SET_PLAY_CORNER_KICK || setPlay == SET_PLAY_KICK_IN)) {
            return getRemainingSeconds(whenCurrentSetPlayBegan, Rules.league.freeKickTime);
        } else if (gameState == STATE_PLAYING && gamePhase != GAME_PHASE_PENALTYSHOOT
                && setPlay == SET_PLAY_PENALTY_KICK) {
            return getRemainingSeconds(whenCurrentGameStateBegan, Rules.league.penaltyShotTime);
        } else if (gameState == STATE_PLAYING && kickOffReason != KICKOFF_PENALTYSHOOT
                && timeKickOffBlocked >= 0) {
            return timeKickOffBlocked;
        } else {
            return getRemainingPauseTime();
        }
    }

    public void updatePenalties() {
        if (gamePhase == GAME_PHASE_NORMAL && gameState == STATE_PLAYING
                && getSecondsSince(whenCurrentGameStateBegan) >= Rules.league.delayedSwitchToPlaying
                && Rules.league instanceof SPL) {
            for (TeamInfo t : team) {
                for (PlayerInfo p : t.player) {
                    if (p.penalty == PlayerInfo.PENALTY_SPL_ILLEGAL_MOTION_IN_SET) {
                        p.penalty = PlayerInfo.PENALTY_NONE;
                    }
                }
            }
        }
    }

    /**
     * Adjusts all timestamps in this class which depend on the system clock
     * for the current time.
     *
     * @param originalTime The time when the structure was saved.
     */
    public void adjustTimestamps(long originalTime) {
        final long timeUpdate = getTime() - originalTime;
        if (whenCurrentGameStateBegan != 0) {
            whenCurrentGameStateBegan += timeUpdate;
        }
        if (whenCurrentSetPlayBegan != 0) {
            whenCurrentSetPlayBegan += timeUpdate;
        }
        for (int i = 0; i < whenPenalized.length; ++i) {
            for (int j = 0; j < whenPenalized[i].length; ++j) {
                if (whenPenalized[i][j] != 0) {
                    whenPenalized[i][j] += timeUpdate;
                }
            }
        }
        if (manWhenClockChanged != 0) {
            manWhenClockChanged += timeUpdate;
        }
    }
}
