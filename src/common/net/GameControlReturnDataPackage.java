package common.net;

import java.io.Serializable;

/**
 * Class for a message. Instances of this class are stored in the log files.
 *
 * @author Felix Thielke
 */
public class GameControlReturnDataPackage implements Serializable {

    private static final long serialVersionUID = 3253050821684309808L;

    /**
     * Host address from which this message was received.
     */
    public final String host;

    /**
     * Raw message data.
     */
    public final byte[] message;

    /**
     * Constructor.
     *
     * @param host host address from which this message was received
     * @param message raw message data
     */
    public GameControlReturnDataPackage(final String host, final byte[] message) {
        this.host = host;
        this.message = message;
    }

}
