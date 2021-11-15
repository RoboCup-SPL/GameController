package controller.action.ui.penalty;

import controller.EventHandler;
import controller.action.ActionType;
import controller.action.GCAction;
import data.AdvancedData;

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
     * Increases the penalty counter of a robot and its team.
     *
     * @param data      The current data to work on.
     * @param side      The side the player is playing on (0:left, 1:right).
     * @param number    The player`s number, beginning with 0!
     */
    protected void handleRepeatedPenalty(final AdvancedData data, final int side, final int number) {
        data.robotPenaltyCount[side][number] = data.penaltyCount[side];
        data.penaltyCount[side]++;
    }

    /**
     * Decreases the hardware penalty budget of a robot and possibly ejects it.
     *
     * @param data      The current data to work on.
     * @param side      The side the player is playing on (0:left, 1:right).
     * @param number    The player`s number, beginning with 0!
     */
    protected void handleHardwarePenalty(final AdvancedData data, final int side, final int number) {
        if (data.robotHardwarePenaltyBudget[side][number] == 0) {
            data.ejected[side][number] = true;
        } else {
            data.robotHardwarePenaltyBudget[side][number]--;
        }
    }
}
