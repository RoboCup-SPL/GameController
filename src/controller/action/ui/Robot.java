package controller.action.ui;

import common.Log;
import controller.EventHandler;
import controller.action.ActionType;
import controller.action.GCAction;
import controller.action.ui.penalty.CoachMotion;
import controller.action.ui.penalty.Penalty;
import controller.action.ui.penalty.PickUp;
import controller.action.ui.penalty.PickUpHL;
import controller.action.ui.penalty.Substitute;
import data.AdvancedData;
import data.HL;
import data.PlayerInfo;
import data.Rules;
import data.SPL;

/**
 * @author Michel Bartsch
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
        if (EventHandler.getInstance().lastUIEvent instanceof Penalty) {
            EventHandler.getInstance().lastUIEvent.performOn(data, player, side, number);
        }
        else if (EventHandler.getInstance().lastUIEvent instanceof TeammatePushing) {
            EventHandler.getInstance().lastUIEvent.performOn(data, player, side, number);
        } 
        else if (player.penalty != PlayerInfo.PENALTY_NONE) {
            Log.state(data, (player.penalty == PlayerInfo.PENALTY_SUBSTITUTE ? "Substituted by Player " : "Unpenalised ")+
                Rules.league.teamColorName[data.team[side].teamColor]
                + " " + (number+1));
            player.penalty = PlayerInfo.PENALTY_NONE;
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
        return !(EventHandler.getInstance().lastUIEvent instanceof Penalty)
                && data.team[side].player[number].penalty != PlayerInfo.PENALTY_NONE
                && (data.getRemainingPenaltyTime(side, number) == 0 || Rules.league instanceof HL)
                && (data.team[side].player[number].penalty != PlayerInfo.PENALTY_SUBSTITUTE || data.getNumberOfRobotsInPlay(side) < Rules.league.robotsPlaying)
                || EventHandler.getInstance().lastUIEvent instanceof PickUpHL
                && data.team[side].player[number].penalty != PlayerInfo.PENALTY_HL_REQUEST_FOR_SERVICE
                && data.team[side].player[number].penalty != PlayerInfo.PENALTY_HL_TEEN_REQUEST_FOR_PICKUP_2_SERVICE
                && data.team[side].player[number].penalty != PlayerInfo.PENALTY_SUBSTITUTE
                || (EventHandler.getInstance().lastUIEvent instanceof PickUp && Rules.league instanceof SPL)
                && data.team[side].player[number].penalty != PlayerInfo.PENALTY_SPL_REQUEST_FOR_PICKUP
                && data.team[side].player[number].penalty != PlayerInfo.PENALTY_SUBSTITUTE
                || EventHandler.getInstance().lastUIEvent instanceof Substitute
                && data.team[side].player[number].penalty != PlayerInfo.PENALTY_SUBSTITUTE
                && !isCoach(data)
                || (EventHandler.getInstance().lastUIEvent instanceof CoachMotion)
                    && (isCoach(data) && (data.team[side].coach.penalty != PlayerInfo.PENALTY_SPL_COACH_MOTION))
                || data.team[side].player[number].penalty == PlayerInfo.PENALTY_NONE
                    && (EventHandler.getInstance().lastUIEvent instanceof Penalty)
                    && !(EventHandler.getInstance().lastUIEvent instanceof CoachMotion)
                    && (!isCoach(data))
                || (data.team[side].player[number].penalty == PlayerInfo.PENALTY_NONE ) 
                    &&(EventHandler.getInstance().lastUIEvent instanceof TeammatePushing)
                || data.testmode;
    }
    
    public boolean isCoach(AdvancedData data){
        if((Rules.league.isCoachAvailable) && (number == Rules.league.teamSize)){
            return true;
        }
        return false;
    }
}