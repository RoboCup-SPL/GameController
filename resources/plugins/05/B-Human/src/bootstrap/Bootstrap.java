package bootstrap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Small program for automatically generating the MessageID enum of the plugin
 * by reading the MessageID enum of the B-Human repository. The path to the
 * repository must be stored in a file whose name is given in CONFIG_FILE.
 *
 * @author Felix Thielke
 */
public class Bootstrap {

    private static final String CONFIG_FILE = "bhumanpath.cfg";

    private static final String MESSAGEIDS_H = "Tools/MessageQueue/MessageIDs.h";

    /**
     * Main method of the program.
     *
     * @param args command line arguments (ignored)
     */
    public static void main(final String[] args) {
        final File bhumanPath;
        try {
            bhumanPath = new File(new BufferedReader(new FileReader(CONFIG_FILE)).readLine());
        } catch (final FileNotFoundException ex) {
            System.out.println(CONFIG_FILE + " not found. MessageIDs will not be generated.");
            return;
        } catch (final IOException ex) {
            System.out.println("B-Human source path could not be read from " + CONFIG_FILE + ". MessageIDs will not be generated.");
            return;
        }

        try {
            generateMessageIDs(bhumanPath);
        } catch (IOException ex) {
            System.err.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    private static void generateMessageIDs(final File bhumanPath) throws IOException {
        try (final BufferedReader messageIDs_h = new BufferedReader(new FileReader(new File(bhumanPath, MESSAGEIDS_H)));
                final BufferedWriter messageID_java = new BufferedWriter(new FileWriter("src/bhuman/message/MessageID.java"))) {
            while (!messageIDs_h.readLine().trim().startsWith("ENUM(MessageID,")) {
            }
            messageID_java.write(
                    "package bhuman.message;\n"
                    + "\n"
                    + "/**\n"
                    + " * Enum containing all MessageIDs used for serialization.\n"
                    + " */\n"
                    + "public enum MessageID {\n"
                    + "\n"
            );
            String line = messageIDs_h.readLine().trim();
            while (!line.startsWith("});")) {
                if (!line.isEmpty() && !line.startsWith("//") && !line.startsWith("numOf")) {
                    final String[] split = line.split("=", 2);
                    if (split.length == 2) {
                        split[0] = split[0].trim() + split[1].substring(Math.max(0, split[1].indexOf(','))).trim();
                    }
                    if (!split[0].startsWith("{")) {
                        messageID_java.write("    " + split[0] + "\n");
                    }
                }

                line = messageIDs_h.readLine().trim();
            }
            messageID_java.write("}\n");
            System.out.println("MessageIDs updated");
        }
    }
}
