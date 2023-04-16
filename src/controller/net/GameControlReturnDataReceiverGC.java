package controller.net;

import common.net.GameControlReturnDataPackage;
import common.net.GameControlReturnDataReceiver;
import data.GameControlReturnData;
import data.Rules;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class GameControlReturnDataReceiverGC extends GameControlReturnDataReceiver {

    private TrueDataSender trueDataSender;

    public GameControlReturnDataReceiverGC(final InetAddress address, final TrueDataSender trueDataSender,
                                           final boolean generateIP) throws SocketException, UnknownHostException {
        super(Rules.league.dropBroadcastMessages ? address : null, generateIP);
        this.trueDataSender = trueDataSender;
    }

    @Override
    protected void handleMessage(final GameControlReturnDataPackage p) {
        trueDataSender.handleGameControlReturnData(p);

        GameControlReturnData player = new GameControlReturnData();
        player.fromByteArray(ByteBuffer.wrap(p.message));
        if (!(player.headerValid && player.versionValid && player.playerNumValid && player.teamNumValid)) {
            return;
        }

        RobotWatcher.update(player);
    }
}
