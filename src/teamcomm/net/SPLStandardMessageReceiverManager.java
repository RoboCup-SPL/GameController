package teamcomm.net;

import java.net.SocketException;

/**
 *
 * @author Felix Thielke
 */
public class SPLStandardMessageReceiverManager implements ISPLStandardMessageReceiver {

    private static final int MAX_TEAMNUMBER = 50;

    private final SPLStandardMessageReceiver[] receivers = new SPLStandardMessageReceiver[MAX_TEAMNUMBER];

    public SPLStandardMessageReceiverManager() throws SocketException {
        for (int i = 0; i < MAX_TEAMNUMBER; i++) {
            receivers[i] = new SPLStandardMessageReceiver(i + 1);
        }
    }

    @Override
    public void start() {
        for (SPLStandardMessageReceiver r : receivers) {
            r.start();
        }
    }

    @Override
    public void interrupt() {
        for (SPLStandardMessageReceiver r : receivers) {
            r.interrupt();
        }
    }

    @Override
    public void join() throws InterruptedException {
        for (SPLStandardMessageReceiver r : receivers) {
            r.join();
        }
    }

    @Override
    public void join(final long millis) throws InterruptedException {
        for (SPLStandardMessageReceiver r : receivers) {
            r.join(millis);
        }
    }
}
