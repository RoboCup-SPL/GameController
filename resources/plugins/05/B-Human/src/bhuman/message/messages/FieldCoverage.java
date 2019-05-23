package bhuman.message.messages;

import bhuman.message.Message;
import bhuman.message.data.ComplexStreamReader;
import bhuman.message.data.Timestamp;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import util.Unsigned;

/**
 * Class for the FieldCoverage message.
 *
 * @author Felix Thielke
 */
public class FieldCoverage implements ComplexStreamReader<FieldCoverage>, Message<FieldCoverage> {

    private static final int NUM_OF_CELLS_X = 18;
    private static final int NUM_OF_CELLS_Y = 12;

    public Map<Integer, List<Timestamp>> lines = new HashMap<>();

    @Override
    public int getStreamedSize(final ByteBuffer stream) {
        return 4 + NUM_OF_CELLS_X * NUM_OF_CELLS_Y / 4;
    }

    @Override
    public FieldCoverage read(final ByteBuffer stream) {
        final long timestamp = Unsigned.toUnsigned(stream.getInt());
        int y = 0, counter = 0;
        short coverage = 0;
        while (stream.remaining() > 0) {
            final List<Timestamp> line = new LinkedList<Timestamp>();
            for (int x = 0; x < NUM_OF_CELLS_X; x++) {
                if ((counter % 8) == 0) {
                    coverage = Unsigned.toUnsigned(stream.get());
                }

                line.add(new Timestamp(timestamp - decodeTimeDifference((coverage >> 6) & 3)));
                coverage <<= 2;
                counter += 2;
            }
            lines.put(y, line);
            ++y;
        }

        return this;
    }

    private static long decodeTimeDifference(int code) {
        if (code == 3) {
            return 60000;
        } else if (code == 2) {
            return 20000;
        } else if (code == 1) {
            return 5000;
        }
        return 1000;
    }
}
