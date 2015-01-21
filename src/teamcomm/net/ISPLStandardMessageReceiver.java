package teamcomm.net;

/**
 *
 * @author Felix Thielke
 */
public interface ISPLStandardMessageReceiver {
    public void start();

    public void interrupt();

    public void join() throws InterruptedException;

    public void join(final long millis) throws InterruptedException;
}
