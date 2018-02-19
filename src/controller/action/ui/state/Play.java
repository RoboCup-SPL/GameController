package controller.action.ui.state;

import common.Log;
import controller.action.ActionType;
import controller.action.GCAction;
import data.AdvancedData;
import data.GameControlData;
import data.PlayerInfo;
import data.TeamInfo;


/**
 * @author Michel Bartsch
 * 
 * This action means that the state is to be set to play.
 */
public class Play extends GCAction
{
    /**
     * Creates a new Play action.
     * Look at the ActionBoard before using this.
     */
    public Play()
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
        if (data.gameState == GameControlData.STATE_PLAYING) {
            return;
        }
        if (data.competitionPhase != GameControlData.GAMEPHASE_PLAYOFF && data.timeBeforeCurrentGameState != 0) {
            data.addTimeInCurrentState();
        }
        data.whenCurrentGameStateBegan = data.getTime();
        data.gameState = GameControlData.STATE_PLAYING;
        if(data.secGameState == GameControlData.STATE2_GOAL_FREE_KICK || data.secGameState == GameControlData.STATE2_PENALTY_FREE_KICK) {
        	data.previousSecGameState = data.secGameState;
        	data.secGameState = GameControlData.STATE2_NORMAL;        	
        }
        Log.state(data, "Playing");
    }
    
    /**
     * Checks if this action is legal with the given data (model).
     * Illegal actions are not performed by the EventHandler.
     * 
     * @param data      The current data to check with.
     */
    @Override
	public boolean isLegal(AdvancedData data) {
		return (data.gameState == GameControlData.STATE_READY
				&& (data.secGameState == GameControlData.STATE2_GOAL_FREE_KICK
						|| data.secGameState == GameControlData.STATE2_PENALTY_FREE_KICK))
				|| (data.gameState == GameControlData.STATE_SET
						&& (data.secGameState != GameControlData.STATE2_PENALTYSHOOT || bothTeamsHavePlayers(data)))
				|| (data.gameState == GameControlData.STATE_PLAYING) || data.testmode;
	}
    
    /**
     * Checks whether both teams have at least one player that is not penalized.
     * This is a precondition for a penalty shot.
     * 
     * @param data      The current data to check with.
     * @return At least one player not penalized in both teams?
     */
    private boolean bothTeamsHavePlayers(AdvancedData data)
    {
        boolean bothPlaying = true;
        for (TeamInfo teamInfo : data.team) {
            boolean playing = false;
            for (PlayerInfo playerInfo : teamInfo.player) {
                playing |= playerInfo != null && playerInfo.penalty == PlayerInfo.PENALTY_NONE;
            }
            bothPlaying &= playing;
        }
        return bothPlaying;
    }
}