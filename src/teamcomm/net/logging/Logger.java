package teamcomm.net.logging;

import common.Log;
import data.GameControlData;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import teamcomm.net.SPLStandardMessagePackage;

/**
 *
 * @author Felix Thielke
 */
public class Logger {

    private static final String LOG_DIRECTORY = "logs_teamcomm";

    private static final Logger instance = new Logger();

    private File logFile;
    private ObjectOutputStream logger;
    private long beginTimestamp;

    private Logger() {
        createLogfile();
    }

    public static Logger getInstance() {
        return instance;
    }

    /**
     * Creates a new log file to store received messages in.
     */
    public final void createLogfile() {
        createLogfile(null);
    }

    /**
     * Creates a new log file to store received messages in.
     */
    public final void createLogfile(final String name) {
        if (!LogReplayer.isReplaying()) {
            // Close current log file
            closeLogfile();

            // Determine file name
            final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-S");
            final String fileName = "teamcomm_" + (name == null || name.isEmpty() ? "" : (name + "_")) + df.format(new Date(System.currentTimeMillis())) + ".log";

            // Determine file path
            final File logDir = new File(LOG_DIRECTORY);
            synchronized (this) {
                if (!logDir.exists() && !logDir.mkdirs()) {
                    logFile = new File(fileName);
                } else {
                    logFile = new File(logDir, fileName);
                }
            }
        }
    }

    /**
     * Closes the currently used log file.
     */
    public void closeLogfile() {
        if (!LogReplayer.isReplaying()) {
            synchronized (this) {
                if (logger != null) {
                    try {
                        logger.close();
                    } catch (IOException e) {
                        Log.error("something went wrong while closing logfile: " + e.getMessage());
                    }
                    logger = null;
                }
                if (logFile != null) {
                    if (logFile.exists() && logFile.length() <= 4) {
                        logFile.delete();
                    }
                    logFile = null;
                }
            }
        }
    }

    public void log(final SPLStandardMessagePackage p) {
        log(SPLStandardMessagePackage.class, p);
    }

    public void log(final GameControlData p) {
        log(GameControlData.class, p);
    }

    public <T extends Serializable> void log(final Class<T> cls, final T p) {
        if (!LogReplayer.isReplaying()) {
            boolean error = false;
            synchronized (this) {
                if (logFile != null) {
                    // Open stream if needed
                    if (logger == null) {
                        try {
                            logger = new ObjectOutputStream(new FileOutputStream(logFile));
                        } catch (IOException ex) {
                            Log.error("error while opening logfile: " + ex.getMessage());
                            error = true;
                        }
                        beginTimestamp = System.currentTimeMillis();
                    }

                    // Log object
                    try {
                        logger.writeLong(System.currentTimeMillis() - beginTimestamp);
                        logger.writeBoolean(p != null);
                        if (p == null) {
                            logger.writeInt(getIDForClass(cls));
                        } else {
                            logger.writeObject(p);
                        }
                    } catch (IOException ex) {
                        Log.error("error while writing to logfile: " + ex.getMessage());
                        error = true;
                    }
                }
            }

            if (error) {
                closeLogfile();
            }
        }
    }

    public static int getIDForClass(final Class<?> cls) {
        if (cls.equals(GameControlData.class)) {
            return 1;
        }

        return -1;
    }
}
