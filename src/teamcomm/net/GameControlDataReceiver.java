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

    private final DatagramSocket datagramSocket;

    private final EventListenerList listeners = new EventListenerList();

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
                    fireEvent(new GameControlDataEvent(this, data));
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
