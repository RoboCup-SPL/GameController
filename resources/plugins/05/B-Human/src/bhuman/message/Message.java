package bhuman.message;

import bhuman.message.data.SimpleStreamReader;
import bhuman.message.data.StreamReader;
import common.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Abstract base class for messages read from the message queue.
 *
 * @author Felix Thielke
 * @param <T> type of the implementing subclass
 */
public abstract class Message<T extends Message> implements StreamReader<T> {

    private static final String CONFIG_FILE = "resources/bhumanpath.cfg";
    private static final String MESSAGEIDS_H = "Tools/MessageQueue/MessageIDs.h";
    private static final String MESSAGEIDS_H_PLUGIN = "resources/MessageIDs.h";

    private static final ArrayList<Class<? extends Message>> classes = new ArrayList<>(256);

    /**
     * Creates the appropriate message for the given MessageID and raw data.
     *
     * @param messageId identifier of the message
     * @param teamNumber team number of the message: used for locating the
     * plugin dir
     * @param data raw data of the message
     * @return message or null if an error occurred
     */
    public static Message<? extends Message> factory(final short messageId, final short teamNumber, final ByteBuffer data) {
        // Accumulate implemented message classes
        if (classes.isEmpty()) {
            for (int i = 0; i < 256; i++) {
                classes.add(null);
            }
            final ArrayList<String> messageIds = parseMessageIDs(teamNumber);
            for (int i = 0; i < messageIds.size(); i++) {
                try {
                    final Class<? extends Message> cls = Class.forName("bhuman.message.messages." + messageIds.get(i)).asSubclass(Message.class);

                    if (Message.class.isAssignableFrom(cls)) {
                        classes.set(i, cls);
                    }
                } catch (ClassNotFoundException ex) {
                }
            }
        }

        // Instantiate the message class that fits the MessageID
        final Class<? extends Message> cls = classes.get(messageId);
        if (cls != null) {
            try {
                final Message inst = cls.newInstance();
                if (SimpleStreamReader.class.isInstance(inst)) {
                    final int streamedSize = ((SimpleStreamReader) inst).getStreamedSize();
                    if (data.remaining() != streamedSize && streamedSize != -1) {
                        return null;
                    }
                }
                return (Message<? extends Message>) inst.read(data);
            } catch (SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException ex) {
            }
        }

        return null;
    }

    /**
     * Parses the MessageID enum from a MessageIDs.h file as used in the B-Human
     * repository into a list of strings. The path to the repository must be
     * stored in a file whose name is given in CONFIG_FILE; if it does not exist
     * the MessageIDs.h in the directory of the plugin is used.
     *
     * @param teamNumber team number of the message: used for locating the
     * plugin dir
     * @return names of MessageIDs
     */
    private static ArrayList<String> parseMessageIDs(final short teamNumber) {
        ArrayList<String> messageIDs = new ArrayList<>();
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
                        messageIDs.add(split[0].split(",", 2)[0].substring(2));
                    }
                }

                line = messageIDs_h.readLine().trim();
            }
        } catch (IOException ex) {
            Log.error("Error while parsing MessageIDs: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }

        return messageIDs;
    }
}
