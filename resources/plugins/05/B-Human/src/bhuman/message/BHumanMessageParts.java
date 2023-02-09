package bhuman.message;

import bhuman.message.data.NativeReaders;
import common.Log;
import data.SPLStandardMessage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author Felix Thielke
 */
public class BHumanMessageParts {

    /**
     * The B-Human standard information transferred in this message.
     */
    public final BHumanStandardMessage bhuman;

    public BHumanMessageParts(final SPLStandardMessage origin, final ByteBuffer data) {
        BHumanStandardMessage bhum = null;

        data.rewind();
        data.order(ByteOrder.LITTLE_ENDIAN);

        if (data.hasRemaining()) {
            String header = new NativeReaders.SimpleStringReader(4).read(data);
            short version = NativeReaders.ucharReader.read(data);
            if (header.equals(BHumanStandardMessage.BHUMAN_STANDARD_MESSAGE_STRUCT_HEADER)) {
                if (version == BHumanStandardMessage.BHUMAN_STANDARD_MESSAGE_STRUCT_VERSION) {
                    bhum = new BHumanStandardMessage();
                    if (bhum.getStreamedSize(data) <= data.remaining()) {
                        bhum.read(data, origin.playerNum);
                    } else {
                        Log.error("Wrong size of B-Human standard message struct: was " + data.remaining() + ", expected " + bhum.getStreamedSize(data));
                    }
                } else {
                    Log.error("Wrong B-Human standard message struct version: was " + version + ", expected " + BHumanStandardMessage.BHUMAN_STANDARD_MESSAGE_STRUCT_VERSION);
                }
            } else {
                data.position(data.position() - 5);
                Log.error("Wrong B-Human standard message struct header");
            }
        }

        this.bhuman = bhum;
    }
}
