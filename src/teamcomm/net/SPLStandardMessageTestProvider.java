package teamcomm.net;

import data.SPLStandardMessage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;
import teamcomm.data.RobotData;

/**
 *
 * @author Felix Thielke
 */
public class SPLStandardMessageTestProvider extends Thread implements ISPLStandardMessageReceiver {

    private final int[] teams;
    private final int robotsPerTeam;

    public SPLStandardMessageTestProvider(final int team1, final int team2, final int robotsPerTeam) {
        this.teams = new int[] {team1, team2};
        this.robotsPerTeam = robotsPerTeam;
    }

    @Override
    public void run() {
        final Random rand = new Random();
        
        while(!isInterrupted()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
            }
            
            final int team = rand.nextInt(2);
            final int robot = rand.nextInt(robotsPerTeam)+1;
            
            final SPLStandardMessage message = new SPLStandardMessage();
            final byte[] bytes = new byte[SPLStandardMessage.SIZE];
            rand.nextBytes(bytes);
            final ByteBuffer buffer = ByteBuffer.wrap(bytes);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.put(SPLStandardMessage.SPL_STANDARD_MESSAGE_STRUCT_HEADER.getBytes());
            buffer.put(SPLStandardMessage.SPL_STANDARD_MESSAGE_STRUCT_VERSION);
            buffer.put((byte)robot);
            buffer.put((byte)team);
            buffer.put((byte)0);
            buffer.position(56);
            buffer.putShort((short)rand.nextInt(SPLStandardMessage.Intention.values().length));
            buffer.putShort(SPLStandardMessage.SPL_STANDARD_MESSAGE_DATA_SIZE);
            buffer.rewind();
            
            RobotData.getInstance().receiveMessage("10.0." + teams[team] + "." + robot, teams[team], message.fromByteArray(buffer) ? message : null);
        }
    }
}
