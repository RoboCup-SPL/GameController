package teamcomm.net;

import common.Log;
import data.SPLStandardMessage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.JOptionPane;
import teamcomm.PluginLoader;
import teamcomm.data.RobotData;
import teamcomm.data.AdvancedMessage;

/**
 * Singleton class for the thread which handles messages from the robots. It
 * spawns one thread for listening on each team port up to team number 100 and
 * processes the messages received by these threads. It also handles logging and
 * replaying of log files.
 *
 * @author Felix Thielke
 */
public class SPLStandardMessageReceiver extends Thread {

    /**
     * Private class for a message. Instances of this class are stored in the
     * log files.
     */
    private static class NetPackage implements Serializable {

        private static final long serialVersionUID = 758311663011901849L;

        public final long timestamp;
        public final String host;
        public final int team;
        public final byte[] message;

        public NetPackage(final String host, final int team, final byte[] message) {
            this.host = host;
            this.team = team;
            this.message = message;
            this.timestamp = System.currentTimeMillis() - beginTimestamp;
        }
    }

    private class ReceiverThread extends Thread {

        private final MulticastSocket socket;
        private final int team;

        public ReceiverThread(final int team) throws UnknownHostException, IOException {
            setName("SPLStandardMessageReceiver_team" + team);

            this.team = team;

            // Bind socket to team port
            socket = new MulticastSocket(null);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress("0.0.0.0", getTeamport(team)));

            try {
                // Join multicast group on all network interfaces (for compatibility with SimRobot)
                socket.joinGroup(InetAddress.getByName("239.0.0.1"));
                final byte[] localaddr = InetAddress.getLocalHost().getAddress();
                localaddr[0] = (byte) 239;
                socket.joinGroup(InetAddress.getByAddress(localaddr));
                final Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
                while (nis.hasMoreElements()) {
                    final NetworkInterface ni = nis.nextElement();
                    final Enumeration<InetAddress> addrs = ni.getInetAddresses();
                    while (addrs.hasMoreElements()) {
                        final byte[] addr = addrs.nextElement().getAddress();
                        addr[0] = (byte) 239;
                        socket.joinGroup(InetAddress.getByAddress(addr));
                    }
                }
            } catch (SocketException ex) {
                // Ignore, because this is only for testing and does not work everywhere
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[SPLStandardMessage.SIZE];
            while (!isInterrupted()) {
                try {
                    final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    if (logReplayThread == null) {
                        if (packet.getAddress().getAddress()[0] != 10) {
                            queue.add(new NetPackage("10.0." + team + "." + buffer[5], team, buffer));
                        } else {
                            queue.add(new NetPackage(packet.getAddress().getHostAddress(), team, buffer));
                        }
                    }

                    buffer = new byte[SPLStandardMessage.SIZE];
                } catch (SocketTimeoutException e) {
                } catch (IOException e) {
                    Log.error("something went wrong while receiving the message packages: " + e.getMessage());
                }
            }

        }
    }

    private class LogReplayThread extends Thread {

        private final ObjectInputStream replayLog;
        private final BufferedInputStream bufStream;
        private long pausedTimestamp = 0;
        private long pausedOffset = 0;

        public LogReplayThread(final File logfile) throws IOException {
            bufStream = new BufferedInputStream(new FileInputStream(logfile));
            replayLog = new ObjectInputStream(bufStream);
        }

        public void togglePause() {
            synchronized (replayLog) {
                if (pausedTimestamp == 0) {
                    pausedTimestamp = System.currentTimeMillis();
                } else {
                    pausedOffset += System.currentTimeMillis() - pausedTimestamp;
                    pausedTimestamp = 0;
                }
                replayLog.notifyAll();
            }
        }

        public boolean isPaused() {
            return pausedTimestamp > 0;
        }

        @Override
        public void run() {
            // Stop logging
            closeLogfile();

            try {
                final long startTimestamp = System.currentTimeMillis();

                bufStream.mark(10);
                if (bufStream.read() < 0) {
                    return;
                }
                bufStream.reset();

                queue.clear();
                Thread.sleep(100);
                RobotData.getInstance().reset();

                final NetPackage pFirst = (NetPackage) replayLog.readObject();
                pausedOffset -= pFirst.timestamp;
                queue.add(pFirst);

                while (!isInterrupted()) {
                    bufStream.mark(10);
                    if (bufStream.read() < 0) {
                        break;
                    }
                    bufStream.reset();

                    final NetPackage p = (NetPackage) replayLog.readObject();

                    while (true) {
                        while (isPaused()) {
                            synchronized (replayLog) {
                                replayLog.wait();
                            }
                        }

                        final long diff = p.timestamp - (System.currentTimeMillis() - (startTimestamp + pausedOffset));
                        if (diff <= 0) {
                            break;
                        }
                        Thread.sleep(diff);
                    }

                    queue.add(p);
                }
            } catch (IOException e) {
                Log.error("error while reading log file: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                Log.error("error while reading log file: " + e.getMessage());
            } catch (InterruptedException ex) {
            } finally {
                try {
                    bufStream.close();
                    replayLog.close();
                } catch (IOException e) {
                }

                queue.clear();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
                RobotData.getInstance().reset();

                createLogfile();

                logReplayThread = null;
            }
        }
    }

    private static final long beginTimestamp = System.currentTimeMillis();

    private static SPLStandardMessageReceiver instance;

    private static final String LOG_DIRECTORY = "logs_teamcomm";
    private static final int MAX_TEAMNUMBER = 100;

    private final ReceiverThread[] receivers = new ReceiverThread[MAX_TEAMNUMBER];
    private final LinkedBlockingQueue<NetPackage> queue = new LinkedBlockingQueue<NetPackage>();
    private File logFile;
    private ObjectOutputStream logger;

    private LogReplayThread logReplayThread;

    /**
     * Constructor.
     *
     * @throws IOException if a problem occurs while creating the receiver
     * threads
     */
    public SPLStandardMessageReceiver() throws IOException {
        // Create log file
        createLogfile();

        // Create receiver threads
        for (int i = 0; i < MAX_TEAMNUMBER; i++) {
            receivers[i] = new ReceiverThread(i + 1);
        }
    }

    /**
     * Returns the only instance of the SPLStandardMessageReceiver.
     *
     * @return instance
     */
    public static SPLStandardMessageReceiver getInstance() {
        if (instance == null) {
            try {
                instance = new SPLStandardMessageReceiver();
            } catch (SocketException ex) {
                JOptionPane.showMessageDialog(null,
                        "Error while setting up packet listeners.",
                        "SocketException",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null,
                        "Error while setting up packet listeners.",
                        "IOException",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }
        }
        return instance;
    }

    /**
     * Starts replaying the given log file.
     *
     * @param logfile path to the file
     * @throws FileNotFoundException if the file could not be found
     * @throws IOException if an error occured while reading the file
     */
    public void replayLog(final File logfile) throws FileNotFoundException, IOException {
        if (logReplayThread == null) {
            logReplayThread = new LogReplayThread(logfile);
            logReplayThread.start();
        }
    }

    /**
     * Stops replaying a log file.
     */
    public void stopReplaying() {
        if (logReplayThread != null) {
            logReplayThread.interrupt();
        }
    }

    /**
     * Pauses or unpauses the replaying of a log file.
     */
    public void toggleReplayPaused() {
        if (logReplayThread != null) {
            logReplayThread.togglePause();
        }
    }

    /**
     * Returns whether the replaying of a log file is paused.
     *
     * @return boolean
     */
    public boolean isReplayPaused() {
        return logReplayThread == null || logReplayThread.isPaused();

    }

    /**
     * Returns whether a log file is currently being replayed.
     *
     * @return boolean
     */
    public boolean isReplaying() {
        return logReplayThread != null;
    }

    /**
     * Creates a new log file to store received messages in.
     */
    public final void createLogfile() {
        // Close current log file
        closeLogfile();

        // Determine file path
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-S");
        final File logDir = new File(LOG_DIRECTORY);
        if (!logDir.exists() && !logDir.mkdirs()) {
            logFile = new File("teamcomm_" + df.format(new Date(System.currentTimeMillis())) + ".log");
        } else {
            logFile = new File(logDir, "teamcomm_" + df.format(new Date(System.currentTimeMillis())) + ".log");
        }
    }

    private void openLogfile() throws IOException {
        // Open stream
        if (logFile != null && logger == null) {
            logger = new ObjectOutputStream(new FileOutputStream(logFile));
        }
    }

    /**
     * Closes the currently used log file.
     */
    public void closeLogfile() {
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

    @Override
    public void run() {
        try {
            // Start receivers
            for (final ReceiverThread receiver : receivers) {
                receiver.start();
            }

            // Handle received packages
            while (!isInterrupted()) {
                final NetPackage p = queue.take();
                final SPLStandardMessage message;
                final Class<? extends SPLStandardMessage> c = PluginLoader.getInstance().getMessageClass(p.team);

                if (logFile != null) {
                    try {
                        openLogfile();
                        logger.writeObject(p);
                    } catch (IOException e) {
                        Log.error("something went wrong while opening logfile: " + e.getMessage());
                        closeLogfile();
                    }
                }

                try {
                    final boolean valid;
                    message = c.newInstance();
                    valid = message.fromByteArray(ByteBuffer.wrap(p.message));
                    if (message instanceof AdvancedMessage && valid) {
                        ((AdvancedMessage) message).init();
                    }

                    RobotData.getInstance().receiveMessage(p.host, valid ? message.teamNum : p.team, valid ? message : null);
                } catch (InstantiationException ex) {
                    Log.error("a problem occured while instantiating custom message class " + c.getSimpleName() + ": " + ex.getMessage());
                } catch (IllegalAccessException ex) {
                    Log.error("a problem occured while instantiating custom message class " + c.getSimpleName() + ": " + ex.getMessage());
                }
                Thread.yield();
            }
        } catch (InterruptedException ex) {
        } finally {
            closeLogfile();

            for (final ReceiverThread receiver : receivers) {
                receiver.interrupt();
            }

            try {
                for (final ReceiverThread receiver : receivers) {
                    receiver.join();
                }
            } catch (InterruptedException ex) {

            }
        }
    }

    private static int getTeamport(final int teamNumber) {
        return teamNumber + 10000;
    }
}
