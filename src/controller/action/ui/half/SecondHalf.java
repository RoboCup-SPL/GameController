package controller.action.ui.half;

import common.Log;
import controller.action.ActionType;
import controller.action.GCAction;
import data.AdvancedData;
import data.GameControlData;


/**
 * @author Michel Bartsch
 *
 * This action means that the half is to be set to the second half.
 */
public class SecondHalf extends GCAction
{
    /**
     * Creates a new SecondHalf action.
     * Look at the ActionBoard before using this.
     */
    public SecondHalf()
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
        if (data.firstHalf != GameControlData.C_FALSE || data.gamePhase == GameControlData.GAME_PHASE_PENALTYSHOOT) {
            data.firstHalf = GameControlData.C_FALSE;
            data.gamePhase = GameControlData.GAME_PHASE_NORMAL;
            FirstHalf.changeSide(data);
            data.kickingTeam = (data.leftSideKickoff ? data.team[0].teamNumber : data.team[1].teamNumber);
            data.kickOffReason = AdvancedData.KICKOFF_HALF;
            data.gameState = GameControlData.STATE_INITIAL;
            data.setPlay = GameControlData.SET_PLAY_NONE;
            // Don't set data.whenCurrentGameStateBegan, because it's used to count the pause
            Log.state(data, "2nd Half");
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
        return (((data.firstHalf != GameControlData.C_TRUE)
                        && (data.gamePhase == GameControlData.GAME_PHASE_NORMAL))
                   || ((data.gamePhase == GameControlData.GAME_PHASE_NORMAL)
                        && (data.gameState == GameControlData.STATE_FINISHED)))
                || data.testmode;
    }
}
