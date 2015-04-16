package teamcomm.data;

import data.PlayerInfo;
import data.SPLStandardMessage;
import java.util.LinkedList;
import teamcomm.net.SPLStandardMessageReceiver;

/**
 * Class representing the state of a robot.
 *
 * @author Felix Thielke
 */
public class RobotState {

    private final String address;
    private SPLStandardMessage lastMessage;
    private final LinkedList<Long> recentMessageTimestamps = new LinkedList<Long>();
    private final LinkedList<Integer> messagesPerSecond = new LinkedList<Integer>();
    private long lastMpsTest = 0;
    private int messageCount = 0;
    private int illegalMessageCount = 0;
    private final int teamNumber;
    private byte penalty = PlayerInfo.PENALTY_NONE;

    /**
     * Constructor.
     *
     * @param address IP address of the robot
     * @param teamNumber team number associated with the port the robot sends
     * his messages on
     */
    public RobotState(final String address, final int teamNumber) {
        this.address = address;
        this.teamNumber = teamNumber;
    }

    /**
     * Handles a message received by the robot this object corresponds to.
     *
     * @param message received message or null if the message was invalid
     */
    public void registerMessage(final SPLStandardMessage message) {
        if (message == null) {
            illegalMessageCount++;
        } else {
            lastMessage = message;
        }
        recentMessageTimestamps.addFirst(System.currentTimeMillis());
        messageCount++;
    }

    /**
     * Returns the IP address of the robot.
     *
     * @return IP address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Returns the most recent legal message received from this robot.
     *
     * @return message
     */
    public SPLStandardMessage getLastMessage() {
        return lastMessage;
    }

    /**
     * Returns the number of messages received during the last second.
     *
     * @return number of messages in the last second
     */
    public int getRecentMessageCount() {
        final long cut;
        if (SPLStandardMessageReceiver.getInstance().isReplaying() && SPLStandardMessageReceiver.getInstance().isReplayPaused()) {
            cut = 0;
        } else {
            cut = System.currentTimeMillis() - 1000;
        }

        Long val = recentMessageTimestamps.peekLast();
        while (val != null && val < cut) {
            recentMessageTimestamps.pollLast();
            val = recentMessageTimestamps.peekLast();
        }

        final int mps = recentMessageTimestamps.size();
        if (lastMpsTest <= cut) {
            messagesPerSecond.add(mps);
            lastMpsTest = System.currentTimeMillis();
        }

        return mps;
    }

    /**
     * Returns the average number of messages per second.
     *
     * @return number of messages per second
     */
    public double getMessagesPerSecond() {
        getRecentMessageCount();

        long sum = 0;
        for (final int mps : messagesPerSecond) {
            sum += mps;
        }

        return (double) sum / (double) messagesPerSecond.size();
    }

    /**
     * Returns the total count of received messages.
     *
     * @return total message count
     */
    public int getMessageCount() {
        return messageCount;
    }

    /**
     * Returns the total count of illegal messages.
     *
     * @return illegal messasge count
     */
    public int getIllegalMessageCount() {
        return illegalMessageCount;
    }

    /**
     * Returns the ratio of illegal messages to the total count of messages.
     *
     * @return ratio
     */
    public double getIllegalMessageRatio() {
        return (double) illegalMessageCount / (double) messageCount;
    }

    /**
     * Returns the team number of this robot.
     *
     * @return team number
     */
    public int getTeamNumber() {
        return teamNumber;
    }

    /**
     * Returns whether this robot is considered inactive because he did not send
     * any messages for a while. This while is currently defined as one second.
     *
     * @return boolean
     */
    public boolean isInactive() {
        return recentMessageTimestamps.isEmpty();
    }

    /**
     * Returns the current penalty of the robot.
     *
     * @return penalty
     * @see PlayerInfo#penalty
     */
    public byte getPenalty() {
        return penalty;
    }

    /**
     * Sets the current penalty of the robot.
     *
     * @param penalty penalty
     * @see PlayerInfo#penalty
     */
    public void setPenalty(final byte penalty) {
        this.penalty = penalty;
    }
}
