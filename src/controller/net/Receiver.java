package controller.net;

import controller.Log;
import data.GameControlData;
import data.GameControlReturnData;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 *
 * @author: Marcel Steinbeck
 *
 * This class is used to receive a packe send by a robot on port {@link GameControlData#GAMECONTROLLER_PORT} via UDP
 * over broadcast.
 * If a package was received, this class will invoke {@link RobotWatcher#update(data.GameControlReturnData)} to update
 * the robots online status.
 *
 * This class is a sigleton!
 */
public class Receiver extends Thread
{
    /** The instance of the singleton. */
    private static Receiver instance;

    /** The used socket to receive the packages. */
    private final DatagramSocket datagramSocket;

    /**
     * Creates a new Receiver.
     *
     * @throws SocketException the an error occurs while creating the socket
     */
    private Receiver() throws SocketException
    {
        datagramSocket = new DatagramSocket(null);
        datagramSocket.setReuseAddress(true);
        datagramSocket.bind(new InetSocketAddress(GameControlData.GAMECONTROLLER_PORT));
    }

    /**
     * Returns the instance of the singleton. If the Receiver wasn't initialized once before, a new instance will
     * be created and returned (lazy instantiation)
     *
     * @return  The instance of the Receiver
     * @throws IllegalStateException if the first creation of the singleton throws an exception
     */
    public synchronized static Receiver getInstance()
    {
        if(instance == null) {
            try {
                instance = new Receiver();
            } catch(SocketException e) {
                throw new IllegalStateException("fatal: Error while setting up Receiver.", e);
            }
        }
        return instance;
    }

    @Override
    public void run() {
       while(!isInterrupted()) {
           final ByteBuffer buffer = ByteBuffer.wrap(new byte[GameControlReturnData.SIZE]);
           final GameControlReturnData player = new GameControlReturnData();

           final DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.array().length);

            try {
                datagramSocket.receive(packet);
                buffer.rewind();

                if(player.fromByteArray(buffer)) {
                    RobotWatcher.update(player);
                }
            } catch(IOException e) {
                Log.error("something went wrong while receiving");
            }
        }

        datagramSocket.close();
    }
}
