package teamcomm.data;

import data.SPLStandardMessage;
import java.util.LinkedList;
import teamcomm.net.SPLStandardMessageReceiver;

/**
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
        recentMessageTimestamps.addFirst(System.currentTimeMillis());
        messageCount++;
    }

    public String getAddress() {
        return address;
    }

    public SPLStandardMessage getLastMessage() {
        return lastMessage;
    }

    public int getRecentMessageCount() {
        final long cut;
        if(SPLStandardMessageReceiver.getInstance().isReplaying() && SPLStandardMessageReceiver.getInstance().isReplayPaused()) {
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

    public double getMessagesPerSecond() {
        getRecentMessageCount();

        long sum = 0;
        for (final int mps : messagesPerSecond) {
            sum += mps;
        }

        return (double) sum / (double) messagesPerSecond.size();
    }

    public int getMessageCount() {
        return messageCount;
    }

    public int getIllegalMessageCount() {
        return illegalMessageCount;
    }

    public double getIllegalMessageRatio() {
        return illegalMessageCount / (illegalMessageCount + messageCount);
    }

    public int getTeamNumber() {
        return teamNumber;
    }
    
    public boolean isInactive() {
        return recentMessageTimestamps.isEmpty();
    }

}
