package controller.net;

import common.net.SPLTeamMessagePackage;
import common.net.SPLTeamMessageReceiver;
import controller.action.ActionBoard;
import controller.EventHandler;
import java.io.IOException;
import java.nio.ByteBuffer;


public class SPLTeamMessageReceiverGC extends SPLTeamMessageReceiver {

    public SPLTeamMessageReceiverGC(final boolean multicast) throws IOException {
        super(multicast, new int[]{EventHandler.getInstance().data.team[0].teamNumber, EventHandler.getInstance().data.team[1].teamNumber});
    }

    @Override
    protected void handleMessage(final SPLTeamMessagePackage p) {
        int team;
        if (p.team == EventHandler.getInstance().data.team[0].teamNumber) {
            team = 0;
        } else if (p.team == EventHandler.getInstance().data.team[1].teamNumber) {
            team = 1;
        } else {
            return;
        }

        // TODO: if p.message.length > SPLTeamMessage.MAX_SIZE -> this is illegal

        ActionBoard.teamMessage[team].actionPerformed(null);
    }
}
