package data;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class TeamMessage implements Serializable {

    private static final long serialVersionUID = 2204681477211322628L;

    public static final int MAX_SIZE = 512;

    public byte[] data;

    public boolean valid;

    public static TeamMessage createFrom(final TeamMessage message) {
        final TeamMessage m = new TeamMessage();
        m.data = message.data;
        m.valid = message.valid;
        return m;
    }

    public boolean fromByteArray(ByteBuffer buffer) {
        valid = true;
        try {
            if (buffer.remaining() > MAX_SIZE) {
                valid = false;
            }
            data = new byte[Math.min(buffer.remaining(), MAX_SIZE)];
            buffer.get(data, 0, data.length);
        } catch (RuntimeException e) {
            valid = false;
        }

        return valid;
    }
}
