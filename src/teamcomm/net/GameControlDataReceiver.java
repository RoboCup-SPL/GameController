package teamcomm.net;

import common.Log;
import data.GameControlData;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import teamcomm.data.RobotData;

/**
 * Class for the thread which receives messages from the GameController.
 * 
 * @author Felix Thielke
 */
public class GameControlDataReceiver extends Thread {

    private static final int GAMECONTROLLER_TIMEOUT = 4000;

    private final DatagramSocket datagramSocket;

    /**
     * Constructor.
     * 
     * @throws SocketException if the socket cannot be bound
     */
    public GameControlDataReceiver() throws SocketException {
        setName("GameControlDataReceiver");
        
        datagramSocket = new DatagramSocket(null);
        datagramSocket.setReuseAddress(true);
        datagramSocket.setSoTimeout(GAMECONTROLLER_TIMEOUT);
        datagramSocket.bind(new InetSocketAddress(GameControlData.GAMECONTROLLER_GAMEDATA_PORT));
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                final ByteBuffer buffer = ByteBuffer.wrap(new byte[GameControlData.SIZE]);
                final DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.array().length);
                datagramSocket.receive(packet);
                buffer.rewind();

                final GameControlData data = new GameControlData();
                if (data.fromByteArray(buffer)) {
                    RobotData.getInstance().updateGameData(data);
                }
            } catch (SocketTimeoutException e) {
                // GameController data is only valid for a limited amount of time
                RobotData.getInstance().updateGameData(null);
            } catch (IOException e) {
                Log.error("something went wrong while receiving the game controller packages : " + e.getMessage());
            }
        }

        datagramSocket.close();
    }
}
