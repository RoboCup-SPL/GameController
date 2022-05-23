package controller.net;

import common.Log;
import common.net.GameControlReturnDataPackage;
import common.net.logging.Logger;
import data.AdvancedData;
import data.GameControlData;
import data.GameControlReturnData;
import data.TrueDataRequest;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

/**
 * A class which sends a GameControlData struct containing true data
 * (i.e. the actual state and time even after a change from Set to Playing in a
 * SPL game) to select receivers which requested this data by sending a
 * TrueDataRequest.
 *
 * @author Felix Thielke
 */
public class TrueDataSender extends Thread {

    private class TrueDataRequestReceiver extends Thread {

        private final DatagramSocket receiveSocket;

        public TrueDataRequestReceiver(final InetAddress address) throws SocketException {
            receiveSocket = new DatagramSocket(null);
            receiveSocket.setReuseAddress(true);
            receiveSocket.setSoTimeout(500);
            receiveSocket.bind(new InetSocketAddress(address, TrueDataRequest.GAMECONTROLLER_TRUEDATAREQUEST_PORT));
        }

        @Override
        public void run() {
            final ByteBuffer buffer = ByteBuffer.allocate(TrueDataRequest.TRUEDATAREQUEST_SIZE);
            final TrueDataRequest request = new TrueDataRequest();
            while (!isInterrupted()) {
                final DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.array().length);

                try {
                    receiveSocket.receive(packet);
                    buffer.rewind();
                    synchronized (whitelist) {
                        if (blacklist.contains(packet.getAddress())) {
                            Log.error("a request for true game data was received from robot " + packet.getAddress().getHostAddress());
                        } else if (request.fromByteArray(buffer)) {
                            whitelist.add(packet.getAddress());
                        }
                    }
                } catch (SocketTimeoutException e) { // ignore, because we set a timeout
                } catch (IOException e) {
                    Log.error("something went wrong while receiving a true game data request : " + e.getMessage());
                }
            }
            receiveSocket.close();
        }
    }

    private final TrueDataRequestReceiver requestReceiver;

    private final DatagramSocket sendSocket;

    private final Object dataMutex = new Object();
    private AdvancedData data;

    private final Set<InetAddress> whitelist = new HashSet<>();
    private final Set<InetAddress> blacklist = new HashSet<>();

    private byte packetNumber = 0;

    /**
     * Creates a new TrueDataSender.
     *
     * @param requestAddress the InetAddress on which to listen for
     * TrueDataRequests
     * @throws SocketException if an error occurs while creating the socket
     */
    public TrueDataSender(final InetAddress requestAddress) throws SocketException {
        sendSocket = new DatagramSocket();
        requestReceiver = new TrueDataRequestReceiver(requestAddress);
    }

    /**
     * Put the given InetAddress on the blacklist so it can never receive true
     * game state information.
     *
     * @param address InetAddress to put on blacklist
     */
    public void putOnBlacklist(final InetAddress address) {
        synchronized (whitelist) {
            blacklist.add(address);
            if (whitelist.contains(address)) {
                whitelist.remove(address);
                Log.error("a request for true game data was received from robot " + address.getHostAddress());
            }
        }
    }

    public void send(final AdvancedData data) {
        synchronized (dataMutex) {
            this.data = (AdvancedData) data.clone();
        }
    }

    public void handleGameControlReturnData(final GameControlReturnDataPackage data) {
        try {
            final InetAddress address = InetAddress.getByName(data.host);

            putOnBlacklist(address);

            final byte[] addressAsBytes = address.getAddress();

            if (addressAsBytes.length != 4) {
                return;
            }

            final byte[] message = new byte[addressAsBytes.length + data.message.length];
            System.arraycopy(addressAsBytes, 0, message, 0, addressAsBytes.length);
            System.arraycopy(data.message, 0, message, addressAsBytes.length, data.message.length);

            synchronized (whitelist) {
                for (final InetAddress receiver : whitelist) {
                    try {
                        sendSocket.send(new DatagramPacket(message, message.length, receiver, GameControlReturnData.GAMECONTROLLER_RETURNDATA_FORWARD_PORT));
                    } catch (IOException e) {
                        Log.error("Error while forwarding game controller return data");
                    }
                }
            }
        } catch (UnknownHostException e) {
        }
    }

    @Override
    public void run() {
        requestReceiver.start();
        while (!isInterrupted()) {
            if (data != null) {
                final byte[] arr;
                synchronized (dataMutex) {
                    data.updateTimes(true);
                    data.packetNumber = packetNumber++;
                    Logger.getInstance().log(data);
                    arr = data.getTrueDataAsByteArray().array();
                }

                synchronized (whitelist) {
                    for (final InetAddress address : whitelist) {
                        try {
                            sendSocket.send(new DatagramPacket(arr, arr.length, address, GameControlData.GAMECONTROLLER_GAMEDATA_PORT));
                        } catch (IOException e) {
                            Log.error("Error while sending true game data");
                        }
                    }
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                interrupt();
            }
        }

        requestReceiver.interrupt();
        sendSocket.close();
        try {
            requestReceiver.join();
        } catch (InterruptedException ex) {
        }
    }
}
