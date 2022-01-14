package common.net;

import common.Log;
import common.net.logging.Logger;
import data.GameControlData;
import data.GameControlReturnData;
import data.Rules;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 *
 * @author Marcel Steinbeck
 *
 * This class is used to receive a packet send by a robot on port
 * {@link GameControlData#GAMECONTROLLER_RETURNDATA_PORT} via UDP over
 * broadcast. If a package was received, this class will invoke
 * {@link RobotWatcher#update(data.GameControlReturnData)} to update the robots
 * online status.
 */
public class GameControlReturnDataReceiver extends Thread {

    /**
     * The used socket to receive the packages.
     */
    private final DatagramSocket datagramSocket;

    /**
     * Creates a new Receiver.
     *
     * @param address the InetAddress to listen on.<br />
     * Only applied if
     * {@link Rules#dropBroadcastMessages rule.dropBroadcastMessages} is set to
     * true.
     * @throws SocketException the an error occurs while creating the socket
     * @throws UnknownHostException if (internally chosen) inet-address is not
     * valid or no network device is bound to an address matching the regex
     * (ignoring loopback interfaces)
     */
    public GameControlReturnDataReceiver(final InetAddress address) throws SocketException, UnknownHostException {
        datagramSocket = new DatagramSocket(null);
        datagramSocket.setReuseAddress(true);
        datagramSocket.setSoTimeout(500);
        if (Rules.league.dropBroadcastMessages) {
            datagramSocket.bind(new InetSocketAddress(address,
                    GameControlData.GAMECONTROLLER_RETURNDATA_PORT));
        } else {
            datagramSocket
                    .bind(new InetSocketAddress(GameControlData.GAMECONTROLLER_RETURNDATA_PORT));
        }
    }

    protected void handleMessage(final GameControlReturnDataPackage p) {

    }

    @Override
    public void run() {
        final byte[] buffer = new byte[GameControlReturnData.SIZE];
        final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (!isInterrupted()) {

            try {
                datagramSocket.receive(packet);

                final GameControlReturnDataPackage p = new GameControlReturnDataPackage(packet.getAddress().getHostAddress(), buffer);
                Logger.getInstance().log(p);
                handleMessage(p);
            } catch (SocketTimeoutException e) { // ignore, because we set a timeout
            } catch (IOException e) {
                Log.error("something went wrong while receiving : " + e.getMessage());
            }
        }

        datagramSocket.close();
    }
}
