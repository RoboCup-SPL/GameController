package common.net.logging;

import common.Log;
import common.net.SPLTeamMessagePackage;
import common.net.GameControlReturnDataPackage;
import data.GameControlData;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Singleton class for logging received messages.
 *
 * @author Felix Thielke
 */
public class Logger {

    private static final String LOG_DIRECTORY = "logs_teamcomm";

    private static final Logger instance = new Logger();

    private File logFile;
    private ObjectOutputStream logger;
    private long beginTimestamp;
    private boolean enabled = true;
    private boolean isReplaying = false;

    private Logger() {
        createLogfile();
    }

    /**
     * Returns the only instance of the logger.
     *
     * @return instance
     */
    public static Logger getInstance() {
        return instance;
    }

    public final void enableLogging() {
        if (!enabled) {
            enabled = true;
            createLogfile();
            System.out.println("Enabled logging of SPLTeamMessages");
        }
    }

    public final void disableLogging() {
        if (enabled) {
            enabled = false;
            closeLogfile();
            System.out.println("Disabled logging of SPLTeamMessages");
        }
    }

    public final void setIsReplaying(boolean replaying) {
        isReplaying = replaying;
    }

    /**
     * Creates a new log file to store received messages in.
     */
    public final void createLogfile() {
        createLogfile(null);
    }

    /**
     * Creates a new log file to store received messages in.
     *
     * @param name string appended to the name of the new log file
     */
    public final void createLogfile(final String name) {
        if (enabled && !isReplaying) {
            // Close current log file
            closeLogfile();

            // Determine file name
            final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-S");
            final String fileName = "teamcomm_" + df.format(new Date(System.currentTimeMillis())) + (name == null || name.isEmpty() ? "" : ("_" + name)) + ".log";

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
        if (!isReplaying) {
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

    /**
     * Logs the given SPLTeamMessage package.
     *
     * @param p package
     */
    public void log(final SPLTeamMessagePackage p) {
        log(SPLTeamMessagePackage.class, p);
    }

    /**
     * Logs the given GameControlReturnData package.
     *
     * @param p package
     */
    public void log(final GameControlReturnDataPackage p) {
        log(GameControlReturnDataPackage.class, p);
    }

    /**
     * Logs the given message from the GameController.
     *
     * @param p game control data
     */
    public void log(final GameControlData p) {
        log(GameControlData.class, p);
    }

    /**
     * Logs the given package.
     *
     * @param <T> type of the package
     * @param cls class of the package
     * @param p package
     */
    public <T extends Serializable> void log(final Class<T> cls, final T p) {
        if (enabled && !isReplaying) {
            boolean error = false;
            synchronized (this) {
                if (logFile != null) {
                    // Open stream if needed
                    if (logger == null) {
                        try {
                            logger = new ObjectOutputStream(Files.newOutputStream(logFile.toPath()));
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

    /**
     * Returns the unique log ID for the given class.
     *
     * @param cls Class
     * @return id
     */
    public static int getIDForClass(final Class<?> cls) {
        if (cls.equals(GameControlData.class)) {
            return 1;
        }

        return -1;
    }
}
