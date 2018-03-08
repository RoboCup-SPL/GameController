package teamcomm.net;

import common.Log;
import data.GameControlData;
import data.TrueDataRequest;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.EventObject;
import javax.swing.event.EventListenerList;
import teamcomm.data.event.GameControlDataEvent;
import teamcomm.data.event.GameControlDataEventListener;
import teamcomm.data.event.GameControlDataTimeoutEvent;

/**
 * Class for the thread which receives messages from the GameController.
 *
 * @author Felix Thielke
 */
public class GameControlDataReceiver extends Thread {

    private static final int GAMECONTROLLER_TIMEOUT = 4000;

    private static final int REQUEST_TRUE_DATA_AFTER = 1000;

    private final DatagramSocket datagramSocket;

    private final EventListenerList listeners = new EventListenerList();

    private final boolean tryToGetTrueData;

    private long timestampOfLastTrueGameControlData = 0;

    /**
     * Constructor.
     *
     * @throws SocketException if the socket cannot be bound
     */
    public GameControlDataReceiver() throws SocketException {
        this(false);
    }

    /**
     * Constructor.
     *
     * @param tryToGetTrueData set to true to request true game state data from
     * the GameController
     * @throws SocketException if the socket cannot be bound
     */
    public GameControlDataReceiver(final boolean tryToGetTrueData) throws SocketException {
        setName("GameControlDataReceiver");
        this.tryToGetTrueData = tryToGetTrueData;

        datagramSocket = new DatagramSocket(null);
        datagramSocket.setReuseAddress(true);
        datagramSocket.setSoTimeout(GAMECONTROLLER_TIMEOUT);
        datagramSocket.bind(new InetSocketAddress(GameControlData.GAMECONTROLLER_GAMEDATA_PORT));
    }

    public void addListener(final GameControlDataEventListener listener) {
        listeners.add(GameControlDataEventListener.class, listener);
    }

    public void removeListener(final GameControlDataEventListener listener) {
        listeners.remove(GameControlDataEventListener.class, listener);
    }

    private void fireEvent(final EventObject e) {
        for (final GameControlDataEventListener listener : listeners.getListeners(GameControlDataEventListener.class)) {
            if (e instanceof GameControlDataEvent) {
                listener.gameControlDataChanged((GameControlDataEvent) e);
            } else if (e instanceof GameControlDataTimeoutEvent) {
                listener.gameControlDataTimeout((GameControlDataTimeoutEvent) e);
            }
        }
    }

    /**
     * Sends a request for true game state data to the GameController.
     *
     * @param gameControllerAddress InetAddress of the GameController
     * @throws IOException if an error occurred while sending
     */
    private void requestTrueData(final InetAddress gameControllerAddress) throws IOException {
        final byte[] request = TrueDataRequest.createRequest().toByteArray();
        final DatagramSocket ds = new DatagramSocket();
        ds.send(new DatagramPacket(request, request.length, gameControllerAddress, TrueDataRequest.GAMECONTROLLER_TRUEDATAREQUEST_PORT));
        ds.close();
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
                    if (tryToGetTrueData == data.isTrueData || (tryToGetTrueData && System.currentTimeMillis() - timestampOfLastTrueGameControlData > 2 * REQUEST_TRUE_DATA_AFTER)) {
                        fireEvent(new GameControlDataEvent(this, data));
                    }

                    if (tryToGetTrueData) {
                        if (data.isTrueData) {
                            timestampOfLastTrueGameControlData = System.currentTimeMillis();
                        } else if (System.currentTimeMillis() - timestampOfLastTrueGameControlData >= REQUEST_TRUE_DATA_AFTER) {
                            try {
                                requestTrueData(packet.getAddress());
                            } catch (IOException e) {
                                Log.error("something went wrong trying to request true game data : " + e.getMessage());
                            }
                        }
                    }
                }
            } catch (SocketTimeoutException e) {
                fireEvent(new GameControlDataTimeoutEvent(this));
            } catch (IOException e) {
                Log.error("something went wrong while receiving the game controller packages : " + e.getMessage());
            }
        }

        datagramSocket.close();
    }
}
