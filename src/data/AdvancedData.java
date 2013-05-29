package data;

import common.Tools;

/**
 * @author: Michel Bartsch
 *
 * This class extends the GameControlData that is send to the robots. It
 * contains all the additional informations the GameControler needs to
 * represent a state of the game, for example time in millis.
 * 
 * There are no synchronized get and set methods because in this architecture
 * only actions in their perform method are allowed to write into this and they
 * are all in the same thread. Look in the EventHandler for more information.
 */
public class AdvancedData extends GameControlData
{
    /** This message is set when the data is put into the timeline */
    public String message = "";
    /** Total time in millis since the Clock started */
    public long sumOfTime = 0;
    /** Time in millis remaining for the first half. */
    public long firstHalfTime = Rules.league.halfTime*1000;
    /** Time in millis remaining for the second half. */
    public long secondHalfTime = Rules.league.halfTime*1000;
    /** Time in millis remaining for the first half. */
    public long firstHalfOverTime = Rules.league.overtimeTime*1000;
    /** Time in millis remaining for the second half. */
    public long secondHalfOverTime = Rules.league.overtimeTime*1000;
    /** Time in millis remaining for the current penalty shoot. */
    public long penaltyShootTime = Rules.league.penaltyShootTime*1000;
    /** Contains the amount of extra time. */
    public int extraTime = 0;
    /** Time in millis remaining in the ready state. */
    public long remainingReady = Rules.league.readyTime*1000;
    /** Time in millis remaining between first and second half. */
    public long remainingPaused = 0;
    /** Time in millis remaining until ball is unblocked after kickoff. */
    public long remainingKickoffBlocked = Rules.league.kickoffTime*1000;

    /** When was the last drop-in? */
    public long whenDropIn;
    
    /** When was each player penalized last (0 = never)? */
    public long[][] whenPenalized = new long[2][Rules.league.teamSize];

    /** Which players were already ejected? */
    public boolean [][] ejected = new boolean[2][Rules.league.teamSize];
    
    /** Pushing counters for each team, 0:left side, 1:right side. */
    public int[] pushes = {0, 0};
    /** Time in millis remaining for each teams timeOut, 0:left side, 1:right side. */
    public long[] timeOut = {Rules.league.timeOutTime*1000, Rules.league.timeOutTime*1000};
    /** If true, this team is currently taking a timeOut, 0:left side, 1:right side. */
    public boolean[] timeOutActive = {false, false};
    /** TimeOut counters for each team, 0:left side, 1:right side. */
    public int[] numberOfTimeOuts = {0, 0};
    /** TimeOut counters within the current half for each team, 0:left side, 1:right side. */
    public int[] numberOfTimeOutsCurrentHalf = {0, 0};
    /** how many penalty-shoots have been made by each team, 0:left side, 1:right side. */
    public int[] penaltyShoot = {0, 0};
    /** If true, left side has the kickoff. */
    public boolean leftSideKickoff = true;
    /** If true, the clock has manually been paused in the testmode. */
    public boolean manPause = false;
    /** If true, the clock has manually been started in the testmode. */
    public boolean manPlay = false;
    /** If true, the game auto-pauses the game for full 10minutes playing. */
    public boolean playoff;
    /** If true, the testmode has been activated. */
    public boolean testmode = false;
    
    /**
     * Creates a new AdvancedData.
     */
    public AdvancedData()
    {
        if(Rules.league.startWithPenalty) {
            secGameState = GameControlData.STATE2_PENALTYSHOOT;
        }
    }
    
    /**
     * Returns the side on which a team plays. The team should be playing
     * via this GameController.
     * 
     * @param teamNumber    The unique teamNumber.
     * 
     * @return The side of the team, 0:left side, 1:right side.
     */
    public int getSide(short teamNumber)
    {
        return teamNumber == team[0].teamNumber ? 0 : 1;
    }
    
    /**
     * Copys all time based values from the given data into this data.
     * This is needed to go back in the timeline without setting back the
     * time.
     * 
     * @param data    The data to take the time from.
     */
    public void copyTime(AdvancedData data)
    {
        sumOfTime = data.sumOfTime;
        firstHalfTime = data.firstHalfTime;
        secondHalfTime = data.secondHalfTime;
        firstHalfOverTime = data.firstHalfOverTime;
        secondHalfOverTime = data.secondHalfOverTime;
        penaltyShootTime = data.penaltyShootTime;
        remainingReady = data.remainingReady;
        remainingPaused = data.remainingPaused;
        remainingKickoffBlocked = data.remainingKickoffBlocked;
        secsRemaining = data.secsRemaining;
    }
    
    public void updateTimes()
    {
        dropInTime = whenDropIn == 0 ? -1 : (short) Tools.getSecondsSince(whenDropIn);
        for (int side = 0; side < team.length; ++side) {
            for (int number = 0; number < team[side].player.length; ++number) {
                PlayerInfo player = team[side].player[number];
                player.secsTillUnpenalised = player.penalty == PlayerInfo.PENALTY_NONE
                        ? 0 : (short) getRemainingPenaltyTime(side, number);
            }
        }
    }
    
    /**
     * Resets the penalize time of all players to 0.
     * This does not unpenalize them!
     */
    public void resetPenaltyTimes()
    {
        for(long[] players : whenPenalized) {
            for(int i = 0; i < players.length; ++i) {
                players[i] = 0;
            }
        }
    }
    
    public int getRemainingPenaltyTime(int side, int number)
    {
        return gameState == STATE_READY && whenPenalized[side][number] != 0 ? (int) ((remainingReady + 999) / 1000)
                : Math.max(0, Tools.getRemainingSeconds(whenPenalized[side][number], Rules.league.penaltyStandardTime));
    }
}