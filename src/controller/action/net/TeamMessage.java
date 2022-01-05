package controller.action.net;

import controller.action.ActionType;
import controller.action.GCAction;
import data.AdvancedData;
import data.GameControlData;


/**
 * @author Arne Hasselbring
 *
 * This action is performed when a team message is received.
 */
public class TeamMessage extends GCAction
{
    /** On which side (0:left, 1:right) */
    private int side;

    /**
     * Creates a new TeamMessage action.
     * Look at the ActionBoard before using this.
     *
     * @param side      On which side (0:left, 1:right)
     */
    public TeamMessage(int side)
    {
        super(ActionType.NET);
        this.side = side;
    }

    /**
     * Performs this action to manipulate the data (model).
     *
     * @param data      The current data to work on.
     */
    @Override
    public void perform(AdvancedData data)
    {
        if (data.gamePhase != GameControlData.GAME_PHASE_PENALTYSHOOT
                && data.gameState != GameControlData.STATE_INITIAL
                && data.gameState != GameControlData.STATE_FINISHED) {
            if (data.team[side].messageBudget > 0) {
                --data.team[side].messageBudget;
            } else {
                // TODO: set score fixed to 0
            }
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
        return true;
    }
}
