package controller.net;

import common.net.SPLStandardMessageReceiver;
import common.net.SPLStandardMessagePackage;
import controller.action.ActionBoard;
import controller.EventHandler;
import java.io.IOException;


public class SPLStandardMessageReceiverGC extends SPLStandardMessageReceiver {

    public SPLStandardMessageReceiverGC(final boolean multicast) throws IOException {
        super(multicast);
    }

    @Override
    protected void handleMessage(final SPLStandardMessagePackage p) {
        int team;
        if (p.team == EventHandler.getInstance().data.team[0].teamNumber) {
            team = 0;
        } else if (p.team == EventHandler.getInstance().data.team[1].teamNumber) {
            team = 1;
        } else {
            return;
        }
        ActionBoard.teamMessage[team].actionPerformed(null);
    }
}
