package bhuman.message;

import bhuman.message.data.ComplexStreamReader;
import bhuman.message.data.SimpleStreamReader;
import common.Log;
import data.SPLStandardMessage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import util.Unsigned;

/**
 * Class for reading from the B-Human MessageQueue.
 *
 * @author Felix Thielke
 */
public class MessageQueue {

    private static final String CONFIG_FILE = "resources/bhumanpath.cfg";
    private static final String MESSAGEIDS_H = "Tools/MessageQueue/MessageIDs.h";
    private static final String MESSAGEIDS_H_PLUGIN = "resources/MessageIDs.h";

    private final String robotIdentifier;

    private final long usedSize;
    private final int numberOfMessages;

    private static final Map<String, Map<Class<?>, Message<? extends Message>>> cachedMessages = new HashMap<>();

    private static final Map<Short, List<Class<? extends Message>>> classes = new HashMap<>();
    private static final Map<Short, List<String>> messageIds = new HashMap<>();

    private final Map<Class<?>, Message<? extends Message>> messages = new HashMap<>();

    private final Set<String> messageNames = new TreeSet<>();

    /**
     * Constructor.
     *
     * @param origin SPLStandardMessage containing this MessageQueue
     * @param buf raw data of the transmitted MessageQueue
     */
    public MessageQueue(final SPLStandardMessage origin, final ByteBuffer buf) {
        robotIdentifier = Unsigned.toUnsigned(origin.teamNum) + "," + origin.playerNum;

        // read header
        usedSize = Unsigned.toUnsigned(buf.getInt());
        numberOfMessages = buf.getInt();

        // read messages
        final short teamNumber = Unsigned.toUnsigned(origin.teamNum);
        if (!messageIds.containsKey(teamNumber)) {
            messageIds.put(teamNumber, parseMessageIDs(teamNumber));
        }
        if (!classes.containsKey(teamNumber)) {
            classes.put(teamNumber, generateClasses(messageIds.get(teamNumber)));
        }
        while (buf.hasRemaining()) {
            final short idIndex = Unsigned.toUnsigned(buf.get());
            final int size = Unsigned.toUnsigned(buf.get()) | (Unsigned.toUnsigned(buf.get()) << 8) | (Unsigned.toUnsigned(buf.get()) << 16);
            final byte[] data = new byte[size];
            buf.get(data);
            messageNames.add(messageIds.get(teamNumber).get(idIndex));
            final Message<? extends Message> msg = createMessage(idIndex, messageIds.get(teamNumber).get(idIndex), classes.get(teamNumber), ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN));
            if (msg != null) {
                messages.put(msg.getClass(), msg);
                getGenericCachedMessage(msg.getClass());
            }
        }
    }

    public Set<String> getMessageNames() {
        return messageNames;
    }

    /**
     * Returns the size of the MessageQueue in bytes.
     *
     * @return size of the MessageQueue in bytes
     */
    public long getUsedSize() {
        return usedSize;
    }

    /**
     * Returns the number of messages in the MessageQueue.
     *
     * @return number of messages
     */
    public int getNumberOfMessages() {
        return numberOfMessages;
    }

    /**
     * Gets a message of the given class from the MessageQueue.
     *
     * @param <T> class type of the message
     * @param cls class of the message
     * @return message or null if there is none
     */
    public <T extends Message<T>> T getMessage(final Class<T> cls) {
        return cls.cast(messages.get(cls));
    }

    /**
     * Gets a message of the given class from the MessageQueue.
     *
     * @param cls class of the message
     * @return message or null if there is none
     */
    public Message getGenericMessage(final Class<? extends Message> cls) {
        return messages.get(cls);
    }

    /**
     * Gets an optionally cached message of the given class from the
     * MessageQueue. If no compatible message is found in the MessageQueue, the
     * last compatible message that was returned by a call to this method on
     * another MessageQueue sent by the same robot is returned.
     *
     * @param <T> class type of the message
     * @param cls class of the message
     * @return message or null if there is none
     */
    public <T extends Message<T>> T getCachedMessage(final Class<T> cls) {
        Map<Class<?>, Message<? extends Message>> cache = cachedMessages.get(robotIdentifier);
        if (cache == null) {
            cache = new HashMap<>();
            cachedMessages.put(robotIdentifier, cache);
        }

        T message = cls.cast(messages.get(cls));
        if (message != null) {
            cache.put(cls, message);
        } else {
            message = cls.cast(cache.get(cls));
        }

        return message;
    }

    /**
     * Gets a message of the given class from the MessageQueue.
     *
     * @param cls class of the message
     * @return message or null if there is none
     */
    public Message getGenericCachedMessage(final Class<? extends Message> cls) {
        Map<Class<?>, Message<? extends Message>> cache = cachedMessages.get(robotIdentifier);
        if (cache == null) {
            cache = new HashMap<>();
            cachedMessages.put(robotIdentifier, cache);
        }

        Message<? extends Message> message = messages.get(cls);
        if (message != null) {
            cache.put(cls, message);
        } else {
            message = cache.get(cls);
        }

        return message;
    }

    private static List<Class<? extends Message>> generateClasses(final List<String> msgIds) {
        final List<Class<? extends Message>> teamClasses = new ArrayList<>(256);
        for (int i = 0; i < 256; i++) {
            teamClasses.add(null);
        }
        for (int i = 0; i < msgIds.size(); i++) {
            try {
                final Class<? extends Message> cls = Class.forName("bhuman.message.messages." + msgIds.get(i)).asSubclass(Message.class);

                if (Message.class.isAssignableFrom(cls)) {
                    teamClasses.set(i, cls);
                }
            } catch (ClassNotFoundException ex) {
            }
        }
        return teamClasses;
    }

    private static List<String> parseMessageIDs(final short teamNumber) {
        final List<String> msgIds = new ArrayList<>();
        File messageIDsPath;
        try {
            messageIDsPath = new File(new BufferedReader(new FileReader("plugins/" + (teamNumber < 10 ? "0" + teamNumber : String.valueOf(teamNumber)) + "/" + CONFIG_FILE)).readLine(), MESSAGEIDS_H);
        } catch (final FileNotFoundException ex) {
            // Config file not found: use MessageIDs.h from the plugin dir
            messageIDsPath = new File("plugins/" + (teamNumber < 10 ? "0" + teamNumber : String.valueOf(teamNumber)) + "/" + MESSAGEIDS_H_PLUGIN);
        } catch (final IOException ex) {
            Log.error("B-Human source path could not be read from " + CONFIG_FILE + ".");
            messageIDsPath = new File("plugins/" + (teamNumber < 10 ? "0" + teamNumber : String.valueOf(teamNumber)) + "/" + MESSAGEIDS_H_PLUGIN);
        }

        try (final BufferedReader messageIDs_h = new BufferedReader(new FileReader(messageIDsPath))) {
            while (!messageIDs_h.readLine().trim().startsWith("ENUM(MessageID,")) {
            }
            String line = messageIDs_h.readLine().trim();
            while (!line.startsWith("});")) {
                if (!line.isEmpty() && !line.startsWith("//") && !line.startsWith("numOf")) {
                    final String[] split = line.split("=", 2);
                    final char start = split[0].charAt(0);
                    if (start != '{' && start != ',') {
                        msgIds.add(split[0].split(",", 2)[0].substring(2));
                    }
                }

                line = messageIDs_h.readLine().trim();
            }
        } catch (IOException | NullPointerException ex) {
            Log.error("Error while parsing MessageIDs from file " + messageIDsPath + ": " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }

        return msgIds;
    }

    /**
     * Creates the appropriate message for the given MessageID and raw data.
     *
     * @param messageId identifier of the message
     * @param teamNumber team number of the message: used for locating the
     * plugin dir
     * @param data raw data of the message
     * @return message or null if an error occurred
     */
    private Message<? extends Message> createMessage(final short messageId, final String messageName, final List<Class<? extends Message>> classes, final ByteBuffer data) {
        // Instantiate the message class that fits the MessageID
        final Class<? extends Message> cls = classes.get(messageId);
        if (cls != null) {
            try {
                final Message cachedInst = getGenericCachedMessage(cls);
                final Message inst = cachedInst != null ? cachedInst : cls.newInstance();
                final int streamedSize = SimpleStreamReader.class.isInstance(inst) ? SimpleStreamReader.class.cast(inst).getStreamedSize() : ComplexStreamReader.class.cast(inst).getStreamedSize(data);
                if (data.remaining() != streamedSize && streamedSize != -1) {
                    Log.error("Wrong size of " + messageName + " message: expected " + streamedSize + ", was " + data.remaining());
                    return null;
                }
                return (Message<? extends Message>) inst.read(data);
            } catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException ex) {
                Log.error(ex.toString());
            }
        }

        return null;
    }
}
