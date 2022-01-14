package controller.net;

import common.net.GameControlReturnDataPackage;
import common.net.GameControlReturnDataReceiver;
import data.GameControlReturnData;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class GameControlReturnDataReceiverGC extends GameControlReturnDataReceiver {

    private TrueDataSender trueDataSender;

    public GameControlReturnDataReceiverGC(final InetAddress address, final TrueDataSender trueDataSender) throws SocketException, UnknownHostException {
        super(address);
        this.trueDataSender = trueDataSender;
    }

    @Override
    protected void handleMessage(final GameControlReturnDataPackage p) {
        try {
            trueDataSender.putOnBlacklist(InetAddress.getByName(p.host));
        } catch (UnknownHostException e) {
        }
        GameControlReturnData player = new GameControlReturnData();
        if (player.fromByteArray(ByteBuffer.wrap(p.message))) {
            RobotWatcher.update(player);
        }
    }
}
