package controller.action.ui.penalty;

import common.Log;

import data.AdvancedData;
import data.PlayerInfo;
import data.Rules;

/**
 * @author Michel Bartsch
 *
 * This action means that the request for pickup penalty has been selected.
 */
public class PickUp extends Penalty
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
        if (data.competitionType == AdvancedData.COMPETITION_TYPE_DYNAMIC_BALL_HANDLING) {
            data.ejected[side][number] = true;
        } else if (player.penalty == PlayerInfo.PENALTY_NONE) {
            if (data.gameState == AdvancedData.STATE_INITIAL) {
                data.whenPenalized[side][number] = 0;
            } else {
                data.whenPenalized[side][number] = data.getTime();
            }
            data.robotPenaltyCount[side][number] = 0;
            if (data.gamePhase != AdvancedData.GAME_PHASE_PENALTYSHOOT
                    && (data.gameState == AdvancedData.STATE_READY
                        || data.gameState == AdvancedData.STATE_PLAYING)) {
                handleHardwarePenalty(data, side, number);
            }
        }

        player.penalty = PlayerInfo.PENALTY_SPL_REQUEST_FOR_PICKUP;
        Log.state(data, "Request for PickUp "+ Rules.league.teamColorName[data.team[side].fieldPlayerColor]+ " " + (number+1));
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
        return true;
    }
}
