package teamcomm.net;

import common.Log;
import data.spl.SPLStandardMessage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.concurrent.LinkedBlockingQueue;
import teamcomm.net.logging.Logger;

/**
 * Class for a thread which handles messages from the robots. It spawns one
 * thread for listening on each team port up to team number 100 and processes
 * the messages received by these threads.
 *
 * @author Felix Thielke
 */
public class SPLStandardMessageReceiver extends Thread {

    private class ReceiverThread extends Thread {

        private final MulticastSocket socket;
        private final int team;

        public ReceiverThread(final int team) throws IOException {
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

                    if (processPackets()) {
                        if (packet.getAddress().getAddress()[0] != 10) {
                            queue.add(new SPLStandardMessagePackage("10.0." + team + "." + buffer[5], team, buffer));
                        } else {
                            queue.add(new SPLStandardMessagePackage(packet.getAddress().getHostAddress(), team, buffer));
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

    private static final int MAX_TEAMNUMBER = 100;

    private final ReceiverThread[] receivers = new ReceiverThread[MAX_TEAMNUMBER];
    private final LinkedBlockingQueue<SPLStandardMessagePackage> queue = new LinkedBlockingQueue<>();

    /**
     * Constructor.
     *
     * @throws IOException if a problem occurs while creating the receiver
     * threads
     */
    public SPLStandardMessageReceiver() throws IOException {
        // Create receiver threads
        for (int i = 0; i < MAX_TEAMNUMBER; i++) {
            receivers[i] = new ReceiverThread(i + 1);
        }
    }

    protected boolean processPackets() {
        return true;
    }

    protected void handleMessage(final SPLStandardMessagePackage p) {
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
                final SPLStandardMessagePackage p = queue.take();

                // Log package
                Logger.getInstance().log(p);

                // Handle message
                handleMessage(p);
                Thread.yield();
            }
        } catch (InterruptedException ex) {
        } finally {
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

    /**
     * Adds the given package to the queue in order to be processed.
     *
     * @param p package
     */
    public void addToPackageQueue(final SPLStandardMessagePackage p) {
        queue.add(p);
    }

    /**
     * Removes all pending packages from the queue.
     */
    public void clearPackageQueue() {
        queue.clear();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
        }
    }

    private static int getTeamport(final int teamNumber) {
        return teamNumber + 10000;
    }
}
