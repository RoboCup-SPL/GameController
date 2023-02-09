package controller.action.ui;

import common.Log;
import controller.action.ActionBoard;
import controller.action.ActionType;
import controller.action.GCAction;
import data.AdvancedData;
import data.GameControlData;
import data.Rules;

/**
 * @author Michel Bartsch
 *
 * This action means that a timeOut is to be taken or ending.
 */
public class TimeOut extends GCAction
{
    /** On which side (0:left, 1:right) */
    private int side;

    /**
     * Creates a new TimeOut action.
     * Look at the ActionBoard before using this.
     *
     * @param side      On which side (0:left, 1:right)
     */
    public TimeOut(int side)
    {
        super(ActionType.UI);
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
        if (!data.timeOutActive[side]) {
            data.previousGamePhase = data.gamePhase;
            data.gamePhase = GameControlData.GAME_PHASE_TIMEOUT;
            data.timeBeforeCurrentGameState = data.timeBeforeStoppageOfPlay;
            data.timeOutActive[side] = true;
            data.timeOutTaken[side] = true;
            if (data.previousGamePhase != GameControlData.GAME_PHASE_PENALTYSHOOT) {
                data.kickingTeam = data.team[1 - side].teamNumber;
            } else if (data.gameState == GameControlData.STATE_SET) {
                data.team[data.kickingTeam == data.team[0].teamNumber ? 0 : 1].penaltyShot--;
            }
            Log.setNextMessage("Timeout "+Rules.league.teamColorName[data.team[side].fieldPlayerColor]);
            data.gameState = -1; // something impossible to force execution of next call
            data.setPlay = GameControlData.SET_PLAY_NONE;
            ActionBoard.initial.perform(data);
        } else {
            data.gamePhase = data.previousGamePhase;
            data.previousGamePhase = GameControlData.GAME_PHASE_TIMEOUT;
            data.timeOutActive[side] = false;
            data.kickOffReason = AdvancedData.KICKOFF_TIMEOUT;
            Log.setNextMessage("End of Timeout "+Rules.league.teamColorName[data.team[side].fieldPlayerColor]);
            if (data.gamePhase != GameControlData.GAME_PHASE_PENALTYSHOOT) {
                ActionBoard.ready.perform(data);
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
      return data.timeOutActive[side]
            || ((data.gameState == GameControlData.STATE_INITIAL ||
                    ((data.gameState == GameControlData.STATE_READY ||
                            data.gameState == GameControlData.STATE_SET)
                        && data.setPlay != GameControlData.SET_PLAY_PENALTY_KICK))
                && !data.timeOutTaken[side]
                && !data.timeOutActive[side == 0 ? 1 : 0]
                && !(data.gamePhase == GameControlData.GAME_PHASE_TIMEOUT)
                && (data.gamePhase != GameControlData.GAME_PHASE_PENALTYSHOOT
                    || data.gameState == GameControlData.STATE_INITIAL)
                && (data.competitionType != GameControlData.COMPETITION_TYPE_DYNAMIC_BALL_HANDLING))
            || data.testmode;
    }
}
