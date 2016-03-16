package controller.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.regex.Pattern;

import common.Log;

import data.GameControlData;
import data.GameControlReturnData;
import data.Rules;

/**
 *
 * @author Marcel Steinbeck
 *
 * This class is used to receive a packet send by a robot on port {@link GameControlData#GAMECONTROLLER_RETURNDATA_PORT} via UDP
 * over broadcast.
 * If a package was received, this class will invoke {@link RobotWatcher#update(data.GameControlReturnData)} to update
 * the robots online status.
 *
 * This class is a sigleton!
 */
public class GameControlReturnDataReceiver extends Thread
{
    /** The instance of the singleton. */
    private static GameControlReturnDataReceiver instance;

    private static Pattern LOOPBACK_PATTERN = Pattern.compile("^127(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");

    /** The used socket to receive the packages. */
    private final DatagramSocket datagramSocket;

    /**
     * Creates a new Receiver.
     *
     * @param pattern the {@link Pattern} the local address has to match against.<br />
     *        Only applied if {@link Rules#dropBroadcastMessages rule.dropBroadcastMessages} is set
     *        to true.
     * @throws SocketException the an error occurs while creating the socket
     * @throws UnknownHostException if (internally chosen) inet-address is not valid or no network
     *         device is bound to an address matching the regex (ignoring loopback interfaces)
     */
    private GameControlReturnDataReceiver(final Pattern pattern) throws SocketException, UnknownHostException
    {
        datagramSocket = new DatagramSocket(null);
        datagramSocket.setReuseAddress(true);
        datagramSocket.setSoTimeout(500);
        if (Rules.league.dropBroadcastMessages) {
            Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();
            nifs: while (nifs.hasMoreElements()) {
                Enumeration<InetAddress> ips = nifs.nextElement().getInetAddresses();
                while (ips.hasMoreElements()) {
                    String addr = ips.nextElement().getHostAddress();
                    if (pattern.matcher(addr).matches()
                            && !LOOPBACK_PATTERN.matcher(addr).matches()) {
                        try {
                            datagramSocket.bind(new InetSocketAddress(InetAddress.getByName(addr),
                                    GameControlData.GAMECONTROLLER_RETURNDATA_PORT));
                        } catch (UnknownHostException e) {
                            // this can never happen, the address got retrieved from the system so it's known
                        }
                        System.out.printf("Listening on %s:%d%n", addr, GameControlData.GAMECONTROLLER_RETURNDATA_PORT);
                        break nifs;
                    }
                }
            }
            if(!datagramSocket.isBound()) {
                throw new UnknownHostException("no local address matches the regex (excluding loopback interfaces)");
            }
        } else {
            datagramSocket.bind(new InetSocketAddress(GameControlData.GAMECONTROLLER_RETURNDATA_PORT));
        }
    }

    /**
     * Initializes the GameControlReturnDataReceiver. This needs to be called before
     * {@link #getInstance()} is available.
     * 
     * @param addresspattern the {@link Pattern} the local address has to match against.<br />
     *        Only applied if {@link Rules#dropBroadcastMessages rule.dropBroadcastMessages} is set
     *        to true.
     * @throws SocketException if an error occurs while creating the socket
     * @throws UnknownHostException if (internally chosen) inet-address is not valid or no network
     *         device is bound to an address matching the regex (ignoring loopback interfaces)
     * @throws IllegalStateException if the Receiver is already initialized
     */
    public synchronized static void initialize(final Pattern addresspattern) throws SocketException, UnknownHostException
    {
        if (instance != null) {
            throw new IllegalStateException("receiver is already initialized");
        } if (addresspattern == null) {
            throw new IllegalArgumentException("null is not avalid pattern");
        } else {
            instance = new GameControlReturnDataReceiver(addresspattern);
        }
    }

    /**
     * Returns the instance of the singleton.
     *
     * @return  The instance of the Receiver
     * @throws  IllegalStateException if the Receiver is not initialized yet
     */
    public synchronized static GameControlReturnDataReceiver getInstance()
    {
        if (instance == null) {
            throw new IllegalStateException("receiver is not initialized yet");
        } else {
            return instance;
        }
    }

    @Override
    public void run() {
       while (!isInterrupted()) {
           final ByteBuffer buffer = ByteBuffer.wrap(new byte[Math.max(GameControlReturnData.SIZE, GameControlReturnData.SIZE1)]);
           final GameControlReturnData player = new GameControlReturnData();
           
           final DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.array().length);

            try {
                datagramSocket.receive(packet);
                buffer.rewind();
                if (player.fromByteArray(buffer)) {
                    RobotWatcher.update(player);
                }
            } catch (SocketTimeoutException e) { // ignore, because we set a timeout
            } catch (IOException e) {
                Log.error("something went wrong while receiving : " + e.getMessage());
            }
        }

        datagramSocket.close();
    }
}
