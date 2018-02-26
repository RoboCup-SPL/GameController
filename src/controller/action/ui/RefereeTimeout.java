package controller.action.ui;

import common.Log;

import controller.action.ActionBoard;
import controller.action.ActionType;
import controller.action.GCAction;
import data.AdvancedData;
import data.GameControlData;

public class RefereeTimeout extends GCAction
{
    public RefereeTimeout()
    {
        super(ActionType.UI);
    }

    @Override
    public void perform(AdvancedData data)
    {
        if (!data.refereeTimeout) {
            data.previousGamePhase = data.gamePhase;
            data.gamePhase = GameControlData.GAME_PHASE_TIMEOUT;
            data.refereeTimeout = true;
            Log.setNextMessage("Referee Timeout");
            if (data.gameState == GameControlData.STATE_PLAYING) {
                data.addTimeInCurrentState();
            }
            if (data.previousGamePhase == GameControlData.GAME_PHASE_PENALTYSHOOT
                    && (data.gameState == GameControlData.STATE_SET || data.gameState == GameControlData.STATE_PLAYING)) {
                data.team[data.kickingTeam == data.team[0].teamNumber ? 0 : 1].penaltyShot--;
            }
            
            data.gameState = -1; //something impossible to force execution of next call
            ActionBoard.initial.perform(data);
        } else {
            data.gamePhase = data.previousGamePhase;
            data.previousGamePhase = GameControlData.GAME_PHASE_TIMEOUT;
            data.refereeTimeout = false;
            data.kickOffReason = AdvancedData.KICKOFF_TIMEOUT;
            Log.setNextMessage("End of Referee Timeout");
            if (data.gamePhase != GameControlData.GAME_PHASE_PENALTYSHOOT) {
                ActionBoard.ready.perform(data);
            }
        }
    }

    @Override
    public boolean isLegal(AdvancedData data)
    {
        return data.gameState != GameControlData.STATE_FINISHED
                && !data.timeOutActive[0] && !data.timeOutActive[1];
    }

}
