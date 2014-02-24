package controller.action.ui;

import common.Log;

import controller.action.ActionType;
import controller.action.GCAction;
import data.AdvancedData;

public class IncGameClock extends GCAction {

    public IncGameClock() {
        super(ActionType.UI);
    }

    @Override
    public void perform(AdvancedData data) {
        data.timeBeforeCurrentGameState -= 1000*60;
        Log.state(data, "Increase Game Clock");
    }

    @Override
    public boolean isLegal(AdvancedData data) {
        return true;
    }

}
