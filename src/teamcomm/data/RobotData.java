package teamcomm.data;

import data.GameControlData;
import data.SPLStandardMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Felix Thielke
 */
public class RobotData {

    public static final int TEAM_LEFT = 0;
    public static final int TEAM_RIGHT = 1;
    public static final int TEAM_OTHER = 2;

    private static RobotData instance;
    private final int[] teamNumbers = new int[]{0, 0};

    @SuppressWarnings("unchecked")
    private final List<RobotState>[] robots = new List[]{new ArrayList<RobotState>(5), new ArrayList<RobotState>(5), new LinkedList<RobotState>()};
    private final HashMap<String, RobotState> robotsByAddress = new HashMap<String, RobotState>();

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    public static RobotData getInstance() {
        if (instance == null) {
            instance = new RobotData();
        }
        return instance;
    }

    private RobotData() {
    }

    public void reset() {
        rwl.writeLock().lock();
        try {
            teamNumbers[0] = 0;
            teamNumbers[1] = 0;
            robots[0].clear();
            robots[1].clear();
            robots[2].clear();
            robotsByAddress.clear();
        } finally {
            rwl.writeLock().unlock();
        }
    }

    public void updateGameData(final GameControlData data) {
        boolean somethingChanged = false;

        rwl.writeLock().lock();
        try {
            if (data == null) {
                if (teamNumbers[0] != 0 && !robots[TEAM_OTHER].isEmpty()) {
                    // Delete info about teams
                    teamNumbers[0] = 0;
                    teamNumbers[1] = 0;
                    for (int i = 0; i < 2; i++) {
                        teamNumbers[i] = 0;
                        robots[TEAM_OTHER].addAll(robots[i]);
                        robots[i].clear();
                    }
                    somethingChanged = true;
                }
            } else {
                // Update teams
                for (int i0 = 0; i0 < 2; i0++) {
                    final int i1 = (i0 + 1) % 2;
                    if (data.team[i0].teamNumber != teamNumbers[i0]) {
                        if (data.team[i0].teamNumber == teamNumbers[i1]) {
                            if (data.team[i1].teamNumber == teamNumbers[i0]) {
                                final List<RobotState> temp = robots[i0];
                                robots[i0] = robots[i1];
                                robots[i1] = temp;
                                teamNumbers[i1] = data.team[i1].teamNumber;
                            } else {
                                robots[TEAM_OTHER].addAll(robots[i0]);
                                robots[i0].clear();
                                robots[i0].addAll(robots[i1]);
                            }
                        } else {
                            robots[TEAM_OTHER].addAll(robots[i0]);
                            robots[i0].clear();
                            ListIterator<RobotState> it = robots[TEAM_OTHER].listIterator();
                            while (it.hasNext()) {
                                final RobotState r = it.next();
                                if (r.getTeamNumber() == data.team[i0].teamNumber) {
                                    robots[i0].add(r);
                                    it.remove();
                                }
                            }
                        }
                        teamNumbers[i0] = data.team[i0].teamNumber;
                        somethingChanged = true;
                    }
                }
            }
        } finally {
            rwl.writeLock().unlock();
        }

        if (somethingChanged) {
            synchronized (this) {
                notifyAll();
            }
        }
    }

    public void receiveMessage(final String address, final int teamNumber, final SPLStandardMessage message) {
        rwl.writeLock().lock();
        try {
            if (message != null) {
                for (int i = 0; i < 2; i++) {
                    if (teamNumbers[i] == 0) {
                        teamNumbers[i] = teamNumber;
                        ListIterator<RobotState> it = robots[TEAM_OTHER].listIterator();
                        while (it.hasNext()) {
                            final RobotState r = it.next();
                            if (r.getTeamNumber() == teamNumber) {
                                robots[i].add(r);
                                it.remove();
                            }
                        }
                        break;
                    } else if (teamNumbers[i] == teamNumber) {
                        break;
                    }
                }
            }

            RobotState r = robotsByAddress.get(address);
            if (r == null) {
                r = new RobotState(address, teamNumber);

                if (teamNumber == teamNumbers[0]) {
                    robots[0].add(r);
                } else if (teamNumber == teamNumbers[1]) {
                    robots[1].add(r);
                } else {
                    robots[2].add(r);
                }
                robotsByAddress.put(address, r);
            }

            r.registerMessage(message);

            for (int t = 0; t < 2; t++) {
                if (r.getTeamNumber() == teamNumbers[t]) {
                    Collections.sort(robots[t], new Comparator<RobotState>() {
                        @Override
                        public int compare(RobotState o1, RobotState o2) {
                            if (o1.getLastMessage() == null) {
                                if (o2.getLastMessage() == null) {
                                    return 0;
                                }
                                return -1;
                            } else if (o2.getLastMessage() == null) {
                                return 1;
                            }
                            return o1.getLastMessage().playerNum - o2.getLastMessage().playerNum;
                        }
                    });
                    break;
                }
            }
        } finally {
            rwl.writeLock().unlock();
        }

        synchronized (this) {
            notifyAll();
        }
    }

    public void removeInactiveRobots() {
        rwl.writeLock().lock();
        try {
            for (int i = 0; i < 3; i++) {
                final ListIterator<RobotState> iter = robots[i].listIterator();
                while (iter.hasNext()) {
                    final RobotState r = iter.next();
                    if (r.getMessageCount() > 10 && r.isInactive()) {
                        iter.remove();
                    }
                }
            }
        } finally {
            rwl.writeLock().unlock();
        }
    }

    public void lockForReading() {
        rwl.readLock().lock();
    }

    public void unlockForReading() {
        rwl.readLock().unlock();
    }

    public Iterator<RobotState> getRobotsForTeam(final int team) {
        if (team >= 0 && team <= 2) {
            return robots[team].iterator();
        } else {
            return null;
        }
    }

    public Iterator<RobotState> getOtherRobots() {
        return robots[TEAM_OTHER].iterator();
    }

    public int[] getTeamNumbers() {
        if (teamNumbers[0] == 0) {
            return null;
        } else {
            return teamNumbers.clone();
        }
    }
}
