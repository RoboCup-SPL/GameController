package controller.action.ui;

import common.Log;
import controller.action.ActionType;
import controller.action.GCAction;
import data.AdvancedData;
import data.GameControlData;
import data.Rules;


/**
 * @author: Michel Bartsch
 * 
 * This action means that the teams change colors, not automatically but because they want to.
 */
public class ManualColorChange extends GCAction
{
    /**
     * Creates a new ManualColorChange action.
     * Look at the ActionBoard before using this.
     */
    public ManualColorChange()
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
        byte tmp = data.team[0].teamColor;
        data.team[0].teamColor = data.team[1].teamColor;
        data.team[1].teamColor = tmp;
        Log.toFile("Manual Color Change");
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
        return ( (Rules.league.colorChangeManual)
              && (data.gameState == GameControlData.STATE_INITIAL) );
    }
}