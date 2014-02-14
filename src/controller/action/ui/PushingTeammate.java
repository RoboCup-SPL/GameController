package controller.action.ui;

import common.Log;

import controller.EventHandler;
import controller.action.ActionType;
import controller.action.GCAction;
import data.AdvancedData;
import data.GameControlData;
import data.PlayerInfo;
import data.Rules;

public class PushingTeammate extends GCAction {
	public int side;
	
	public PushingTeammate(int side) {
		super(ActionType.UI);
		this.side = side;
	}

	@Override
	public void performOn(AdvancedData data, PlayerInfo player, int side, int number){
		Log.state(data, "Pushing Teammate "+
                Rules.league.teamColorName[data.team[side].teamColor]
                + " " + (number+1));
	}

	@Override
	public boolean isLegal(AdvancedData data) {
		return (data.gameState == GameControlData.STATE_PLAYING) || (data.testmode);
	}

	@Override
	public void perform(AdvancedData data) {
		if(EventHandler.getInstance().lastUIEvent == this) {
            EventHandler.getInstance().noLastUIEvent = true;
        }
	}

}
