package controller.action.ui.penalty;

import controller.EventHandler;
import controller.action.ActionType;
import controller.action.GCAction;
import data.AdvancedData;
import data.PlayerInfo;

/**
 *
 * @author Michel-Zen
 * @author Dennis Schürholz (bhuman@dennisschuerholz.de)
 */
public abstract class Penalty extends GCAction
{
    /**
     * Creates a new Defender action.
     * Look at the ActionBoard before using this.
     */
    public Penalty()
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
        if (EventHandler.getInstance().lastUIEvent == this) {
            EventHandler.getInstance().noLastUIEvent = true;
        }
    }

    /**
     * Performs an ejection if the robot exceeds limit on repeated penalties
     *
     * @param data      The current data to work on.
     * @param player    The already penalised player.
     * @param side      The side the player is playing on (0:left, 1:right).
     * @param number    The player`s number, beginning with 0!
     */
    protected void handleRepeatedPenalty(final AdvancedData data, final PlayerInfo player,
            final int side, final int number, final int... states) {
        data.robotPenaltyCount[side][number] = 0;
        if (containsState(states, data.gameState)) {
            data.robotPenaltyCount[side][number] = data.penaltyCount[side];
            data.penaltyCount[side]++;
        }
    }

    private boolean containsState(final int[] states, final int state) {
        if (states == null || states.length == 0) {
            return false;
        }
        for (final int s : states) {
            if (s == state) {
                return true;
            }
        }
        return false;
    }
}