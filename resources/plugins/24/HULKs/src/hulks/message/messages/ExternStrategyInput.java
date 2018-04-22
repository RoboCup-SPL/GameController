package hulks.message.messages;

import hulks.message.Message;
import hulks.message.data.SimpleStreamReader;
import java.nio.ByteBuffer;
import java.util.EnumMap;
import util.Unsigned;

/**
 * Class for the ExternStrategyInput message.
 *
 * @author Felix Thielke
 */
public class ExternStrategyInput implements SimpleStreamReader<ExternStrategyInput>, Message<ExternStrategyInput> {

    public static enum StrategyType {

        enableBreakingSupporterBlock,
    }

    public float priority; //< Higher value == higher priority
    public EnumMap<StrategyType, Boolean> externStrategy = new EnumMap<>(StrategyType.class);

    @Override
    public int getStreamedSize() {
        return 8;
    }

    @Override
    public ExternStrategyInput read(final ByteBuffer stream) {
        priority = stream.getFloat();

        final long bitfield = Unsigned.toUnsigned(stream.getInt());
        long bit = 1;
        for (final StrategyType type : StrategyType.values()) {
            externStrategy.put(type, (bitfield & bit) != 0);
            bit <<= 1;
        }

        return this;
    }

}
