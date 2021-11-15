package bhuman.message.data;

import java.nio.ByteBuffer;
import util.Unsigned;

/**
 *
 * @author Felix Thielke
 */
public class Timestamp implements SimpleStreamReader<Timestamp> {

    public long timestamp;

    public Timestamp() {
    }

    public Timestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int getStreamedSize() {
        return 4;
    }

    @Override
    public Timestamp read(final ByteBuffer stream) {
        timestamp = Unsigned.toUnsigned(stream.getInt());
        return this;
    }

    public long getTimeSince(final long other) {
        return timestamp - other;
    }

}
