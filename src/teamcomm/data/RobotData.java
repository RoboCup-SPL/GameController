package teamcomm.data;

import data.GameControlData;
import data.Rules;
import data.SPLStandardMessage;
import data.TeamInfo;
import data.Teams;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import teamcomm.PluginLoader;

/**
 * Singleton class managing the known information about communicating robots.
 *
 * @author Felix Thielke
 */
public class RobotData {

    /**
     * Index of the team playing on the left side of the field.
     */
    public static final int TEAM_LEFT = 0;
    /**
     * Index of the team playing on the right side of the field.
     */
    public static final int TEAM_RIGHT = 1;
    /**
     * Index of the virtual team containing illegaly communicating robots.
     */
    public static final int TEAM_OTHER = 2;

    private static RobotData instance;

    private final int[] teamNumbers = new int[]{0, 0};
    private final Map<Integer, Integer> teamColors = new HashMap<Integer, Integer>();

    private boolean mirrored = false;

    @SuppressWarnings("unchecked")
    private final List<RobotState>[] robots = new List[]{new ArrayList<RobotState>(5), new ArrayList<RobotState>(5), new LinkedList<RobotState>()};
    private final HashMap<String, RobotState> robotsByAddress = new HashMap<String, RobotState>();

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    /**
     * Returns the only instance of the RobotData class.
     *
     * @return instance
     */
    public static RobotData getInstance() {
        if (instance == null) {
            instance = new RobotData();
        }
        return instance;
    }

    private RobotData() {
    }

    /**
     * Resets all information about robots and teams.
     */
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

    /**
     * Updates info about the game with a message from the GameController.
     *
     * @param data data sent by the GameController
     */
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

                // Update penalties
                for (int i = 0; i < 2; i++) {
                    for (final RobotState r : robots[i]) {
                        if (r.getLastMessage() != null && r.getLastMessage().playerNum - 1 < data.team[i].player.length) {
                            r.setPenalty(data.team[i].player[r.getLastMessage().playerNum - 1].penalty);
                        }
                    }
                }

                // Update team colors
                for (int i = 0; i < 2; i++) {
                    teamColors.put((int) data.team[i].teamNumber, (int) data.team[i].teamColor);
                }
            }
        } finally {
            rwl.writeLock().unlock();
        }

        if (somethingChanged) {
            if (data != null) {
                // (re)load plugins
                PluginLoader.getInstance().update((int) data.team[0].teamNumber, (int) data.team[1].teamNumber);

                // handle dropin games
                if ((data.team[0].teamNumber == 98 || data.team[0].teamNumber == 99) && (data.team[1].teamNumber == 98 || data.team[1].teamNumber == 99)) {
                    Rules.league = Rules.LEAGUES[1];
                } else {
                    Rules.league = Rules.LEAGUES[0];
                }
            }

            synchronized (this) {
                notifyAll();
            }
        }
    }

    /**
     * Handles a message that was received from a robot.
     *
     * @param address IP address of the sender
     * @param teamNumber team number belonging to the port on which the message
     * was received
     * @param message received message
     */
    public void receiveMessage(final String address, final int teamNumber, final SPLStandardMessage message) {
        rwl.writeLock().lock();
        try {
            // update the team info if no GameController info is available
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

                        // (re)load plugins
                        PluginLoader.getInstance().update(teamNumber);

                        // handle dropin games
                        if ((teamNumbers[0] == 0 || teamNumbers[0] == 98 || teamNumbers[0] == 99) && (teamNumbers[1] == 0 || teamNumbers[1] == 98 || teamNumbers[1] == 99)) {
                            Rules.league = Rules.LEAGUES[1];
                        } else {
                            Rules.league = Rules.LEAGUES[0];
                        }
                        break;
                    } else if (teamNumbers[i] == teamNumber) {
                        break;
                    }
                }
            }

            // create the robot state if it does not yet exist
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

            // let the robot state handle the message
            r.registerMessage(message);

            // sort the robot data by player numbers
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

    /**
     * Remove information about robots who did not send any messages for a
     * while.
     */
    public void removeInactiveRobots() {
        rwl.writeLock().lock();
        try {
            for (int i = 0; i < 3; i++) {
                final ListIterator<RobotState> iter = robots[i].listIterator();
                while (iter.hasNext()) {
                    final RobotState r = iter.next();
                    if (r.getMessageCount() > 10 && r.isInactive()) {
                        iter.remove();
                        robotsByAddress.remove(r.getAddress());
                    }
                }
            }
        } finally {
            rwl.writeLock().unlock();
        }
    }

    /**
     * Lock the RobotData for reading. Call this before calling any of the
     * methods for data retrieval in order to avoid synchronization issues.
     */
    public void lockForReading() {
        rwl.readLock().lock();
    }

    /**
     * Unlock the RobotData after locking it via lockForReading().
     */
    public void unlockForReading() {
        rwl.readLock().unlock();
    }

    /**
     * Returns an iterator for the robot states of the given team.
     *
     * @param team one of RobotData#TEAM_LEFT, RobotData#TEAM_RIGHT and
     * RobotData#TEAM_OTHER
     * @return an iterator for the robot states of the given team or null if no
     * info about teams is available
     */
    public Iterator<RobotState> getRobotsForTeam(final int team) {
        if (team >= 0 && team <= 2) {
            return robots[outputSide(team)].iterator();
        } else {
            return null;
        }
    }

    /**
     * Returns an iterator for the robot states of robots not associated with a
     * playing team.
     *
     * @return iterator
     */
    public Iterator<RobotState> getOtherRobots() {
        return robots[TEAM_OTHER].iterator();
    }

    /**
     * Returns the team numbers of the currently playing teams.
     *
     * @return team numbers of the currently playing teams or null if no info
     * about teams is available
     */
    public int[] getTeamNumbers() {
        if (teamNumbers[0] == 0) {
            return null;
        } else {
            if (mirrored) {
                return new int[]{teamNumbers[1], teamNumbers[0]};
            } else {
                return teamNumbers.clone();
            }
        }
    }

    /**
     * Returns the team color of the given team. The team color is either sent
     * by the game controller or given by the GameController configuration.
     *
     * @param teamNumber number of the team
     * @return the team color
     * @see TeamInfo#teamColor
     */
    public int getTeamColor(final int teamNumber) {
        Integer color = teamColors.get(teamNumber);
        if (color == null) {
            String[] colorStrings = null;
            try {
                colorStrings = Teams.getColors(teamNumber);
            } catch (NullPointerException e) {
            } catch (ArrayIndexOutOfBoundsException e) {
            }
            if (colorStrings == null || colorStrings.length < 1) {
                if (teamNumber == teamNumbers[TEAM_RIGHT]) {
                    return GameControlData.TEAM_RED;
                } else {
                    return GameControlData.TEAM_BLUE;
                }
            } else if (colorStrings[0].equals("blue")) {
                return GameControlData.TEAM_BLUE;
            } else if (colorStrings[0].equals("red")) {
                return GameControlData.TEAM_RED;
            } else if (colorStrings[0].equals("yellow")) {
                return GameControlData.TEAM_YELLOW;
            } else {
                return GameControlData.TEAM_BLACK;
            }
        }

        return color;
    }
    
    public String getTeamName(final Integer teamNumber) {
        final String[] teamNames = Teams.getNames(true);
        if (teamNumber != null) {
            if (teamNumber < teamNames.length) {
                return ("Team " + teamNames[teamNumber]);
            } else {
                return ("Unknown Team (" + teamNumber + ")");
            }
        } else {
            return "Unknown Team";
        }
    }

    /**
     * Returns whether the team sides are mirrored.
     *
     * @return boolean
     */
    public boolean isMirrored() {
        return mirrored;
    }

    /**
     * Sets whether the team sides are mirrored.
     *
     * @param mirrored boolean
     */
    public void setMirrored(final boolean mirrored) {
        this.mirrored = mirrored;
    }

    private int outputSide(final int side) {
        return mirrored ? (side == 0 ? 1 : (side == 1 ? 0 : side)) : side;
    }

}
