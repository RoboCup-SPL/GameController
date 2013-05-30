package controller.action.ui.half;

import common.Log;
import controller.action.ActionType;
import controller.action.GCAction;
import data.AdvancedData;
import data.GameControlData;
import data.PlayerInfo;
import data.Rules;


/**
 * @author: Michel Bartsch
 * 
 * This action means that a penalty shoot is to be starting.
 */
public class PenaltyShoot extends GCAction
{
    /**
     * Creates a new PenaltyShoot action.
     * Look at the ActionBoard before using this.
     */
    public PenaltyShoot()
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
        if(data.secGameState != GameControlData.STATE2_PENALTYSHOOT) {
            data.secGameState = GameControlData.STATE2_PENALTYSHOOT;
            data.gameState = GameControlData.STATE_INITIAL;
            data.timeBeforeCurrentGameState = 0;
            data.resetPenalties();
            for(int i=0; i<2; i++) {
                data.numberOfTimeOutsCurrentHalf[i] = 0;
            }
            Log.state(data, "Half set to PenaltyShoot");
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
        return (data.secGameState == GameControlData.STATE2_PENALTYSHOOT)
          || ( (data.firstHalf != GameControlData.C_TRUE)
            && (data.gameState == GameControlData.STATE_FINISHED)
            && !(Rules.league.overtime
                && (data.playoff) 
                && (data.secGameState == GameControlData.STATE2_NORMAL)
                && (data.team[0].score == data.team[1].score) ) )
          || (data.testmode);
    }
}