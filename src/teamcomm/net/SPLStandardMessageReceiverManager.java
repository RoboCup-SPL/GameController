package teamcomm.net;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.SocketException;

/**
 *
 * @author Felix Thielke
 */
public class SPLStandardMessageReceiverManager {

    private static final int MAX_TEAMNUMBER = 50;

    private final SPLStandardMessageReceiver[] receivers = new SPLStandardMessageReceiver[MAX_TEAMNUMBER];

    public SPLStandardMessageReceiverManager() throws IOException, SocketException {
        this(false);
    }

    public SPLStandardMessageReceiverManager(final boolean local) throws IOException, SocketException {
        for (int i = 0; i < MAX_TEAMNUMBER; i++) {
            receivers[i] = new SPLStandardMessageReceiver(i + 1, local);
        }
    }

    public void start() {
        for (SPLStandardMessageReceiver r : receivers) {
            r.start();
        }
    }

    public void interrupt() {
        for (SPLStandardMessageReceiver r : receivers) {
            r.interrupt();
        }
    }

    public void join() throws InterruptedException {
        for (SPLStandardMessageReceiver r : receivers) {
            r.join();
        }
    }

    public void join(final long millis) throws InterruptedException {
        for (SPLStandardMessageReceiver r : receivers) {
            r.join(millis);
        }
    }
    
    public void setLogger(final ObjectOutputStream logStream) {
        for (SPLStandardMessageReceiver r : receivers) {
            r.setLogger(logStream);
        }
    }
}
