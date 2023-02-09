package controller.action.ui.penalty;

import common.Log;
import data.AdvancedData;
import data.GameControlData;
import data.PlayerInfo;
import data.Rules;

/**
 * @author Arne Hasselbring
 *
 * This action means that the local game stuck penalty has been selected.
 */
public class LocalGameStuck extends Penalty
{
    /**
     * Performs this action`s penalty on a selected player.
     *
     * @param data      The current data to work on.
     * @param player    The player to penalise.
     * @param side      The side the player is playing on (0:left, 1:right).
     * @param number    The player`s number, beginning with 0!
     */
    @Override
    public void performOn(AdvancedData data, PlayerInfo player, int side, int number)
    {
        player.penalty = PlayerInfo.PENALTY_SPL_LOCAL_GAME_STUCK;
        data.robotPenaltyCount[side][number] = 0;
        data.whenPenalized[side][number] = data.getTime();
        Log.state(data, "Local Game Stuck "+
                Rules.league.teamColorName[data.team[side].fieldPlayerColor]
                + " " + (number+1));
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
        return (data.gameState == GameControlData.STATE_PLAYING)
                && (data.gamePhase != GameControlData.GAME_PHASE_PENALTYSHOOT)
                && (data.getRemainingSeconds(data.whenCurrentGameStateBegan, Rules.league.kickoffTime + Rules.league.minDurationBeforeStuck) <= 0)
                && (data.competitionType != GameControlData.COMPETITION_TYPE_DYNAMIC_BALL_HANDLING)
                || data.testmode;
    }
}
