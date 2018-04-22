package hulks.message.messages;

import hulks.message.Message;
import hulks.message.data.ComplexStreamReader;
import hulks.message.data.Timestamp;
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

    public Map<Byte, List<Timestamp>> lines = new HashMap<>();

    @Override
    public int getStreamedSize(final ByteBuffer stream) {
        return 1 + ((stream.remaining() - 1) & ~1);
    }

    @Override
    public FieldCoverage read(final ByteBuffer stream) {
        final byte y = stream.get();
        final List<Timestamp> line = lines.getOrDefault(y, new LinkedList<Timestamp>());
        while (stream.remaining() >= 2) {
            final Timestamp timestamp = new Timestamp(Unsigned.toUnsigned(stream.getShort()) * 100);
            if (!line.isEmpty() && timestamp.timestamp < line.get(line.size() - 1).timestamp) {
                line.clear();
            }
            line.add(timestamp);
        }
        lines.put(y, line);

        return this;
    }
}
