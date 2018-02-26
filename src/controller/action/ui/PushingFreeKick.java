package controller.action.ui;

import common.Log;
import controller.action.ActionType;
import controller.action.GCAction;
import data.AdvancedData;
import data.GameControlData;
import data.Rules;


/**
 * @author Mario Grobler
 *
 * This action means that a team gets a pushing free kick
 */
public class PushingFreeKick extends GCAction
{
    /** On which side (0:left, 1:right) */
    private int side;

    /**
     * Creates a new PushingFreeKick action.
     * Look at the ActionBoard before using this.
     *
     * @param side      On which side (0:left, 1:right)
     */
    public PushingFreeKick(int side) {
        super(ActionType.UI);
        this.side = side;
    }

    /**
     * Performs this action to manipulate the data (model).
     *
     * @param data      The current data to work on.
     */
    @Override
    public void perform(AdvancedData data) {
        if (data.setPlay == GameControlData.SET_PLAY_PUSHING_FREE_KICK && data.kickingTeam == data.team[side].teamNumber) {
            return;
        }
        data.whenCurrentSetPlayBegan = data.getTime();
        data.setPlay = GameControlData.SET_PLAY_PUSHING_FREE_KICK;
        data.kickingTeam = data.team[side].teamNumber;
        Log.state(data, "Pushing Free Kick for "+Rules.league.teamColorName[data.team[side].teamColor]);
    }

    /**
     * Checks if this action is legal with the given data (model).
     * Illegal actions are not performed by the EventHandler.
     *
     * @param data      The current data to check with.
     */
    @Override
    public boolean isLegal(AdvancedData data) {
        return (data.gameState == GameControlData.STATE_PLAYING)
                && (data.gamePhase != GameControlData.GAME_PHASE_PENALTYSHOOT)
                && ((data.setPlay == GameControlData.SET_PLAY_NONE)
                       || (data.setPlay == GameControlData.SET_PLAY_PUSHING_FREE_KICK && data.kickingTeam == data.team[side].teamNumber))
                || data.testmode;
    }
}
