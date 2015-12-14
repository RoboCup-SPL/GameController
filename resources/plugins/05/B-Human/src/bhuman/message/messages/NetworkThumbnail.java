package bhuman.message.messages;

import bhuman.message.Message;
import bhuman.message.data.NativeReaders;
import java.nio.ByteBuffer;

/**
 * Class for the NetworkThumbnail message.
 *
 * @author Felix Thielke
 */
public class NetworkThumbnail extends Message<NetworkThumbnail> {

    /**
     * Sequence number of the current part of the image (starts at 0).
     */
    public byte sequence;
    /**
     * Part of the RLE-encoded image data.
     */
    public byte[] data;

    @Override
    public NetworkThumbnail read(final ByteBuffer stream) {
        sequence = NativeReaders.scharReader.read(stream);
        int count = stream.getInt();
        NativeReaders.SCharArrayReader reader = new NativeReaders.SCharArrayReader(count);
        if (stream.remaining() != reader.getStreamedSize()) {
            return null;
        }
        data = reader.read(stream);

        return this;
    }

}
