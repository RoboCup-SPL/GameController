package controller.action.ui.state;

import common.Log;
import controller.action.ActionType;
import controller.action.GCAction;
import controller.action.ui.half.FirstHalf;
import data.AdvancedData;
import data.GameControlData;
import data.PlayerInfo;
import data.Rules;

/**
 * @author Michel Bartsch
 *
 * This action means that the state is to be set to set.
 */
public class Set extends GCAction {

    /**
     * Creates a new Set action. Look at the ActionBoard before using this.
     */
    public Set() {
        super(ActionType.UI);
    }

    /**
     * Performs this action to manipulate the data (model).
     *
     * @param data The current data to work on.
     */
    @Override
    public void perform(AdvancedData data) {
        if (data.gameState == GameControlData.STATE_SET) {
            return;
        }
        if ((data.competitionPhase != GameControlData.COMPETITION_PHASE_PLAYOFF) && data.timeBeforeCurrentGameState != 0) {
            data.addTimeInCurrentState();
        }
        data.whenCurrentGameStateBegan = data.getTime();

        if (data.gamePhase == GameControlData.GAME_PHASE_PENALTYSHOOT) {
            data.timeBeforeCurrentGameState = 0;
            if (data.gameState != GameControlData.STATE_INITIAL) {
                data.kickingTeam = data.team[data.kickingTeam == data.team[0].teamNumber ? 1 : 0].teamNumber;
                FirstHalf.changeSide(data);
            }
            if (data.gameState != GameControlData.STATE_PLAYING) {
                data.team[data.team[0].teamNumber == data.kickingTeam ? 0 : 1].penaltyShot++;
            }

            // restore selected player:
            for (int side = 0; side < 2; side++) {
                int number = data.penaltyShootOutPlayers[side][data.team[side].teamNumber == data.kickingTeam ? 0 : 1];

                for (int playerID = 0; playerID < Rules.league.teamSize; playerID++) {
                    if (playerID != number) {
                        PlayerInfo playerToSub = data.team[side].player[playerID];

                        if (playerToSub.penalty != PlayerInfo.PENALTY_NONE) {
                            data.addToPenaltyQueue(side, data.whenPenalized[side][playerID], playerToSub.penalty,
                                    data.robotPenaltyCount[side][playerID]);
                        }

                        playerToSub.penalty = PlayerInfo.PENALTY_SUBSTITUTE;
                        data.robotPenaltyCount[side][playerID] = 0;
                        data.whenPenalized[side][playerID] = data.getTime();
                    }
                }
                // unpenalise selected player:

                if (number != -1)
                    data.team[side].player[number].penalty = PlayerInfo.PENALTY_NONE;
            }
        	
        }
        data.gameState = GameControlData.STATE_SET;
        data.setPlay = GameControlData.SET_PLAY_NONE;
        Log.state(data, "Set");
    }

    /**
     * Checks if this action is legal with the given data (model). Illegal
     * actions are not performed by the EventHandler.
     *
     * @param data The current data to check with.
     */
    @Override
    public boolean isLegal(AdvancedData data) {
        return (data.gameState == GameControlData.STATE_READY)
                || (data.gameState == GameControlData.STATE_SET)
                || ((data.gamePhase == GameControlData.GAME_PHASE_PENALTYSHOOT)
                && ((data.gameState != GameControlData.STATE_PLAYING)
                || (Rules.league.penaltyShotRetries))
                && !data.timeOutActive[0]
                && !data.timeOutActive[1]
                && !data.refereeTimeout)
                || data.testmode;
    }
}
