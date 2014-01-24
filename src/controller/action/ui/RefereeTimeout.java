package controller.action.ui;

import common.Log;

import controller.action.ActionBoard;
import controller.action.ActionType;
import controller.action.GCAction;
import data.AdvancedData;
import data.GameControlData;

public class RefereeTimeout extends GCAction {

	public RefereeTimeout() {
		super(ActionType.UI);
	}

	@Override
	public void perform(AdvancedData data) {
		if(!data.refereeTimeout) {
            data.timeOut = GameControlData.C_TRUE;
            data.refereeTimeout = true;
            Log.setNextMessage("Referee Timeout");
            data.gameState = -1; //something impossible to force execution of next call
            data.addTimeInCurrentState();
            ActionBoard.initial.perform(data);
        } else {
            data.timeOut = GameControlData.C_FALSE;
            data.refereeTimeout = false;
            Log.setNextMessage("End of Referee Timeout ");
            if(data.secGameState != GameControlData.STATE2_PENALTYSHOOT) {
                ActionBoard.ready.perform(data);
            }
        }
	}

	@Override
	public boolean isLegal(AdvancedData data) {
		//refree can make referee timeout at any time of the game
		return true;
	}

}
