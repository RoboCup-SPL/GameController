package controller.action.ui.penalty;

import controller.EventHandler;
import controller.action.GCAction;
import controller.Log;
import controller.action.ActionBoard;
import controller.action.ActionType;
import data.AdvancedData;
import data.GameControlData;
import data.PlayerInfo;
import data.Rules;


/**
 * @author: Michel Bartsch
 * 
 * This action means that the ball holding penalty has been selected.
 */
public class Holding extends GCAction
{
    /**
     * Creates a new Holding action.
     * Look at the ActionBoard before using this.
     */
    public Holding()
    {
        super(ActionType.UI);
        penalty = PlayerInfo.PENALTY_SPL_BALL_HOLDING;
    }

    /**
     * Performs this action to manipulate the data (model).
     * 
     * @param data      The current data to work on.
     */
    @Override
    public void perform(AdvancedData data)
    {
        if(EventHandler.getInstance().lastUIEvent == this) {
            EventHandler.getInstance().noLastUIEvent = true;
        }
    }
    
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
        player.penalty = penalty;
        ActionBoard.clock.setPlayerPenTime(data, side, number, Rules.PENALTY_STANDARD_TIME);
        
        Log.state(data, "Ball Holding "+
                Rules.TEAM_COLOR_NAME[data.team[side].teamColor]
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
        return (data.gameState == GameControlData.STATE_PLAYING) || data.testmode;
    }
}