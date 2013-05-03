package controller.action.ui;

import controller.action.GCAction;
import controller.Log;
import controller.action.ActionType;
import data.AdvancedData;
import data.GameControlData;
import data.Rules;


/**
 * @author: Michel Bartsch
 * 
 * This action means that the clock is to be resetted.
 */
public class ClockReset extends GCAction
{
    /**
     * Creates a new ClockReset action.
     * Look at the ActionBoard before using this.
     */
    public ClockReset()
    {
        super(ActionType.UI);
    }

    /**
     * Performs this action to manipulate the data (model).
     * 
     * @param data      The current data to work on.
     */
    @Override
    public void perform(AdvancedData data)
    {
        if(data.secGameState == GameControlData.STATE2_PENALTYSHOOT) {
            data.penaltyShootTime = Rules.PENALTY_SHOOT_TIME*1000;
        } else {
            if(data.firstHalf == GameControlData.C_TRUE) {
                data.firstHalfTime = Rules.HALF_TIME*1000;
            } else {
                data.secondHalfTime = Rules.HALF_TIME*1000;
            }
        }
        if(data.gameState == GameControlData.STATE_READY) {
            data.remainingReady = Rules.READY_TIME*1000;
        } else if( (data.gameState == GameControlData.STATE_INITIAL)
                && (data.firstHalf == GameControlData.C_TRUE) ) {
            data.remainingPaused = Rules.PAUSE_TIME*1000;
        }
        Log.state(data, "Time reset");
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
        return data.testmode;
    }
}