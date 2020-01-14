package teamcomm.data;

import common.ApplicationLock;
import data.GameControlData;
import data.Rules;
import data.SPL;
import data.SPLMixedTeam;
import data.SPLStandardMessage;
import data.TeamInfo;
import data.Teams;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.event.EventListenerList;
import teamcomm.PluginLoader;
import teamcomm.data.event.GameControlDataEvent;
import teamcomm.data.event.GameControlDataEventListener;
import teamcomm.data.event.GameControlDataTimeoutEvent;
import teamcomm.data.event.TeamEvent;
import teamcomm.data.event.TeamEventListener;
import teamcomm.net.logging.LogReplayer;
import teamcomm.net.logging.Logger;

/**
 * Singleton class managing the known information about communicating robots.
 *
 * @author Felix Thielke
 */
public class GameState implements GameControlDataEventListener {

    /**
     * Index of the team playing on the left side of the field.
     */
    public static final int TEAM_LEFT = 0;
    /**
     * Index of the team playing on the right side of the field.
     */
    public static final int TEAM_RIGHT = 1;
    /**
     * Index of the virtual team containing illegally communicating robots.
     */
    public static final int TEAM_OTHER = 2;

    private static final int CHANGED_LEFT = 1;
    private static final int CHANGED_RIGHT = 2;
    private static final int CHANGED_OTHER = 4;

    private static final GameState instance = new GameState();

    private GameControlData lastGameControlData;

    private final int[] teamNumbers = new int[]{0, 0};
    private final Map<Integer, Integer> teamColors = new HashMap<>();

    private boolean mirrored = false;

    private final Map<Integer, Collection<RobotState>> robots = new HashMap<>();

    private static final Comparator<RobotState> playerNumberComparator = new Comparator<RobotState>() {
        @Override
        public int compare(RobotState o1, RobotState o2) {
            if (o1.getPlayerNumber() == null) {
                if (o2.getPlayerNumber() == null) {
                    return o1.hashCode() - o2.hashCode();
                }
                return -1;
            } else if (o2.getPlayerNumber() == null) {
                return 1;
            }
            return o1.getPlayerNumber() - o2.getPlayerNumber();
        }
    };

    private final HashMap<String, RobotState> robotsByAddress = new HashMap<>();

    private final EventListenerList listeners = new EventListenerList();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ScheduledFuture<?> taskHandle;

    /**
     * Returns the only instance of the RobotData class.
     *
     * @return instance
     */
    public static GameState getInstance() {
        return instance;
    }

    private GameState() {
        taskHandle = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!(LogReplayer.getInstance().isReplaying() && LogReplayer.getInstance().isPaused())) {
                    // Check if the GameController is running
                    try {
                        final ApplicationLock lock = new ApplicationLock("GameController");
                        if (!lock.acquire()) {
                            // Do not log messages if a GameController is running on the same system
                            Logger.getInstance().disableLogging();
                        } else {
                            Logger.getInstance().enableLogging();
                            lock.release();
                        }
                    } catch (IOException e) {
                    }

                    // Update robots
                    int changed = 0;
                    synchronized (robotsByAddress) {
                        final Iterator<RobotState> iter = robotsByAddress.values().iterator();
                        while (iter.hasNext()) {
                            final RobotState r = iter.next();
                            if (r.updateConnectionStatus() == RobotState.ConnectionStatus.INACTIVE) {
                                iter.remove();
                            }
                        }

                        for (final Entry<Integer, Collection<RobotState>> team : robots.entrySet()) {
                            final Iterator<RobotState> it = team.getValue().iterator();
                            while (it.hasNext()) {
                                final RobotState r = it.next();
                                if (!robotsByAddress.containsKey(r.getAddress())) {
                                    it.remove();

                                    synchronized (teamNumbers) {
                                        if (team.getKey() == teamNumbers[TEAM_LEFT]) {
                                            changed |= CHANGED_LEFT;
                                            if (team.getValue().isEmpty() && lastGameControlData == null) {
                                                teamNumbers[TEAM_LEFT] = 0;
                                            }
                                        } else if (team.getKey() == teamNumbers[TEAM_RIGHT]) {
                                            changed |= CHANGED_RIGHT;
                                            if (team.getValue().isEmpty() && lastGameControlData == null) {
                                                teamNumbers[TEAM_RIGHT] = 0;
                                            }
                                        } else {
                                            changed |= CHANGED_OTHER;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    sendEvents(changed);
                }
            }
        }, RobotState.ConnectionStatus.HIGH_LATENCY.threshold * 2, RobotState.ConnectionStatus.HIGH_LATENCY.threshold / 2, TimeUnit.MILLISECONDS);
    }

    /**
     * Shuts down the thread which removes inactive robots. To be called before
     * the program exits.
     */
    public void shutdown() {
        taskHandle.cancel(false);
    }

    /**
     * Resets all information about robots and teams.
     */
    public void reset() {
        lastGameControlData = null;
        synchronized (teamNumbers) {
            teamNumbers[0] = 0;
            teamNumbers[1] = 0;
        }
        synchronized (robotsByAddress) {
            robots.clear();
            robotsByAddress.clear();
        }
        sendEvents(CHANGED_LEFT | CHANGED_RIGHT | CHANGED_OTHER);
    }

    /**
     * Updates info about the game with a message from the GameController.
     *
     * @param e event containing the data sent by the GameController
     */
    @Override
    public void gameControlDataChanged(final GameControlDataEvent e) {
        int changed = 0;

        if (lastGameControlData == null) {
            synchronized (teamNumbers) {
                teamNumbers[TEAM_LEFT] = e.data.team[0].teamNumber;
                teamNumbers[TEAM_RIGHT] = e.data.team[1].teamNumber;
            }
            changed = CHANGED_LEFT | CHANGED_RIGHT | CHANGED_OTHER;
        } else {
            synchronized (teamNumbers) {
                if (e.data.team[0].teamNumber != teamNumbers[TEAM_LEFT]) {
                    teamNumbers[TEAM_LEFT] = e.data.team[0].teamNumber;
                    changed = CHANGED_LEFT | CHANGED_OTHER;
                }
                if (e.data.team[1].teamNumber != teamNumbers[TEAM_RIGHT]) {
                    teamNumbers[TEAM_RIGHT] = e.data.team[1].teamNumber;
                    changed |= CHANGED_RIGHT | CHANGED_OTHER;
                }
            }
        }

        teamColors.put((int) e.data.team[0].teamNumber, (int) e.data.team[0].teamColor);
        teamColors.put((int) e.data.team[1].teamNumber, (int) e.data.team[1].teamColor);

        // Update penalties
        for (final TeamInfo team : e.data.team) {
            final Collection<RobotState> teamRobots = robots.get((int) team.teamNumber);
            if (teamRobots != null) {
                for (final RobotState r : teamRobots) {
                    if (r.getPlayerNumber() != null && r.getPlayerNumber() <= team.player.length) {
                        r.setPenalty(team.player[r.getPlayerNumber() - 1].penalty);
                    }
                }
            }
        }

        if (changed != 0) {
            // (re)load plugins
            PluginLoader.getInstance().update((int) e.data.team[0].teamNumber, (int) e.data.team[1].teamNumber);
        }

        if (LogReplayer.getInstance().isReplaying()) {
            lastGameControlData = e.data;
            return;
        }

        // Open a new logfile for the current GameController state if the
        // state changed from or to initial/finished
        final StringBuilder logfileName = new StringBuilder();
        if (Rules.league.competitionType == GameControlData.COMPETITION_TYPE_MIXEDTEAM || (e.data.team[0].teamNumber >= 90 && e.data.team[0].teamNumber < 100 && e.data.team[1].teamNumber >= 90 && e.data.team[1].teamNumber < 100)) {
            logfileName.append("MixedTeam_");
        }
        if (e.data.firstHalf == GameControlData.C_TRUE) {
            logfileName.append(getTeamName((int) e.data.team[0].teamNumber, false, false)).append("_").append(getTeamName((int) e.data.team[1].teamNumber, false, false));
        } else {
            logfileName.append(getTeamName((int) e.data.team[1].teamNumber, false, false)).append("_").append(getTeamName((int) e.data.team[0].teamNumber, false, false));
        }
        logfileName.append(e.data.firstHalf == GameControlData.C_TRUE ? "_1st" : "_2nd").append("Half");
        if (e.data.gameState == GameControlData.STATE_READY && (lastGameControlData == null || lastGameControlData.gameState == GameControlData.STATE_INITIAL)) {
            Logger.getInstance().createLogfile(logfileName.toString());
        } else if (e.data.gameState == GameControlData.STATE_INITIAL && (lastGameControlData == null || lastGameControlData.gameState != GameControlData.STATE_INITIAL)) {
            Logger.getInstance().createLogfile(logfileName.append("_initial").toString());
        } else if (e.data.gameState == GameControlData.STATE_FINISHED && (lastGameControlData == null || lastGameControlData.gameState != GameControlData.STATE_FINISHED)) {
            Logger.getInstance().createLogfile(logfileName.append("_finished").toString());
        }

        lastGameControlData = e.data;

        // Log the GameController data
        if (e.data != null || changed != 0) {
            Logger.getInstance().log(e.data);
        }

        // send events
        sendEvents(changed);
    }

    /**
     * Updates info about the game when no message was received from the
     * GameController.
     *
     * @param e event
     */
    @Override
    public void gameControlDataTimeout(final GameControlDataTimeoutEvent e) {
        if (LogReplayer.getInstance().isReplaying()) {
            return;
        }

        int changed = 0;

        if (lastGameControlData != null) {
            synchronized (teamNumbers) {
                teamNumbers[TEAM_LEFT] = 0;
                teamNumbers[TEAM_RIGHT] = 0;
                int s = 0;
                for (final Entry<Integer, Collection<RobotState>> entry : robots.entrySet()) {
                    if (!entry.getValue().isEmpty()) {
                        teamNumbers[s++] = entry.getKey();
                        if (s == 2) {
                            break;
                        }
                    }
                }
            }
            changed = CHANGED_LEFT | CHANGED_RIGHT | CHANGED_OTHER;
            Logger.getInstance().createLogfile();
        }
        lastGameControlData = null;

        // send events
        sendEvents(changed);
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
        int changed = 0;

        // update the team info if no GameController info is available
        if (lastGameControlData == null) {
            synchronized (teamNumbers) {
                boolean exists = false;
                for (int i = 0; i < 2; i++) {
                    if (teamNumbers[i] == teamNumber) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    for (int i = 0; i < 2; i++) {
                        if (teamNumbers[i] == 0) {
                            teamNumbers[i] = teamNumber;

                            // (re)load plugins
                            PluginLoader.getInstance().update(teamNumber);

                            changed |= (i + 1) | CHANGED_OTHER;
                            break;
                        }
                    }
                }
            }
        }

        // create the robot state if it does not yet exist
        RobotState r;
        synchronized (robotsByAddress) {
            r = robotsByAddress.get(address);
            if (r == null) {
                r = new RobotState(address, teamNumber);

                robotsByAddress.put(address, r);
            }

            Collection<RobotState> set = robots.get(teamNumber);
            if (set == null) {
                set = new HashSet<>();
                robots.put(teamNumber, set);
            }
            if (set.add(r)) {
                if (teamNumbers[TEAM_LEFT] == teamNumber) {
                    changed |= CHANGED_LEFT;
                } else if (teamNumbers[TEAM_RIGHT] == teamNumber) {
                    changed |= CHANGED_RIGHT;
                }
            }
        }

        // let the robot state handle the message
        r.registerMessage(message);

        // send events
        sendEvents(changed);
    }

    private void sendEvents(final int changed) {
        boolean leftSent = false;
        boolean rightSent = false;

        if ((changed & CHANGED_OTHER) != 0) {
            final List<RobotState> rs = new ArrayList<>();
            synchronized (robotsByAddress) {
                for (final Entry<Integer, Collection<RobotState>> entry : robots.entrySet()) {
                    if (entry.getKey() == teamNumbers[TEAM_LEFT]) {
                        if ((changed & CHANGED_LEFT) != 0) {
                            final List<RobotState> list = new ArrayList<>();
                            for (final RobotState r : entry.getValue()) {
                                list.add(r);
                            }
                            list.sort(playerNumberComparator);
                            fireEvent(new TeamEvent(this, outputSide(TEAM_LEFT), teamNumbers[TEAM_LEFT], list));
                            leftSent = true;
                        }
                    } else if (entry.getKey() == teamNumbers[TEAM_RIGHT]) {
                        if ((changed & CHANGED_RIGHT) != 0) {
                            final List<RobotState> list = new ArrayList<>();
                            for (final RobotState r : entry.getValue()) {
                                list.add(r);
                            }
                            list.sort(playerNumberComparator);
                            fireEvent(new TeamEvent(this, outputSide(TEAM_RIGHT), teamNumbers[TEAM_RIGHT], list));
                            rightSent = true;
                        }
                    } else {
                        for (final RobotState r : entry.getValue()) {
                            rs.add(r);
                        }
                    }
                }
            }
            rs.sort(playerNumberComparator);
            fireEvent(new TeamEvent(this, TEAM_OTHER, 0, rs));
        }

        if (!leftSent && (changed & CHANGED_LEFT) != 0) {
            final Collection<RobotState> rs;
            synchronized (robotsByAddress) {
                rs = robots.get(teamNumbers[TEAM_LEFT]);
            }
            final List<RobotState> list = new ArrayList<>();
            if (rs != null) {
                for (final RobotState r : rs) {
                    list.add(r);
                }
                list.sort(playerNumberComparator);
            }
            fireEvent(new TeamEvent(this, outputSide(TEAM_LEFT), teamNumbers[TEAM_LEFT], list));
        }

        if (!rightSent && (changed & CHANGED_RIGHT) != 0) {
            final Collection<RobotState> rs;
            synchronized (robotsByAddress) {
                rs = robots.get(teamNumbers[TEAM_RIGHT]);
            }
            final List<RobotState> list = new ArrayList<>();
            if (rs != null) {
                for (final RobotState r : rs) {
                    list.add(r);
                }
                list.sort(playerNumberComparator);
            }
            fireEvent(new TeamEvent(this, outputSide(TEAM_RIGHT), teamNumbers[TEAM_RIGHT], list));
        }
    }

    private void fireEvent(final TeamEvent e) {
        for (final TeamEventListener listener : listeners.getListeners(TeamEventListener.class)) {
            listener.teamChanged(e);
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
            if (Rules.league == Rules.getLeagueRules(SPL.class) && teamNumber >= 90 && teamNumber < 100) {
                Rules.league = Rules.getLeagueRules(SPLMixedTeam.class);
                try {
                    colorStrings = Teams.getColors(teamNumber);
                } catch (final NullPointerException | ArrayIndexOutOfBoundsException e) {
                } finally {
                    Rules.league = Rules.getLeagueRules(SPL.class);
                }
            } else {
                try {
                    colorStrings = Teams.getColors(teamNumber);
                } catch (final NullPointerException | ArrayIndexOutOfBoundsException e) {
                }
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
            } else if (colorStrings[0].equals("black")) {
                return GameControlData.TEAM_BLACK;
            } else if (colorStrings[0].equals("green")) {
                return GameControlData.TEAM_GREEN;
            } else if (colorStrings[0].equals("orange")) {
                return GameControlData.TEAM_ORANGE;
            } else if (colorStrings[0].equals("purple")) {
                return GameControlData.TEAM_PURPLE;
            } else if (colorStrings[0].equals("brown")) {
                return GameControlData.TEAM_BROWN;
            } else if (colorStrings[0].equals("gray")) {
                return GameControlData.TEAM_GRAY;
            } else {
                return GameControlData.TEAM_WHITE;
            }
        }

        return color;
    }

    /**
     * Returns the most recently received GameControlData.
     *
     * @return GameControlData of null if none was received recently
     */
    public GameControlData getLastGameControlData() {
        return lastGameControlData;
    }

    /**
     * Returns the team name of the given team.
     *
     * @param teamNumber number of the team
     * @param withNumber whether the team number should be in the returned
     * string
     * @param withPrefix whether the pre- or suffix "Team" should be included
     * @return the team name
     */
    public String getTeamName(final Integer teamNumber, final boolean withNumber, final boolean withPrefix) {
        final String[] teamNames;
        if (Rules.league == Rules.getLeagueRules(SPL.class) && teamNumber >= 90 && teamNumber < 100) {
            Rules.league = Rules.getLeagueRules(SPLMixedTeam.class);
            try {
                teamNames = Teams.getNames(withNumber);
            } catch (final NullPointerException | ArrayIndexOutOfBoundsException e) {
                return null;
            } finally {
                Rules.league = Rules.getLeagueRules(SPL.class);
            }
        } else {
            try {
                teamNames = Teams.getNames(withNumber);
            } catch (final NullPointerException | ArrayIndexOutOfBoundsException e) {
                return null;
            }
        }
        if (teamNumber != null) {
            if (teamNumber < teamNames.length && teamNames[teamNumber] != null) {
                return ((withPrefix ? "Team " : "") + teamNames[teamNumber]);
            } else {
                return ("Unknown" + (withPrefix ? " Team" : "") + (withNumber ? " (" + teamNumber + ")" : ""));
            }
        } else {
            return "Unknown" + (withPrefix ? " Team" : "");
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
        if (mirrored != this.mirrored) {
            this.mirrored = mirrored;
            sendEvents(CHANGED_LEFT | CHANGED_RIGHT);
        }
    }

    private int outputSide(final int side) {
        return mirrored ? (side == 0 ? 1 : (side == 1 ? 0 : side)) : side;
    }

    /**
     * Registers a GUI component as a listener receiving events about team
     * changes.
     *
     * @param listener component
     */
    public void addListener(final TeamEventListener listener) {
        listeners.add(TeamEventListener.class, listener);
        sendEvents(CHANGED_LEFT | CHANGED_RIGHT | CHANGED_OTHER);
    }

    /**
     * Unregisters a GUI component as a listener receiving events about team
     * changes.
     *
     * @param listener component
     */
    public void removeListener(final TeamEventListener listener) {
        listeners.remove(TeamEventListener.class, listener);
    }

}
