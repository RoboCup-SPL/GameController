package controller.action.ui.state;

import common.Log;
import controller.action.ActionType;
import controller.action.GCAction;
import data.AdvancedData;
import data.GameControlData;

/**
 * @author Michel Bartsch
 * 
 * This action means that the state is to be set to ready.
 */
public class Ready extends GCAction
{
    /**
     * Creates a new Ready action.
     * Look at the ActionBoard before using this.
     */
    public Ready()
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
        if (data.gameState == GameControlData.STATE_READY) {
            return;
        }
        if (data.gameState == GameControlData.STATE_INITIAL) {
            data.resetPenaltyTimes();
        } else if (data.gameState == GameControlData.STATE_PLAYING) {
            data.addTimeInCurrentState();
            data.timeBeforeStoppageOfPlay = data.timeBeforeCurrentGameState;
        } else if (data.gameState == GameControlData.STATE_SET) {
            data.addTimeInCurrentStateToPenalties();
        }
        data.whenCurrentGameStateBegan = data.getTime();
        data.gameState = GameControlData.STATE_READY;
        data.setPlay = GameControlData.SET_PLAY_NONE;
        Log.state(data, "Ready");
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
        return ((data.gameState == GameControlData.STATE_INITIAL)
              && !data.timeOutActive[0] 
              && !data.timeOutActive[1]
              && !data.refereeTimeout
              && (data.gamePhase != GameControlData.GAME_PHASE_PENALTYSHOOT))
            || (data.gameState == GameControlData.STATE_READY)
            || data.testmode;
    }
}
