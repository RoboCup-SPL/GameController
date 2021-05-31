package controller.action.ui;

import common.Log;

import controller.action.ActionType;
import controller.action.GCAction;
import data.AdvancedData;
import data.GameControlData;
import data.Rules;

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
        ++data.incGameClockCounter;
        Log.state(data, "Increase Game Clock");
    }

    @Override
    public boolean isLegal(AdvancedData data)
    {
        return (data.competitionType == GameControlData.COMPETITION_TYPE_1VS1
                    ? data.firstHalf == 0
                    && data.team[0].score * (data.autonomouslyCalibrated[0] ? Rules.league.autonomousCalibrationScoreFactor : 1.f) == data.team[1].score * (data.autonomouslyCalibrated[1] ? Rules.league.autonomousCalibrationScoreFactor : 1.f)
                    && data.getRemainingGameTime(true) <= 10
                    && data.incGameClockCounter < 5
                    : data.gameState != GameControlData.STATE_PLAYING
                    && data.timeBeforeCurrentGameState >= 1000*60)
                || data.testmode;
    }
}
