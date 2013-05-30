package controller.action.ui;

import common.Log;
import controller.action.ActionType;
import controller.action.GCAction;
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
        data.timeBeforeCurrentGameState = 0;
        data.whenCurrentGameStateBegan = data.getTime();
        data.manWhenClockChanged = data.whenCurrentGameStateBegan;
        data.manRemainingGameTimeOffset = 0;
        if(data.gameState == GameControlData.STATE_READY) {
            data.remainingReady = Rules.league.readyTime*1000;
        } else if( (data.gameState == GameControlData.STATE_INITIAL)
                && (data.firstHalf == GameControlData.C_TRUE) ) {
            data.remainingPaused = Rules.league.pauseTime*1000;
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