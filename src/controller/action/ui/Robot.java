package controller.action.ui;

import common.Log;
import controller.EventHandler;
import controller.action.ActionType;
import controller.action.GCAction;
import controller.action.ui.penalty.MotionInSet;
import controller.action.ui.penalty.Penalty;
import controller.action.ui.penalty.PickUp;
import data.AdvancedData;
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
        final boolean lastUIEventWasPenalty = EventHandler.getInstance().lastUIEvent instanceof Penalty;
        final boolean lastUIEventWasMotionInSet = EventHandler.getInstance().lastUIEvent instanceof MotionInSet;
        final boolean lastUIEventWasRobot = EventHandler.getInstance().lastUIEvent instanceof Robot;
        final Robot lastUIEventAsRobot = lastUIEventWasRobot ? (Robot)EventHandler.getInstance().lastUIEvent : null;
        final PlayerInfo player = data.team[side].player[number];
        boolean wantLastUIEvent = false;
        if (lastUIEventWasPenalty && (!lastUIEventWasMotionInSet || player.penalty == PlayerInfo.PENALTY_NONE)) {
            EventHandler.getInstance().lastUIEvent.performOn(data, player, side, number);
        } else if (data.gamePhase == AdvancedData.GAME_PHASE_PENALTYSHOOT
                && (data.team[side].player[number].penalty == PlayerInfo.PENALTY_NONE
                    || data.team[side].player[number].penalty == PlayerInfo.PENALTY_SUBSTITUTE)
                && (data.gameState == AdvancedData.STATE_SET
                    || data.gameState == AdvancedData.STATE_INITIAL)) {

            // make all other players to substitute:
            for (int playerID = 0; playerID < Rules.league.teamSize; playerID++) {
                if (playerID != number) {
                    data.team[side].player[playerID].penalty = PlayerInfo.PENALTY_SUBSTITUTE;
                    data.robotPenaltyCount[side][playerID] = 0;
                    data.whenPenalized[side][playerID] = data.getTime();
                }
            }

            // unpenalise selected player:
            player.penalty = PlayerInfo.PENALTY_NONE;
            data.penaltyShootOutPlayers[side][data.team[side].teamNumber == data.kickingTeam ? 0 : 1] = number;

            Log.state(data, "Selected Player " + Rules.league.teamColorName[data.team[side].teamColor] + " "
                    + (number + 1) + " as " + (data.team[side].teamNumber == data.kickingTeam ? "taker" : "keeper"));
        } else if (player.penalty == PlayerInfo.PENALTY_SUBSTITUTE && data.gamePhase != AdvancedData.GAME_PHASE_PENALTYSHOOT) {
            wantLastUIEvent = EventHandler.getInstance().lastUIEvent != this;
        } else if (lastUIEventWasRobot
                && lastUIEventAsRobot.side == side
                && data.team[side].player[number].penalty != PlayerInfo.PENALTY_SUBSTITUTE
                && data.gamePhase != AdvancedData.GAME_PHASE_PENALTYSHOOT) {
            final int substituteNumber = lastUIEventAsRobot.number;
            if (player.penalty == PlayerInfo.PENALTY_NONE && data.gameState != AdvancedData.STATE_INITIAL) {
                data.team[side].player[substituteNumber].penalty = Rules.league.substitutePenalty;
                data.robotPenaltyCount[side][substituteNumber] = 0;
                data.whenPenalized[side][substituteNumber] = data.getTime();
            } else {
                data.team[side].player[substituteNumber].penalty = player.penalty;
                data.robotPenaltyCount[side][substituteNumber] = data.robotPenaltyCount[side][number];
                data.whenPenalized[side][substituteNumber] = data.whenPenalized[side][number];
            }
            player.penalty = PlayerInfo.PENALTY_SUBSTITUTE;
            data.robotPenaltyCount[side][number] = 0;
            data.whenPenalized[side][number] = data.getTime();
            Log.state(data, "Substituted " + Rules.league.teamColorName[data.team[side].teamColor] + " " + (number + 1) + " by " + (substituteNumber + 1));
        } else if (player.penalty != PlayerInfo.PENALTY_NONE) {
            player.penalty = PlayerInfo.PENALTY_NONE;
            Log.state(data, ("Unpenalised ")+
                    Rules.league.teamColorName[data.team[side].teamColor]
                    + " " + (number+1));
        }
        EventHandler.getInstance().noLastUIEvent = !wantLastUIEvent;
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
        final boolean lastUIEventWasPenalty = EventHandler.getInstance().lastUIEvent instanceof Penalty;
        final boolean lastUIEventWasMotionInSet = EventHandler.getInstance().lastUIEvent instanceof MotionInSet;
        // This can only be true if the robot currently has the substitute penalty because otherwise lastUIEvent is not set, see above.
        final boolean lastUIEventWasRobot = EventHandler.getInstance().lastUIEvent instanceof Robot;
        final Robot lastUIEventAsRobot = lastUIEventWasRobot ? (Robot)EventHandler.getInstance().lastUIEvent : null;
        final boolean isRobotSubstitute = data.team[side].player[number].penalty == PlayerInfo.PENALTY_SUBSTITUTE;
        return
                // penalize a robot
                data.team[side].player[number].penalty == PlayerInfo.PENALTY_NONE
                && lastUIEventWasPenalty
                // change penalty to pickup in SPL
                || (EventHandler.getInstance().lastUIEvent instanceof PickUp && Rules.league instanceof SPL)
                && data.team[side].player[number].penalty != PlayerInfo.PENALTY_SPL_REQUEST_FOR_PICKUP
                && !isRobotSubstitute
                // new player selection in penalty shootout:
                || (data.gamePhase == AdvancedData.GAME_PHASE_PENALTYSHOOT
                    && (data.team[side].player[number].penalty == PlayerInfo.PENALTY_NONE
                        || data.team[side].player[number].penalty == PlayerInfo.PENALTY_SUBSTITUTE)
                    && (data.gameState == AdvancedData.STATE_SET
                        || data.gameState == AdvancedData.STATE_INITIAL))
                // unpenalize a robot / select a robot that should enter (i.e. that is currently substituted)
                || !data.ejected[side][number]
                && (!lastUIEventWasPenalty || lastUIEventWasMotionInSet)
                && data.team[side].player[number].penalty != PlayerInfo.PENALTY_NONE
                && (Rules.league.allowEarlyPenaltyRemoval
                    // special case: Illegal motion in Set can be removed manually because it happens that accidentally
                    // too many robots are penalized and the buttons were pressed in an order that makes "Undo" unusable.
                    || (Rules.league instanceof SPL && data.gameState == AdvancedData.STATE_SET
                        && data.team[side].player[number].penalty == PlayerInfo.PENALTY_SPL_ILLEGAL_MOTION_IN_SET)
                    // penalty time is over
                    || data.getRemainingPenaltyTime(side, number, true) == 0)
                && (!isRobotSubstitute
                    || data.gamePhase != AdvancedData.GAME_PHASE_PENALTYSHOOT)
                // click on the robot that should be substituted (after having clicked on the robot that should enter)
                || lastUIEventWasRobot
                && lastUIEventAsRobot.side == side
                && !isRobotSubstitute
                && data.gamePhase != AdvancedData.GAME_PHASE_PENALTYSHOOT
                && (!(Rules.league instanceof SPL) || number != 0)

                || data.testmode;
    }
 }
