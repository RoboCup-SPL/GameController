package data;


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
    /** Time in millis remaining to be penalized for each layer. */
    public long[][] playerPenTime = new long[2][Rules.league.teamSize];
    /** Pushing counters for each team, 0:left side, 1:right side. */
    public int[] pushes = {0, 0};
    /** Time in millis since last dropIn. */
    public long dropIn = -1*1000;
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
        super();
        if(Rules.league.startWithPenalty) {
            secGameState = GameControlData.STATE2_PENALTYSHOOT;
        }
    }
    
    /**
     * Copy constructure.
     * 
     * @param data    Object to copy.
     */
    public AdvancedData(AdvancedData data)
    {
        super(data);
        message = data.message;
        sumOfTime = data.sumOfTime;
        firstHalfTime = data.firstHalfTime;
        secondHalfTime = data.secondHalfTime;
        firstHalfOverTime = data.firstHalfOverTime;
        secondHalfOverTime = data.secondHalfOverTime;
        penaltyShootTime = data.penaltyShootTime;
        remainingReady = data.remainingReady;
        remainingPaused = data.remainingPaused;
        remainingKickoffBlocked = data.remainingKickoffBlocked;
        playerPenTime = new long[2][Rules.league.teamSize];
        for(int i=0; i<2; i++) {
            for(int j=0; j<playerPenTime[i].length; j++) {
                playerPenTime[i][j] = data.playerPenTime[i][j];
            }
        }
        pushes = new int[2];
        pushes[0] = data.pushes[0];
        pushes[1] = data.pushes[1];
        dropIn = data.dropIn;
        timeOut = new long[2];
        timeOut[0] = data.timeOut[0];
        timeOut[1] = data.timeOut[1];
        timeOutActive = new boolean[2];
        timeOutActive[0] = data.timeOutActive[0];
        timeOutActive[1] = data.timeOutActive[1];
        numberOfTimeOuts = new int[2];
        numberOfTimeOuts[0] = data.numberOfTimeOuts[0];
        numberOfTimeOuts[1] = data.numberOfTimeOuts[1];
        penaltyShoot = new int[2];
        penaltyShoot[0] = data.penaltyShoot[0];
        penaltyShoot[0] = data.penaltyShoot[0];
        playoff = data.playoff;
        manPause = data.manPause;
        manPlay = data.manPlay;
    }
    
    /**
     * Returns the side on which a team plays. The team should be playing
     * via this GameControler.
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
        for(int i=0; i<2; i++) {
            for(int j=0; j<playerPenTime[i].length; j++) {
                if(team[i].player[j].penalty != PlayerInfo.PENALTY_NONE) {
                    playerPenTime[i][j] = data.playerPenTime[i][j];
                    team[i].player[j].secsTillUnpenalised = data.team[i].player[j].secsTillUnpenalised;
                }
            }
        }
        if(dropInTeam == data.dropInTeam) {
            dropIn = data.dropIn;
            dropInTime = data.dropInTime;
        }
        secsRemaining = data.secsRemaining;
    }
}