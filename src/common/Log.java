package common;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author Michel Bartsch
 *
 * This class should be used to log into a log file. A new file will be created
 * every time the GameController is started.
 *
 * This class is a singleton!
 */
public class Log
{
    /** The instance of the singleton. */
    private static final Log instance = new Log();

    /** The error-file to write into. */
    private FileWriter errorFile;
    /** The file to write into. */
    private final String errorPath = "error.txt";

    /** The format of timestamps. */
    public static final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy.M.dd-kk.mm.ss");

    /**
     * Creates a new Log.
     */
    private Log() {}

    /**
     * Writes a line, beginning with a timestamp, in the error-file and creates
     * a new one, if it does not yet exist.
     *
     * This can be used before initialising the log!
     *
     * @param s     The string to be written in the error-file.
     */
    public static void error(String s)
    {
        System.err.println(s);
        try {
            if (instance.errorFile == null) {
                instance.errorFile = new FileWriter(instance.errorPath);
            }
            instance.errorFile.write(timestampFormat.format(new Date(System.currentTimeMillis()))+": "+s+"\n");
            instance.errorFile.flush();
        } catch (IOException e) {
             System.err.println("cannot write to error file!");
        }
    }

    /**
     * Closes the Log
     *
     * @throws IOException if an error occurred while trying to close the FileWriters
     */
    public static void close() throws IOException {
        if (instance.errorFile != null) {
            instance.errorFile.close();
        }
    }
}
