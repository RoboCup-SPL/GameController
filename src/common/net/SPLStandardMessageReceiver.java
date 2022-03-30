package common.net;

import common.Log;
import common.net.logging.Logger;
import data.SPLStandardMessage;
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
public class SPLStandardMessageReceiver extends Thread {

    private class ReceiverThread extends Thread {

        private final AbstractSelector selector;
        private final boolean multicast;
        private int openChannels = 0;

        public ReceiverThread(final boolean multicast) throws IOException {
            setName("SPLStandardMessageReceiver");

            selector = SelectorProvider.provider().openSelector();
            this.multicast = multicast;
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
            ByteBuffer buffer = ByteBuffer.allocate(SPLStandardMessage.SIZE);
            while (!isInterrupted()) {
                try {
                    if (selector.select(openChannels < MAX_TEAMNUMBER ? 50 : 500) > 0) {
                        final Iterator<SelectionKey> it = selector.selectedKeys().iterator();

                        while (it.hasNext()) {
                            final SelectionKey key = it.next();
                            final int team = (int) key.attachment();
                            final DatagramChannel channel = (DatagramChannel) key.channel();
                            final InetSocketAddress address = (InetSocketAddress) channel.receive(buffer);

                            if (address != null && processPackets()) {
                                if (multicast && address.getAddress().getAddress()[0] != 10) {
                                    queue.add(new SPLStandardMessagePackage("10.0." + team + "." + buffer.get(5), team, buffer.array()));
                                } else {
                                    queue.add(new SPLStandardMessagePackage(address.getAddress().getHostAddress(), team, buffer.array()));
                                }
                            }

                            buffer = ByteBuffer.allocate(SPLStandardMessage.SIZE);
                            it.remove();
                        }
                    }

                    if (openChannels < MAX_TEAMNUMBER) {
                        openChannels++;
                        try {
                            openChannel(openChannels);
                        } catch (IOException e) {
                            Log.error("could not open UDP socket for team " + openChannels + ": " + e.getMessage());
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
    private final LinkedBlockingQueue<SPLStandardMessagePackage> queue = new LinkedBlockingQueue<>();

    /**
     * Constructor.
     *
     * @param multicast Also open multicast ports
     * @throws IOException if a problem occurs while creating the receiver
     * threads
     */
    public SPLStandardMessageReceiver(final boolean multicast) throws IOException {
        // Create receiver thread
        receiver = new ReceiverThread(multicast);
    }

    protected boolean processPackets() {
        return true;
    }

    protected void handleMessage(final SPLStandardMessagePackage p) {

    }

    @Override
    public void run() {
        try {
            // Start receiver
            receiver.start();

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
