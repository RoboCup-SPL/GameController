package teamcomm.net;

import common.Log;
import data.SPLStandardMessage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import teamcomm.data.RobotData;

/**
 *
 * @author Felix Thielke
 */
public class SPLStandardMessageReceiver extends Thread {

    private final DatagramSocket datagramSocket;
    private final int teamNumber;

    public SPLStandardMessageReceiver(final int teamNumber) throws SocketException {
        assert (teamNumber > 0 && teamNumber < 100);
        this.teamNumber = teamNumber;
        datagramSocket = new DatagramSocket(null);
        datagramSocket.setReuseAddress(true);
        datagramSocket.bind(new InetSocketAddress(getTeamport(teamNumber)));
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                final ByteBuffer buffer = ByteBuffer.wrap(new byte[SPLStandardMessage.SIZE]);
                final DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.array().length);
                datagramSocket.receive(packet);
                buffer.rewind();

                final SPLStandardMessage message = new SPLStandardMessage();
                RobotData.getInstance().receiveMessage(packet.getAddress().getHostAddress(), teamNumber, message.fromByteArray(buffer) ? message : null);
            } catch (SocketTimeoutException e) {
            } catch (IOException e) {
                Log.error("something went wrong while receiving the game controller packages : " + e.getMessage());
            }
        }

        datagramSocket.close();
    }

    private static int getTeamport(final int teamNumber) {
        return teamNumber + 10000;
    }
}
