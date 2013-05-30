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
        long tmp = data.getTime();
        ActionBoard.clock.timeElapsed = tmp - lastTime;
        lastTime = tmp;
        
        if(data.gameState == GameControlData.STATE_READY
               && data.getSecondsSince(data.whenCurrentGameStateBegan) >= Rules.league.readyTime) {
            ActionBoard.set.perform(data);
        }
        
        if(!data.manPause) {
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
        boolean halfNotStarted = data.timeBeforeCurrentGameState == 0 && data.gameState != GameControlData.STATE_PLAYING;
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