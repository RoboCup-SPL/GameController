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
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import teamcomm.data.RobotData;

/**
 *
 * @author Felix Thielke
 */
public class SPLStandardMessageReceiver extends Thread {

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
            this.team = team;

            // Bind socket to team port
            socket = new MulticastSocket(null);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(getTeamport(team)));

            // Join multicast group (for compatibility with SimRobot)
            final byte[] addr = localhost.getAddress();
            addr[0] = (byte) 239;
            final InetAddress address = InetAddress.getByAddress(addr);
            socket.joinGroup(address);
        }

        @Override
        public void run() {
            byte[] buffer = new byte[SPLStandardMessage.SIZE];
            while (!isInterrupted()) {
                try {
                    final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    if (logReplayThread == null) {
                        if (packet.getAddress().equals(localhost) || packet.getAddress().getAddress()[0] != 10) {
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
                pausedOffset += pFirst.timestamp;
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

                        final long diff = p.timestamp - (System.currentTimeMillis() - (startTimestamp - pausedOffset));
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

                logReplayThread = null;
            }
        }
    }

    private static final long beginTimestamp = System.currentTimeMillis();
    private final InetAddress localhost;

    private static SPLStandardMessageReceiver instance;

    private static final int MAX_TEAMNUMBER = 50;

    private final ReceiverThread[] receivers = new ReceiverThread[MAX_TEAMNUMBER];
    private final LinkedBlockingQueue<NetPackage> queue = new LinkedBlockingQueue<NetPackage>();
    private final ObjectOutputStream logger;

    private LogReplayThread logReplayThread;

    public SPLStandardMessageReceiver() throws IOException, SocketException {
        this(null);
    }

    public SPLStandardMessageReceiver(final File logfile) throws IOException, SocketException {
        if (logfile == null) {
            logger = null;
        } else {
            logger = new ObjectOutputStream(new FileOutputStream(logfile));
        }

        // Determine local IP address
        InetAddress addr;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            addr = InetAddress.getByName("127.0.0.1");
        }
        this.localhost = addr;

        // Create receiver threads
        for (int i = 0; i < MAX_TEAMNUMBER; i++) {
            receivers[i] = new ReceiverThread(i + 1);
        }
    }

    public static SPLStandardMessageReceiver getInstance() {
        return instance;
    }

    public static void setInstance(final SPLStandardMessageReceiver instance) {
        SPLStandardMessageReceiver.instance = instance;
    }

    public void replayLog(final File logfile) throws FileNotFoundException, IOException {
        if (logReplayThread == null) {
            logReplayThread = new LogReplayThread(logfile);
            logReplayThread.start();
        }
    }

    public void stopReplaying() {
        if (logReplayThread != null) {
            logReplayThread.interrupt();
        }
    }

    public void toggleReplayPaused() {
        if (logReplayThread != null) {
            logReplayThread.togglePause();
        }
    }

    public boolean isReplayPaused() {
        return logReplayThread == null || logReplayThread.isPaused();

    }

    public boolean isReplaying() {
        return logReplayThread != null;
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
                final SPLStandardMessage message = new SPLStandardMessage();

                if (logger != null) {
                    try {
                        logger.writeObject(p);
                    } catch (IOException e) {
                        Log.error("something went wrong while logging message packages: " + e.getMessage());
                    }
                }

                RobotData.getInstance().receiveMessage(p.host, p.team, message.fromByteArray(ByteBuffer.wrap(p.message)) ? message : null);
            }
        } catch (InterruptedException ex) {
        } finally {
            if (logger != null) {
                try {
                    logger.close();
                } catch (IOException e) {
                    Log.error("something went wrong while closing logfile: " + e.getMessage());
                }
            }

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
