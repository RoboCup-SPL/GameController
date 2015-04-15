package bhuman.message.messages;

import bhuman.message.MessageID;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author Felix Thielke
 */
public abstract class Message {

    protected Message(final byte[] data) {
    }

    private static final Map<MessageID, Class<Message>> classes = new EnumMap<MessageID, Class<Message>>(MessageID.class);

    public static Message factory(final MessageID identifier, final byte[] data) {
        if (classes.isEmpty()) {
            for (final MessageID id : MessageID.values()) {
                try {
                    final Class<Message> cls = (Class<Message>) Class.forName("bhuman.message.messages." + id.toString().substring(2));

                    classes.put(id, cls);
                } catch (ClassNotFoundException ex) {
                }
            }
        }

        final Class<Message> cls = classes.get(identifier);
        if (cls != null) {
            try {
                return cls.getConstructor(byte[].class).newInstance(data);
            } catch (NoSuchMethodException ex) {
            } catch (SecurityException ex) {
            } catch (InstantiationException ex) {
            } catch (IllegalAccessException ex) {
            } catch (IllegalArgumentException ex) {
            } catch (InvocationTargetException ex) {
            }
        }

        return null;
    }
}
