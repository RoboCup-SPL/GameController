package controller.action.ui.half;

import common.Log;
import controller.action.ActionType;
import controller.action.GCAction;
import data.AdvancedData;
import data.GameControlData;
import data.Rules;
import data.TeamInfo;

/**
 * @author Michel Bartsch
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
        if (data.firstHalf != GameControlData.C_TRUE || data.gamePhase == GameControlData.GAME_PHASE_PENALTYSHOOT) {
            data.firstHalf = GameControlData.C_TRUE;
            data.gamePhase = GameControlData.GAME_PHASE_NORMAL;
            changeSide(data);
            data.kickingTeam = (data.leftSideKickoff ? data.team[0].teamNumber : data.team[1].teamNumber);
            data.kickOffReason = AdvancedData.KICKOFF_HALF;
            data.gameState = GameControlData.STATE_INITIAL;
            data.setPlay = GameControlData.SET_PLAY_NONE;
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
        return ((data.firstHalf == GameControlData.C_TRUE)
                && (data.gamePhase == GameControlData.GAME_PHASE_NORMAL))
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

        // swap number of penalties per team
        if (!Rules.league.resetPenaltyCountOnHalftime)
        {
          int tmp = data.penaltyCount[0];
          data.penaltyCount[0] = data.penaltyCount[1];
          data.penaltyCount[1] = tmp;
        }

        // swap penalty shootout sides:
        int[] tmp = data.penaltyShootOutPlayers[0];
        data.penaltyShootOutPlayers[0] = data.penaltyShootOutPlayers[1];
        data.penaltyShootOutPlayers[1] = tmp;

        boolean[] ejected = data.ejected[0];
        data.ejected[0] = data.ejected[1];
        data.ejected[1] = ejected;

        tmp = data.robotHardwarePenaltyBudget[0];
        data.robotHardwarePenaltyBudget[0] = data.robotHardwarePenaltyBudget[1];
        data.robotHardwarePenaltyBudget[1] = tmp;

        // if necessary, swap back team colors
        if (data.gamePhase != GameControlData.GAME_PHASE_PENALTYSHOOT
                && data.colorChangeAuto) {
            byte color = data.team[0].teamColor;
            data.team[0].teamColor = data.team[1].teamColor;
            data.team[1].teamColor = color;
        }

        if (Rules.league.timeOutPerHalf && (data.gamePhase != GameControlData.GAME_PHASE_PENALTYSHOOT)) {
            data.timeOutTaken = new boolean[] {false, false};
        } else {
            boolean timeOutTaken = data.timeOutTaken[0];
            data.timeOutTaken[0] = data.timeOutTaken[1];
            data.timeOutTaken[1] = timeOutTaken;
        }
        
        data.timeBeforeCurrentGameState = 0;
        data.timeBeforeStoppageOfPlay = 0;
        if (data.gamePhase != GameControlData.GAME_PHASE_PENALTYSHOOT) {
            data.resetPenalties();
        }
    }
}
