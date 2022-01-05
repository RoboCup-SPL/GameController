package controller.action.ui;

import common.Log;

import controller.action.ActionType;
import controller.action.GCAction;
import data.AdvancedData;
import data.GameControlData;
import data.Rules;
import data.TeamInfo;

public class IncGameClock extends GCAction
{
    public IncGameClock()
    {
        super(ActionType.UI);
    }

    @Override
    public void perform(AdvancedData data)
    {
        data.timeBeforeCurrentGameState -= 1000*60;
        if (data.gameState != GameControlData.STATE_PLAYING && data.timeBeforeStoppageOfPlay != 0) {
            data.timeBeforeStoppageOfPlay -= 1000 * 60;
        }
        for (TeamInfo teamInfo : data.team) {
            teamInfo.messageBudget += Rules.league.additionalMessageBudgetPerMinute;
        }
        Log.state(data, "Increase Game Clock");
    }

    @Override
    public boolean isLegal(AdvancedData data)
    {
        return data.gameState != GameControlData.STATE_PLAYING
                && data.timeBeforeCurrentGameState >= 1000*60
                || data.testmode;
    }
}
