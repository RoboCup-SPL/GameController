package common.net;

import common.Log;
import common.net.logging.Logger;
import data.SPLTeamMessage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class for a thread which handles messages from the robots. It spawns another
 * thread for listening on team ports up to team number 100 and processes the
 * messages received by that thread.
 *
 * @author Felix Thielke
 */
public class SPLTeamMessageReceiver extends Thread {

    private class ReceiverThread extends Thread {

        private final AbstractSelector selector;
        private final boolean multicast;
        private final int[] teams;
        private int openChannels = 0;

        public ReceiverThread(final boolean multicast, final int[] teams) throws IOException {
            setName("SPLTeamMessageReceiver");

            selector = SelectorProvider.provider().openSelector();
            this.multicast = multicast;
            this.teams = teams;
        }

        private void openChannel(final int team) throws IOException {
            // Bind channel to team port
            final DatagramChannel channel = SelectorProvider.provider().openDatagramChannel(StandardProtocolFamily.INET);
            channel.configureBlocking(false);
            channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            channel.bind(new InetSocketAddress("0.0.0.0", getTeamport(team)));
            if (multicast) {
                try (final MulticastSocket ms = new MulticastSocket()) {
                    // Join multicast group on all network interfaces (for compatibility with SimRobot)
                    final Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
                    final byte[] localaddr = InetAddress.getLocalHost().getAddress();
                    localaddr[0] = (byte) 239;

                    final NetworkInterface defNI = ms.getNetworkInterface();
                    channel.setOption(StandardSocketOptions.IP_MULTICAST_IF, defNI);
                    channel.join(InetAddress.getByName("239.0.0.1"), defNI);
                    channel.join(InetAddress.getByAddress(localaddr), defNI);

                    while (nis.hasMoreElements()) {
                        final NetworkInterface ni = nis.nextElement();
                        channel.join(InetAddress.getByName("239.0.0.1"), ni);
                        channel.join(InetAddress.getByAddress(localaddr), ni);
                    }
                } catch (IOException ex) {
                    // Ignore, because this is only for testing and does not work everywhere
                }
            }

            // Register channel with selector
            channel.register(selector, SelectionKey.OP_READ, team);
        }

        @Override
        public void run() {
            // Receive one more byte than the maximum length to detect packets that are too long.
            ByteBuffer buffer = ByteBuffer.allocate(SPLTeamMessage.MAX_SIZE + 1);
            while (!isInterrupted()) {
                try {
                    if (selector.select(openChannels < (teams == null ? MAX_TEAMNUMBER : teams.length) ? 50 : 500) > 0) {
                        final Iterator<SelectionKey> it = selector.selectedKeys().iterator();

                        while (it.hasNext()) {
                            final SelectionKey key = it.next();
                            final int team = (int) key.attachment();
                            final DatagramChannel channel = (DatagramChannel) key.channel();
                            final InetSocketAddress address = (InetSocketAddress) channel.receive(buffer);

                            if (address != null && processPackets()) {
                                byte[] data = new byte[buffer.position()];
                                buffer.rewind();
                                buffer.get(data, 0, data.length);
                                if (multicast) {
                                    // This works only for teams who have the player number in the second byte of the team message.
                                    queue.add(new SPLTeamMessagePackage("10.0." + team + "." + (buffer.get(1) & 15), team, data));
                                } else {
                                    queue.add(new SPLTeamMessagePackage(address.getAddress().getHostAddress(), team, data));
                                }
                            }

                            buffer = ByteBuffer.allocate(SPLTeamMessage.MAX_SIZE + 1);
                            it.remove();
                        }
                    }

                    if (openChannels < (teams == null ? MAX_TEAMNUMBER : teams.length)) {
                        try {
                            openChannel(teams == null ? (openChannels + 1) : teams[openChannels]);
                        } catch (IOException e) {
                            Log.error("could not open UDP socket for team " + (teams == null ? (openChannels + 1) : teams[openChannels]) + ": " + e.getMessage());
                        } finally {
                            openChannels++;
                        }
                    }
                } catch (SocketTimeoutException e) {
                } catch (IOException e) {
                    Log.error("something went wrong while receiving the message packages: " + e.getMessage());
                }
            }

        }
    }

    private static final int MAX_TEAMNUMBER = 100;

    private final ReceiverThread receiver;
    private final LinkedBlockingQueue<SPLTeamMessagePackage> queue = new LinkedBlockingQueue<>();

    /**
     * Constructor.
     *
     * @param multicast Also open multicast ports
     * @param teams Optional list of team numbers to watch. If null, all 100 ports are watched.
     * @throws IOException if a problem occurs while creating the receiver
     * threads
     */
    public SPLTeamMessageReceiver(final boolean multicast, final int[] teams) throws IOException {
        // Create receiver thread
        receiver = new ReceiverThread(multicast, teams);
    }

    protected boolean processPackets() {
        return true;
    }

    protected void handleMessage(final SPLTeamMessagePackage p) {

    }

    @Override
    public void run() {
        try {
            // Start receiver
            receiver.start();

            // Handle received packages
            while (!isInterrupted()) {
                final SPLTeamMessagePackage p = queue.take();

                // Log package
                Logger.getInstance().log(p);

                // Handle message
                handleMessage(p);
                Thread.yield();
            }
        } catch (InterruptedException ex) {
        } finally {
            receiver.interrupt();

            try {
                receiver.join();
            } catch (InterruptedException ex) {

            }
        }
    }

    /**
     * Adds the given package to the queue in order to be processed.
     *
     * @param p package
     */
    public void addToPackageQueue(final SPLTeamMessagePackage p) {
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
