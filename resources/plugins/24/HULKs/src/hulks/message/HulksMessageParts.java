package hulks.message;

import hulks.message.data.NativeReaders;
import common.Log;
import data.SPLStandardMessage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author Felix Thielke
 */
public class HulksMessageParts {

    private static final String BHULKS_STANDARD_MESSAGE_STRUCT_HEADER = "BHLK";
    private static final short BHULKS_STANDARD_MESSAGE_STRUCT_VERSION = 9;

    /**
     * The B-HULKs information transferred in this message.
     */
    public final BHULKsStandardMessage bhulks;
    public final HULKsStandardMessage hulks;

    /**
     * The MessageQueue transferred in this message.
     */
    public final MessageQueue queue;

    public HulksMessageParts(final SPLStandardMessage origin, final ByteBuffer data) {
        BHULKsStandardMessage bhlk = null;
        HULKsStandardMessage hulk = null;
        MessageQueue q = null;

        data.rewind();
        data.order(ByteOrder.LITTLE_ENDIAN);

        String header = new NativeReaders.SimpleStringReader(4).read(data);
        short version = NativeReaders.ucharReader.read(data);
        if (header.equals(BHULKS_STANDARD_MESSAGE_STRUCT_HEADER)) {
            if (version == BHULKS_STANDARD_MESSAGE_STRUCT_VERSION) {
                bhlk = new BHULKsStandardMessage();
                if (bhlk.getStreamedSize(data) <= data.remaining()) {
                    bhlk.read(data);
                } else {
                    Log.error("Wrong size of BHULKs message struct: was " + data.remaining() + ", expected " + bhlk.getStreamedSize(data));
                }
            } else {
                Log.error("Wrong BHULKs message struct version: was " + version + ", expected " + BHULKS_STANDARD_MESSAGE_STRUCT_VERSION);
            }
            hulk = new HULKsStandardMessage();
            hulk.read(data);
            if (!hulk.isValid()) {
                Log.error("Received invalid HULKs message.");
            }
        } else {
            Log.error("Could not find BHULKs message struct header");
        }

        this.bhulks = bhlk;
        this.hulks = hulk;
        this.queue = q;
    }

}
