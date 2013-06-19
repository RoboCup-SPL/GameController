package controller.action.ui.half;

import common.Log;
import controller.action.ActionBoard;
import controller.action.ActionType;
import controller.action.GCAction;
import data.AdvancedData;
import data.GameControlData;
import data.PlayerInfo;
import data.Rules;
import data.TeamInfo;

/**
 * @author: Michel Bartsch
 * 
 * This action means that the half is to be set to the first half.
 */
public class FirstHalf extends GCAction
{
    /**
     * Creates a new FirstHalf action.
     * Look at the ActionBoard before using this.
     */
    public FirstHalf()
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
        if(data.firstHalf != GameControlData.C_TRUE || data.secGameState == GameControlData.STATE2_PENALTYSHOOT) {
            data.firstHalf = GameControlData.C_TRUE;
            data.secGameState = GameControlData.STATE2_NORMAL;
            if(data.colorChangeAuto) {
                data.team[0].teamColor = GameControlData.TEAM_BLUE;
                data.team[1].teamColor = GameControlData.TEAM_RED;
            }
            changeSide(data);
            data.kickOffTeam = (data.leftSideKickoff ? data.team[0].teamColor : data.team[1].teamColor);
            data.gameState = GameControlData.STATE_INITIAL;
            // Don't set data.whenCurrentGameStateBegan, because it's used to count the pause
            Log.state(data, "1st Half");
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
        return ( (data.firstHalf == GameControlData.C_TRUE)
                && (data.secGameState == GameControlData.STATE2_NORMAL) )
                || (data.testmode);
    }
    
    /**
     * Switches sides for the teams, both for first to second and also
     * second to first half if needed.
     * 
     * @param data      The current data to work on.
     */
    public static void changeSide(AdvancedData data)
    {
        TeamInfo team = data.team[0];
        data.team[0] = data.team[1];
        data.team[1] = team;
        
        // swap back goal colors
        byte color = data.team[0].goalColor;
        data.team[0].goalColor = data.team[1].goalColor;
        data.team[1].goalColor = color;

        // if necessary, swap back team colors
        if(data.secGameState != GameControlData.STATE2_PENALTYSHOOT
                && data.colorChangeAuto) {
            color = data.team[0].teamColor;
            data.team[0].teamColor = data.team[1].teamColor;
            data.team[1].teamColor = color;
            if(Rules.league.timeOutPerHalf)
                data.timeOutTaken = new boolean[] {false, false};
        }
        
        boolean timeOutTaken = data.timeOutTaken[0];
        data.timeOutTaken[0] = data.timeOutTaken[1];
        data.timeOutTaken[1] = timeOutTaken;
        
        byte penaltyShot = data.penaltyShot[0];
        data.penaltyShot[0] = data.penaltyShot[1];
        data.penaltyShot[1] = penaltyShot;
        short penaltyTries = data.penaltyTries[0];
        data.penaltyTries[0] = data.penaltyTries[1];
        data.penaltyTries[1] = penaltyTries;
        
        data.timeBeforeCurrentGameState = 0;
        data.whenDropIn = 0;
        data.resetPenalties();
    }
}