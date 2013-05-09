package controller.action.ui.state;

import controller.Log;
import controller.action.ActionBoard;
import controller.action.ActionType;
import controller.action.GCAction;
import controller.action.ui.half.FirstHalf;
import data.AdvancedData;
import data.GameControlData;
import data.Rules;


/**
 * @author: Michel Bartsch
 * 
 * This action means that the state is to be set to finish.
 */
public class Finish extends GCAction
{
    /**
     * Creates a new Finish action.
     * Look at the ActionBoard before using this.
     */
    public Finish()
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
        if(data.gameState == GameControlData.STATE_FINISHED) {
            return;
        }
        data.gameState = GameControlData.STATE_FINISHED;
        if(data.secGameState != GameControlData.STATE2_PENALTYSHOOT) {
            ActionBoard.clock.resetPlayerPenTime(data);
            if(data.firstHalf == GameControlData.C_TRUE) {
                data.remainingPaused = Rules.league.pauseTime*1000;
            } else if (data.fulltime && data.team[0].score == data.team[1].score){
                data.remainingPaused = Rules.league.pausePenaltyShootTime*1000;
            } else {
                data.remainingPaused = 0;
            }
        } else {
            byte tmp = data.team[0].teamColor;
            data.team[0].teamColor = data.team[1].teamColor;
            data.team[1].teamColor = tmp;
            data.kickOffTeam = data.kickOffTeam == GameControlData.TEAM_BLUE ? GameControlData.TEAM_RED : GameControlData.TEAM_BLUE;
            FirstHalf.changeSide(data);
        }
        Log.state(data, "State set to Finished");
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
        return (data.gameState == GameControlData.STATE_READY)
            || (data.gameState == GameControlData.STATE_SET)
            || (data.gameState == GameControlData.STATE_PLAYING)
            || (data.gameState == GameControlData.STATE_FINISHED)
            || data.testmode;
    }
}