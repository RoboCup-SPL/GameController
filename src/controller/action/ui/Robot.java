package controller.action.ui;

import controller.EventHandler;
import common.Log;
import controller.action.ActionType;
import controller.action.GCAction;
import data.AdvancedData;
import data.Humanoid;
import data.PlayerInfo;
import data.Rules;


/**
 * @author: Michel Bartsch
 * 
 * This action means that a player has been selected.
 */
public class Robot extends GCAction
{
    /** On which side (0:left, 1:right) */
    private int side;
    /** The players`s number, beginning with 0! */
    private int number;
    
    
    /**
     * Creates a new Robot action.
     * Look at the ActionBoard before using this.
     * 
     * @param side      On which side (0:left, 1:right)
     * @param number    The players`s number, beginning with 0!
     */
    public Robot(int side, int number)
    {
        super(ActionType.UI);
        this.side = side;
        this.number = number;
    }

    /**
     * Performs this action to manipulate the data (model).
     * 
     * @param data      The current data to work on.
     */
    @Override
    public void perform(AdvancedData data)
    {
        PlayerInfo player = data.team[side].player[number];
        if(EventHandler.getInstance().lastUIEvent.penalty != PlayerInfo.PENALTY_NONE) {
            EventHandler.getInstance().lastUIEvent.performOn(data, player, side, number);
        } else if(player.penalty != PlayerInfo.PENALTY_NONE) {
            player.penalty = PlayerInfo.PENALTY_NONE;
            player.secsTillUnpenalised = 0;
            Log.state(data, "Unpenalised "+
                Rules.league.teamColorName[data.team[side].teamColor]
                + " " + (number+1));
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
        return ( (data.team[side].player[number].penalty != PlayerInfo.PENALTY_NONE)
              && ( (data.team[side].player[number].secsTillUnpenalised == 0)
                || ( (Rules.league instanceof Humanoid)
                    && (number == 0) ) ) )
            || ( (EventHandler.getInstance().lastUIEvent != null)
              && (EventHandler.getInstance().lastUIEvent.penalty != PlayerInfo.PENALTY_NONE)
              && (data.team[side].player[number].penalty == PlayerInfo.PENALTY_NONE) )
            || ( (EventHandler.getInstance().lastUIEvent != null)
              && ( (EventHandler.getInstance().lastUIEvent.penalty == PlayerInfo.PENALTY_SPL_REQUEST_FOR_PICKUP)
                    || (EventHandler.getInstance().lastUIEvent.penalty == PlayerInfo.PENALTY_HL_REQUEST_FOR_PICKUP) ) )
            || data.testmode;
    }
}