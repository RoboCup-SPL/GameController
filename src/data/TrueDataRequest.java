package data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Class for a message which can be sent to the GameController in order to
 * request true game state information. This can be used for displaying the true
 * game state in the TeamCommunicationMonitor/GameStateVisualizer and similar
 * applications. See teamcomm.net.GameControlDataReceiver for a reference
 * implementation.
 *
 * @author Felix Thielke
 */
public class TrueDataRequest {

    public static final int GAMECONTROLLER_TRUEDATAREQUEST_PORT = 3636; // port to receive requests for true game state data on

    public static final String TRUEDATAREQUEST_HEADER = "RGTr";
    public static final byte TRUEDATAREQUEST_VERSION = 0;
    public static final byte TRUEDATAREQUEST_SIZE = 4 + 1;

    public String header;
    public byte version;

    public static TrueDataRequest createRequest() {
        final TrueDataRequest request = new TrueDataRequest();
        request.header = TRUEDATAREQUEST_HEADER;
        request.version = TRUEDATAREQUEST_VERSION;
        return request;
    }

    public boolean fromByteArray(final ByteBuffer buf) {
        buf.order(ByteOrder.LITTLE_ENDIAN);
        final byte[] headerBytes = new byte[4];
        buf.get(headerBytes);
        header = new String(headerBytes);
        version = buf.get();
        return !buf.hasRemaining() && header.equals(TRUEDATAREQUEST_HEADER) && version == TRUEDATAREQUEST_VERSION;
    }

    public byte[] toByteArray() {
        final ByteBuffer buf = ByteBuffer.allocate(TRUEDATAREQUEST_SIZE);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put(header.getBytes());
        buf.put(version);
        return buf.array();
    }
}
