package teamcomm.net;

import java.net.SocketException;

/**
 *
 * @author Felix Thielke
 */
public class SPLStandardMessageReceiverManager {

    private static final int MAX_TEAMNUMBER = 50;

    private final SPLStandardMessageReceiver[] receivers = new SPLStandardMessageReceiver[MAX_TEAMNUMBER];

    public SPLStandardMessageReceiverManager() throws SocketException {
        for (int i = 0; i < MAX_TEAMNUMBER; i++) {
            receivers[i] = new SPLStandardMessageReceiver(i + 1);
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
}
