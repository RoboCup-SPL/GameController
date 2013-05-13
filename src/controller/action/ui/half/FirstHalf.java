package controller.action.ui.half;

import common.Log;
import controller.action.ActionBoard;
import controller.action.ActionType;
import controller.action.GCAction;
import data.AdvancedData;
import data.GameControlData;
import data.PlayerInfo;


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
            data.team[0].teamColor = GameControlData.TEAM_BLUE;
            data.team[1].teamColor = GameControlData.TEAM_RED;
            changeSide(data);
            data.gameState = GameControlData.STATE_INITIAL;
            for(int i=0; i<2; i++) {
                data.numberOfTimeOutsCurrentHalf[i] = 0;
            }
            Log.state(data, "Half set to FirstHalf");
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
        return (data.firstHalf == GameControlData.C_TRUE) || (data.testmode);
    }
    
    /**
     * Switches sides for the teams, both for first to second and also
     * second to first half if needed.
     * 
     * @param data      The current data to work on.
     */
    public static void changeSide(AdvancedData data)
    {
        byte tmpTeamID = data.team[0].teamNumber;
        data.team[0].teamNumber = data.team[1].teamNumber;
        data.team[1].teamNumber = tmpTeamID;
        
        byte tmpScore = data.team[0].score;
        data.team[0].score = data.team[1].score;
        data.team[1].score = tmpScore;
        
        int tmpTimeOuts = data.numberOfTimeOuts[0];
        data.numberOfTimeOuts[0] = data.numberOfTimeOuts[1];
        data.numberOfTimeOuts[1] = tmpTimeOuts;
        boolean tmpTimeOut = data.timeOutActive[0];
        data.timeOutActive[0] = data.timeOutActive[1];
        data.timeOutActive[1] = tmpTimeOut;
        long tmpTimeOutTime = data.timeOut[0];
        data.timeOut[0] = data.timeOut[1];
        data.timeOut[1] = tmpTimeOutTime;
        
        int tmpPenaltyShoot = data.penaltyShoot[0];
        data.penaltyShoot[0] = data.penaltyShoot[1];
        data.penaltyShoot[1] = tmpPenaltyShoot;
        
        for(int i=0; i<2; i++) {
            data.pushes[i] = 0;
        }
        for(int i=0; i<2; i++) {
            for(int j=0; j<data.team[i].player.length; j++) {
                data.team[i].player[j].penalty = PlayerInfo.PENALTY_NONE;
                data.team[i].player[j].secsTillUnpenalised = 0;
            }
        }
        ActionBoard.clock.resetPlayerPenTime(data);
    }
}