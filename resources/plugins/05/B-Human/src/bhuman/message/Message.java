package bhuman.message;

import bhuman.message.data.SimpleStreamReader;
import bhuman.message.data.StreamReader;
import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.Map;

/**
 * Abstract base class for messages read from the message queue.
 *
 * @author Felix Thielke
 * @param <T> type of the implementing subclass
 */
public abstract class Message<T extends Message> implements StreamReader<T> {

    private static final Map<MessageID, Class<? extends Message>> classes = new EnumMap<>(MessageID.class);

    /**
     * Creates the appropriate message for the given MessageID and raw data.
     *
     * @param identifier MessageID of the message
     * @param data raw data of the message
     * @return message or null if an error occurred
     */
    public static Message<? extends Message> factory(final MessageID identifier, final ByteBuffer data) {
        // Accumulate implemented message classes
        if (classes.isEmpty()) {
            for (final MessageID id : MessageID.values()) {
                try {
                    final Class<? extends Message> cls = Class.forName("bhuman.message.messages." + id.toString().substring(2)).asSubclass(Message.class);

                    if (Message.class.isAssignableFrom(cls)) {
                        classes.put(id, cls);
                    }
                } catch (ClassNotFoundException ex) {
                }
            }
        }

        // Instantiate the message class that fits the MessageID
        final Class<? extends Message> cls = classes.get(identifier);
        if (cls != null) {
            try {
                final Message inst = cls.newInstance();
                if (SimpleStreamReader.class.isInstance(inst)) {
                    if (data.remaining() != ((SimpleStreamReader) inst).getStreamedSize()) {
                        return null;
                    }
                }
                return (Message<? extends Message>) inst.read(data);
            } catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException ex) {
            }
        }

        return null;
    }

}
