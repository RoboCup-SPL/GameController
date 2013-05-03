package controller.action.ui.half;

import controller.action.GCAction;
import controller.Log;
import controller.action.ActionType;
import data.AdvancedData;
import data.GameControlData;
import data.PlayerInfo;


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
            for(int i=0; i<2; i++) {
                data.pushes[i] = 0;
            }
            for(int i=0; i<2; i++) {
                for(int j=0; j<data.team[i].player.length; j++) {
                    data.team[i].player[j].penalty = PlayerInfo.PENALTY_NONE;
                    data.team[i].player[j].secsTillUnpenalised = 0;
                }
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
            && (data.gameState == GameControlData.STATE_FINISHED) )
          || (data.testmode);
    }
}