package controller.action.clock;

import controller.action.ActionBoard;
import controller.action.ActionType;
import controller.action.GCAction;
import controller.action.ui.penalty.Pushing;
import data.AdvancedData;
import data.GameControlData;
import data.Rules;


/**
 * @author: Michel Bartsch
 * 
 * This action means that some time has been passed.
 */
public class ClockTick extends GCAction
{
    /** A timestamp when the this action has been performed last. */
    private long lastTime;
    /** The time in millis since this action has been performed last. */
    private long timeElapsed;
    
    
    /**
     * Creates a new ClockTick action.
     * Look at the ActionBoard before using this.
     */
    public ClockTick()
    {
        super(ActionType.CLOCK);
        lastTime = System.currentTimeMillis();
    }

    /**
     * Performs this action to manipulate the data (model).
     * 
     * @param data      The current data to work on.
     */
    @Override
    public void perform(AdvancedData data)
    {
        long tmp = System.currentTimeMillis();
        ActionBoard.clock.timeElapsed = tmp - lastTime;
        lastTime = tmp;
        data.sumOfTime += timeElapsed;
        
        if(isClockRunning(data)) {
            if(data.secGameState == GameControlData.STATE2_PENALTYSHOOT) {
                if(data.gameState == GameControlData.STATE_PLAYING) {
                    data.penaltyShootTime -= timeElapsed;
                }
            } else {
                if(data.firstHalf == GameControlData.C_TRUE) {
                    data.firstHalfTime -= timeElapsed;
                } else {
                    data.secondHalfTime -= timeElapsed;
                }
            }
        }
        long millisRemaining;
        if(data.secGameState == GameControlData.STATE2_PENALTYSHOOT) {
            millisRemaining = data.penaltyShootTime;
        } else {
            if(data.firstHalf == GameControlData.C_TRUE) {
                millisRemaining = data.firstHalfTime;
            } else {
                millisRemaining = data.secondHalfTime;
            }
        }
        if(millisRemaining > 0) {
            data.secsRemaining = (int)Math.ceil((double)millisRemaining/1000);
            data.extraTime = 0;
        } else {
            data.secsRemaining = 0;
            data.extraTime = -1* (int)Math.ceil((double)millisRemaining/1000);
        }
        if(!data.manPause) {
            for(int i=0; i<data.playerPenTime.length; i++) {
                for(int j=0; j<data.playerPenTime[i].length; j++) {
                    if(data.team[i].player[j].secsTillUnpenalised != Pushing.BANN_TIME) {
                        data.playerPenTime[i][j] = Math.max(0, data.playerPenTime[i][j] - timeElapsed);
                        data.team[i].player[j].secsTillUnpenalised = (short)Math.ceil((double)data.playerPenTime[i][j]/1000);
                    }
                }
            }
            if(data.dropInTime != -1) {
                data.dropIn += timeElapsed;
                data.dropInTime = (short)(data.dropIn/1000);
            }
            for(int i=0; i<2; i++) {
                if(data.timeOutActive[i]) {
                    data.timeOut[i] = Math.max(0, data.timeOut[i] - timeElapsed);
                }
            }
            if(data.gameState == GameControlData.STATE_READY) {
                data.remainingReady -= timeElapsed;
                if(data.remainingReady <= 0) {
                    ActionBoard.set.perform(data);
                }
            }

            if(data.remainingPaused > 0) {
                data.remainingPaused -= timeElapsed;
                if(data.remainingPaused <= 0) {
                    data.remainingPaused = 0;
                }
            }
            if( (data.gameState == GameControlData.STATE_FINISHED)
             && (data.secGameState == GameControlData.STATE2_NORMAL) ) {
                if(data.firstHalf == GameControlData.C_TRUE) {
                    if(data.remainingPaused <= Rules.PAUSE_TIME*1000/2) {
                        ActionBoard.secondHalf.perform(data);
                    }
                } else {
                    if(data.remainingPaused > 0
                     && data.remainingPaused <= Rules.PAUSE_PENALTY_SHOOT_TIME*1000/2) {
                        ActionBoard.penaltyShoot.perform(data);
                    }
                }
            }
            data.remainingKickoffBlocked -= timeElapsed;
        }
    }
    
    /**
     * Checks if this action is legal with the given data (model).
     * Illegal actions are not performed by the EventHandler.
     * 
     * @param data      The current data to check with.
     */
    @Override
    public boolean isLegal(AdvancedData data)
    {
        return true;
    }
    
    public boolean isClockRunning(AdvancedData data) {
        boolean halfNotStarted = (data.firstHalf == GameControlData.C_TRUE ?
                data.firstHalfTime : data.secondHalfTime) == Rules.HALF_TIME*1000;
        return ( !( (data.gameState == GameControlData.STATE_INITIAL)
         || (data.gameState == GameControlData.STATE_FINISHED)
         || (
                ( (data.gameState == GameControlData.STATE_READY)
               || (data.gameState == GameControlData.STATE_SET) )
                && (data.fulltime || halfNotStarted)
                )
         || data.manPause )
         || data.manPlay       );
    }
    
    /**
     * Sets the penalize time for the specific player. Use this instead of just
     * setting secsTillUnpenalised.
     * 
     * @param data      The current data to work on.
     * @param side      The player's side (0:left, 1:right).
     * @param number    The player's number
     * @param seconds   Penalize for that much seconds.
     */
    public void setPlayerPenTime(AdvancedData data, int side, int number, int seconds)
    {
        data.team[side].player[number].secsTillUnpenalised = (short)seconds;
        data.playerPenTime[side][number] = seconds*1000;
    }
    
    /**
     * Resets the penalize time of all players to 0.
     * This does not unpenalize them!
     * 
     * @param data      The current data to work on.
     */
    public void resetPlayerPenTime(AdvancedData data)
    {
        for(int i=0; i<data.playerPenTime.length; i++) {
            for(int j=0; j<data.playerPenTime[i].length; j++) {
                data.playerPenTime[i][j] = 0;
            }
        }
    }
    
    /**
     * Resets the time since last drop in.
     * Use this instead of just setting dropInTime.
     * 
     * @param data      The current data to work on.
     */
    public void newDropInTime(AdvancedData data)
    {
        data.dropInTime = 0;
        data.dropIn = 0;
    }
}