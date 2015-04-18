package bhuman.message;

import bhuman.message.data.StreamReader;
import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author Felix Thielke
 */
public abstract class Message<T extends Message> implements StreamReader<T> {

    private static final Map<MessageID, Class<Message<? extends Message>>> classes = new EnumMap<MessageID, Class<Message<? extends Message>>>(MessageID.class);

    public static Message<? extends Message> factory(final MessageID identifier, final ByteBuffer data) {
        //System.out.println(identifier + " (" + data.remaining() + ")");

        if (classes.isEmpty()) {
            for (final MessageID id : MessageID.values()) {
                try {
                    final Class<Message<? extends Message>> cls = (Class<Message<? extends Message>>) Class.forName("bhuman.message.messages." + id.toString().substring(2));

                    classes.put(id, cls);
                } catch (ClassNotFoundException ex) {
                }
            }
        }

        final Class<Message<? extends Message>> cls = classes.get(identifier);
        if (cls != null) {
            try {
                return cls.newInstance().read(data);
            } catch (SecurityException ex) {
            } catch (InstantiationException ex) {
            } catch (IllegalAccessException ex) {
            } catch (IllegalArgumentException ex) {
            }
        }

        return null;
    }

}
