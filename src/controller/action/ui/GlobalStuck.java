package controller.action.ui;

import controller.Log;
import controller.action.ActionBoard;
import controller.action.ActionType;
import controller.action.GCAction;
import data.AdvancedData;
import data.GameControlData;
import data.Rules;


/**
 * @author: Michel Bartsch
 * 
 * This action means that a global game stuck has occured.
 */
public class GlobalStuck extends GCAction
{
    /** On which side (0:left, 1:right) */
    private int side;
    
    
    /**
     * Creates a new GlobalStuck action.
     * Look at the ActionBoard before using this.
     * 
     * @param side      On which side (0:left, 1:right)
     */
    public GlobalStuck(int side)
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
        data.kickOffTeam = data.team[side == 0 ? 1 : 0].teamColor;
        data.gameState = GameControlData.STATE_READY;
        data.remainingReady = Rules.READY_TIME*1000;
        ActionBoard.clock.resetPlayerPenTime(data);
        if(-1*(data.remainingKickoffBlocked - Rules.KICKOFF_TIME*1000) < Rules.MIN_DURATION_BEFORE_GLOBAL_GAME_STUCK*1000)
        {
            Log.state(data, "Kickoff-Goal by "+Rules.TEAM_COLOR_NAME[data.team[side].teamColor]);
        } else {
            Log.state(data, "Global Game Stuck Kickoff "+Rules.TEAM_COLOR_NAME[data.team[side == 0 ? 1 : 0].teamColor]);
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
        return (data.gameState == GameControlData.STATE_PLAYING) || data.testmode;
    }
}