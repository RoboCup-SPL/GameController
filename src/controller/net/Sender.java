package controller.net;

import common.Log;
import data.AdvancedData;
import data.GameControlData;
import data.Rules;
import data.Teams;
import java.io.IOException;
import java.net.*;

/**
 * @author Marcel Steinbeck
 *
 * This class is used to send the current {@link GameControlData} (game-state)
 * to all robots every 500 ms. The package will be send via UDP on port
 * {@link GameControlData#GAMECONTROLLER_GAMEDATA_PORT} over broadcast.
 *
 * To prevent race-conditions (the sender is executed in its thread-context),
 * the sender will hold a deep copy of {@link GameControlData} (have a closer
 * look to the copy-constructor
 * {@link GameControlData#GameControlData(data.GameControlData)}).
 *
 * This class is a singleton!
 */
public class Sender extends Thread {

    /**
     * The instance of the singleton.
     */
    private static Sender instance;

    /**
     * The packet number that is increased with each packet sent.
     */
    private byte packetNumber = 0;

    /**
     * The socket, which is used to send the current game-state
     */
    private final DatagramSocket datagramSocket;

    /**
     * The used inet-address (the broadcast address).
     */
    private final InetAddress group;

    /**
     * The current deep copy of the game-state.
     */
    private AdvancedData data;

    /**
     * Creates a new Sender.
     *
     * @throws SocketException if an error occurs while creating the socket
     * @throws UnknownHostException if the used inet-address is not valid
     */
    private Sender(final InetAddress broadcastAddress) throws SocketException, UnknownHostException {
        instance = this;

        this.datagramSocket = new DatagramSocket();
        this.group = broadcastAddress;
    }

    /**
     * Initialises the Sender. This needs to be called before
     * {@link #getInstance()} is available.
     *
     * @param broadcastAddress the broadcast address to use
     * @throws SocketException if an error occurs while creating the socket
     * @throws UnknownHostException if the used inet-address is not valid
     * @throws IllegalStateException if the sender is already initialized
     */
    public synchronized static void initialize(final InetAddress broadcastAddress) throws SocketException, UnknownHostException {
        if (null != instance) {
            throw new IllegalStateException("sender is already initialized");
        } else {
            instance = new Sender(broadcastAddress);
        }
    }

    /**
     * Returns the instance of the singleton.
     *
     * @return The instance of the Sender
     * @throws IllegalStateException if the Sender is not initialized yet
     */
    public synchronized static Sender getInstance() {
        if (null == instance) {
            throw new IllegalStateException("sender is not initialized yet");
        } else {
            return instance;
        }
    }

    /**
     * Sets the current game-state to send. Creates a deep copy of data to
     * prevent race-conditions. Have a closer look to
     * {@link GameControlData#GameControlData(data.GameControlData)}
     *
     * @param data the current game-state to send to all robots
     */
    public void send(AdvancedData data) {
        // Adjust name of the current teamcomm logfile
        if (this.data == null
                || (data.gameState == GameControlData.STATE_READY && this.data.gameState == GameControlData.STATE_INITIAL)
                || (data.gameState == GameControlData.STATE_INITIAL && this.data.gameState != GameControlData.STATE_INITIAL)
                || (data.gameState == GameControlData.STATE_FINISHED && this.data.gameState != GameControlData.STATE_FINISHED)) {
            final StringBuilder logfileName = new StringBuilder();
            final String[] teamNames = Teams.getNames(false);
            if (data.competitionType == GameControlData.COMPETITION_TYPE_GENERAL_PENALTY_KICK) {
                logfileName.append("GPKC_");
            }
            if (data.firstHalf == GameControlData.C_TRUE) {
                logfileName.append(data.team[0].teamNumber < teamNames.length && teamNames[data.team[0].teamNumber] != null ? teamNames[data.team[0].teamNumber] : "Unknown").append("_").append(data.team[1].teamNumber < teamNames.length && teamNames[data.team[1].teamNumber] != null ? teamNames[data.team[1].teamNumber] : "Unknown");
            } else {
                logfileName.append(data.team[1].teamNumber < teamNames.length && teamNames[data.team[1].teamNumber] != null ? teamNames[data.team[1].teamNumber] : "Unknown").append("_").append(data.team[0].teamNumber < teamNames.length && teamNames[data.team[0].teamNumber] != null ? teamNames[data.team[0].teamNumber] : "Unknown");
            }
            if (!Rules.league.startWithPenalty) {
                logfileName.append(data.firstHalf == GameControlData.C_TRUE ? "_1st" : "_2nd").append("Half");
            }
            if (data.gameState == GameControlData.STATE_READY && (this.data == null || this.data.gameState == GameControlData.STATE_INITIAL)) {
                teamcomm.net.logging.Logger.getInstance().createLogfile(logfileName.toString());
            } else if (data.gameState == GameControlData.STATE_INITIAL && (this.data == null || this.data.gameState != GameControlData.STATE_INITIAL)) {
                teamcomm.net.logging.Logger.getInstance().createLogfile(logfileName.append("_initial").toString());
            } else if (data.gameState == GameControlData.STATE_FINISHED && (this.data == null || this.data.gameState != GameControlData.STATE_FINISHED)) {
                teamcomm.net.logging.Logger.getInstance().createLogfile(logfileName.append("_finished").toString());
            }
        }

        // Clone data
        this.data = (AdvancedData) data.clone();

        // Hand data over to the TrueDataSender
        TrueDataSender.getInstance().send(data);
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            final AdvancedData data = this.data;
            if (data != null) {
                data.updateTimes(false);
                data.packetNumber = packetNumber;
                teamcomm.net.logging.Logger.getInstance().log(data);
                byte[] arr = data.toByteArray().array();
                DatagramPacket packet = new DatagramPacket(arr, arr.length, group, GameControlData.GAMECONTROLLER_GAMEDATA_PORT);

                try {
                    datagramSocket.send(packet);
                    packetNumber++;
                } catch (IOException e) {
                    Log.error("Error while sending");
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                interrupt();
            }
        }

        datagramSocket.close();
    }
}
