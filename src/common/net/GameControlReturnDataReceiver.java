package common.net;

import common.Log;
import common.net.logging.Logger;
import data.GameControlReturnData;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author Marcel Steinbeck
 *
 * This class is used to receive a packet send by a robot on port
 * {@link GameControlReturnData#GAMECONTROLLER_RETURNDATA_PORT} via UDP over
 * broadcast.
 */
public class GameControlReturnDataReceiver extends Thread {

    private class ReceiverThread extends Thread {

        /**
         * The used socket to receive the packages.
         */
        private final DatagramSocket datagramSocket;

        /**
         * Whether this receiver receives messages forwarded by the GameController
         * (instead of directly from the robots).
         */
        private final boolean forwarded;

        /**
         * Creates a new Receiver.
         *
         * @param address the InetAddress to listen on. If null, listens on any address.
         * @param forwarded whether this thread should received forwarded packets (from GC) instead of the original ones
         * @throws SocketException if an error occurs while creating the socket
         */
        public ReceiverThread(final InetAddress address, boolean forwarded) throws SocketException {
            setName("GameControlReturnDataReceiver");

            this.forwarded = forwarded;

            final int port = forwarded ? GameControlReturnData.GAMECONTROLLER_RETURNDATA_FORWARD_PORT : GameControlReturnData.GAMECONTROLLER_RETURNDATA_PORT;

            datagramSocket = new DatagramSocket(null);
            datagramSocket.setReuseAddress(true);
            datagramSocket.setSoTimeout(500);
            datagramSocket.bind(address != null
                    ? new InetSocketAddress(address, port)
                    : new InetSocketAddress(port));
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                final byte[] buffer = new byte[GameControlReturnData.SIZE + (forwarded ? 4 : 0)];
                final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                try {
                    datagramSocket.receive(packet);

                    if (processPackets()) {
                        if (forwarded) {
                            final String originalHost = InetAddress.getByAddress(Arrays.copyOfRange(buffer, 0, 4)).getHostAddress();
                            queue.add(new GameControlReturnDataPackage(originalHost, Arrays.copyOfRange(buffer, 4, buffer.length)));
                        } else if (generateIP) {
                            queue.add(new GameControlReturnDataPackage("10.0." + (int) buffer[6] + "." + (int) buffer[5],
                                    buffer));
                        } else {
                            queue.add(new GameControlReturnDataPackage(packet.getAddress().getHostAddress(), buffer));
                        }
                    }
                } catch (SocketTimeoutException e) { // ignore, because we set a timeout
                } catch (IOException e) {
                    Log.error("something went wrong while receiving : " + e.getMessage());
                }
            }

            datagramSocket.close();
        }
    }

    private final ReceiverThread receiver;
    private final LinkedBlockingQueue<GameControlReturnDataPackage> queue = new LinkedBlockingQueue<>();
    private final boolean generateIP;

    /**
     * Creates a new Receiver.
     *
     * @param address the InetAddress to listen on. If null, listens on any address.
     * @param generateIP Generate a sender IP based on data in return packet.
     * @throws SocketException if an error occurs while creating the socket
     */
    public GameControlReturnDataReceiver(final InetAddress address, boolean generateIP) throws SocketException {
        // Create receiver thread
        receiver = new ReceiverThread(address, false);
        this.generateIP = generateIP;
    }

    /**
     * Creates a new Receiver for forwarded messages.
     *
     * @throws SocketException if an error occurs while creating the socket
     */
    public GameControlReturnDataReceiver() throws SocketException {
        // Create receiver thread
        receiver = new ReceiverThread(null, true);
        generateIP = false;
    }

    protected boolean processPackets() {
        return true;
    }

    protected void handleMessage(final GameControlReturnDataPackage p) {

    }

    @Override
    public void run() {
        try {
            // Start receiver
            receiver.start();

            // Handle received packages
            while (!isInterrupted()) {
                final GameControlReturnDataPackage p = queue.take();

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
    public void addToPackageQueue(final GameControlReturnDataPackage p) {
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
}
