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
 * This action means that a timeOut is to be taken or ending.
 */
public class TimeOut extends GCAction
{
    /** On which side (0:left, 1:right) */
    private int side;
    
    
    /**
     * Creates a new TimeOut action.
     * Look at the ActionBoard before using this.
     * 
     * @param side      On which side (0:left, 1:right)
     */
    public TimeOut(int side)
    {
        super(ActionType.UI);
        this.side = side;
    }

    /**
     * Performs this action to manipulate the data (model).
     * 
     * @param data      The current data to work on.
     */
    @Override
    public void perform(AdvancedData data)
    {
        if(!data.timeOutActive[side]) {
            if(Rules.league.timeOutTimeResette) {
                data.timeOut[side] = Rules.league.timeOutTime*1000;
            }
            data.timeOutActive[side] = true;
            data.numberOfTimeOuts[side]++;
            data.numberOfTimeOutsCurrentHalf[side]++;
            data.gameState = GameControlData.STATE_INITIAL;
            if(data.secGameState != GameControlData.STATE2_PENALTYSHOOT) {
                data.kickOffTeam = data.team[side].teamColor == GameControlData.TEAM_BLUE ? GameControlData.TEAM_RED : GameControlData.TEAM_BLUE;
            }
            Log.state(data, "TimeOut by "+Rules.league.teamColorName[data.team[side].teamColor]);
        } else {
            data.timeOutActive[side] = false;
            Log.state(data, "TimeOut by "+Rules.league.teamColorName[data.team[side].teamColor]+" ended");
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
      return data.timeOutActive[side]
            || ( (data.gameState == GameControlData.STATE_INITIAL ||
                  data.gameState == GameControlData.STATE_READY ||
                  data.gameState == GameControlData.STATE_SET)
                && data.numberOfTimeOuts[side] < Rules.league.timeOutMaxNumber
                && data.numberOfTimeOutsCurrentHalf[side] < Rules.league.timeOutMaxNumberHalf
                && !data.timeOutActive[side == 0 ? 1 : 0])
            || data.testmode;
    }
}