package controller.net;

import common.net.SPLStandardMessageReceiver;
import common.net.SPLStandardMessagePackage;
import controller.action.ActionBoard;
import controller.EventHandler;
import data.SPLStandardMessage;
import java.io.IOException;
import java.nio.ByteBuffer;


public class SPLStandardMessageReceiverGC extends SPLStandardMessageReceiver {

    public SPLStandardMessageReceiverGC(final boolean multicast) throws IOException {
        super(multicast, new int[]{EventHandler.getInstance().data.team[0].teamNumber, EventHandler.getInstance().data.team[1].teamNumber});
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

        // Check at least for valid header and version because we don't know what other things may be going on on these ports.
        final SPLStandardMessage message = new SPLStandardMessage();
        message.fromByteArray(ByteBuffer.wrap(p.message));
        if (!(message.headerValid && message.versionValid)) {
            return;
        }

        ActionBoard.teamMessage[team].actionPerformed(null);
    }
}
