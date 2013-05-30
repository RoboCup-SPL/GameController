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
        
        if(isClockRunning(data)) {
            if(data.secGameState == GameControlData.STATE2_PENALTYSHOOT) {
            } else if(data.secGameState == GameControlData.STATE2_OVERTIME) {
                if(data.firstHalf == GameControlData.C_TRUE) {
                    data.firstHalfOverTime -= timeElapsed;
                } else {
                    data.secondHalfOverTime -= timeElapsed;
                }
            } else {
                if(data.firstHalf == GameControlData.C_TRUE) {
                    data.firstHalfTime -= timeElapsed;
                } else {
                    data.secondHalfTime -= timeElapsed;
                }
            }
        }
        long millisRemaining = 0;
        if(data.secGameState == GameControlData.STATE2_PENALTYSHOOT) {
        } else if(data.secGameState == GameControlData.STATE2_OVERTIME) {
            if(data.firstHalf == GameControlData.C_TRUE) {
                millisRemaining = data.firstHalfOverTime;
            } else {
                millisRemaining = data.secondHalfOverTime;
            }
        } else {
            if(data.firstHalf == GameControlData.C_TRUE) {
                millisRemaining = data.firstHalfTime;
            } else {
                millisRemaining = data.secondHalfTime;
            }
        }
        if(!data.manPause) {
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
                    if(data.remainingPaused <= Rules.league.pauseTime*1000/2) {
                        ActionBoard.secondHalf.perform(data);
                    }
                } else {
                    if(data.remainingPaused > 0
                     && data.remainingPaused <= Rules.league.pausePenaltyShootOutTime*1000/2) {
                        if( (Rules.league.overtime)
                                && (data.playoff) ) {
                            ActionBoard.firstHalf.perform(data);
                        } else {
                            ActionBoard.penaltyShoot.perform(data);
                        }
                    }
                }
            }
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
        boolean halfNotStarted = data.secGameState != GameControlData.STATE2_OVERTIME ?
                ((data.firstHalf == GameControlData.C_TRUE ?
                data.firstHalfTime : data.secondHalfTime) == Rules.league.halfTime*1000) :
                ((data.firstHalf == GameControlData.C_TRUE ?
                data.firstHalfOverTime : data.secondHalfOverTime) == Rules.league.overtimeTime*1000);
        return ( !( (data.gameState == GameControlData.STATE_INITIAL)
         || (data.gameState == GameControlData.STATE_FINISHED)
         || (
                ( (data.gameState == GameControlData.STATE_READY)
               || (data.gameState == GameControlData.STATE_SET) )
                && ((data.playoff && Rules.league.playOffTimeStop) || halfNotStarted)
                )
         || data.manPause )
         || data.manPlay       );
    }
}