package controller.net;

import controller.EventHandler;
import data.GameControlReturnData;
import data.Rules;

/**
 * @author Marcel Steinbeck, Michel Bartsch
 *
 * You can ask this class about the robots online-status.
 *
 * This class is a sigleton!
 */
public class RobotWatcher
{
    /** The instance of the singleton. */
    private static RobotWatcher instance = new RobotWatcher();

    /** A timestamp when the last reply from each robot was received. */
    private long [][] robotsLastAnswer = new long[2][Rules.league.teamSize];
    /** The calculated information about the online-status. */
    private RobotOnlineStatus [][] status = new RobotOnlineStatus[2][Rules.league.teamSize];

    /** What the constants name says. */
    private final static int MILLIS_UNTIL_ROBOT_IS_OFFLINE = 4*1000;
    private final static int MILLIS_UNTIL_ROBOT_HAS_HIGH_LATANCY = 2*1000;

    /**
     * Creates a new RobotWatcher.
     */
    private RobotWatcher()
    {
        for (int i  = 0; i < 2; i++) {
            for (int j = 0; j < Rules.league.teamSize; j++) {
                status[i][j] = RobotOnlineStatus.UNKNOWN;
            }
        }
    }

    /**
     * Recieves robots' answers to update corresponding timestamps.
     *
     * @param gameControlReturnData     The robot's answer.
     */
    public static synchronized void update(GameControlReturnData gameControlReturnData)
    {
        int team, number;
        if (gameControlReturnData.team == EventHandler.getInstance().data.team[0].teamNumber) {
            team = 0;
        } else if (gameControlReturnData.team == EventHandler.getInstance().data.team[1].teamNumber) {
            team = 1;
        } else {
            return;
        }
        number = gameControlReturnData.player;
        if (number <= 0 || number > Rules.league.teamSize) {
            return;
        }
        instance.robotsLastAnswer[team][number-1] = System.currentTimeMillis();
    }

    /**
     * Calculates new online-status for each robot.
     *
     * @return The updated online-status of each robot.
     */
    public static synchronized RobotOnlineStatus[][] updateRobotOnlineStatus()
    {
        long currentTime = System.currentTimeMillis();
        int robotsOffline;
        for (int i=0; i<2; i++) {
            robotsOffline = 0;
            for (int j=0; j < instance.status[i].length; j++) {
                if (currentTime - instance.robotsLastAnswer[i][j] > MILLIS_UNTIL_ROBOT_IS_OFFLINE) {
                    instance.status[i][j] = RobotOnlineStatus.OFFLINE;
                    if (++robotsOffline >= Rules.league.teamSize) {
                        for (int k=0; k < Rules.league.teamSize; k++) {
                            instance.status[i][k] = RobotOnlineStatus.UNKNOWN;
                        }
                    }
                } else if (currentTime - instance.robotsLastAnswer[i][j] > MILLIS_UNTIL_ROBOT_HAS_HIGH_LATANCY) {
                    instance.status[i][j] = RobotOnlineStatus.HIGH_LATENCY;
                } else {
                    instance.status[i][j] = RobotOnlineStatus.ONLINE;
                }
            }
        }
        return instance.status;
    }
}
