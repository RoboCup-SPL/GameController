package teamcomm.data;

import data.SPLStandardMessage;

/**
 *
 * @author Felix Thielke
 */
public class RobotState {

    private final String address;
    private SPLStandardMessage lastMessage;
    private int messageCount = 0;
    private int illegalMessageCount = 0;
    private final int teamNumber;
    private final long startTimestamp = System.currentTimeMillis();

    public RobotState(final String address, final int teamNumber) {
        this.address = address;
        this.teamNumber = teamNumber;
    }

    public void registerMessage(final SPLStandardMessage message) {
        if (message == null) {
            illegalMessageCount++;
        } else {
            lastMessage = message;
        }
        messageCount++;
    }

    public String getAddress() {
        return address;
    }

    public SPLStandardMessage getLastMessage() {
        return lastMessage;
    }

    public double getMessagesPerSecond() {
        return (double) (System.currentTimeMillis() - startTimestamp) / 1000.0 * (double) messageCount;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public int getIllegalMessageCount() {
        return illegalMessageCount;
    }

    public int getTeamNumber() {
        return teamNumber;
    }

}
