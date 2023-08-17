package eventrecorder;

import java.net.SocketException;
import java.text.SimpleDateFormat;

import javax.swing.JOptionPane;

import data.GameControlData;
import data.PlayerInfo;
import data.TeamInfo;
import eventrecorder.action.ActionHistory;
import eventrecorder.action.EntryCreateAction;
import eventrecorder.gui.MainFrame;
import eventrecorder.data.DataModel;
import eventrecorder.data.LogEntry;
import eventrecorder.data.LogType;
import teamcomm.data.event.GameControlDataEvent;
import teamcomm.data.event.GameControlDataEventListener;
import teamcomm.data.event.GameControlDataTimeoutEvent;
import teamcomm.net.GameControlDataReceiver;

/**
 * This is a little tool to record events while a game takes place.
 *
 * @author Andre Muehlenbrock
 */

public class EventRecorder {
    public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd.MM.YY - HH:mm 'Uhr'");
    public static final SimpleDateFormat SECONDS_FORMAT = new SimpleDateFormat("mm:ss");
    public static final String[] GAME_STATE_NAMES = new String[]{"Initial", "Ready", "Set", "Playing", "Finished"};

    public static final int GAMECONTROLLER_TIMEOUT = 3000; /**< Timeout in ms when the manual play/stop/reset buttons should be usable again */

    public static DataModel model;
    public static ActionHistory history;
    public static MainFrame gui;

    public static GameControlDataReceiver gcDataReceiver;

    private static GameControlData lastData = null;
    private static TeamInfo[] lastTeamData = null;
    private static byte lastGameState = -1;
    private static byte lastSetPlay = -1;
    private static byte lastKickingTeam = -1;

    private static final boolean[] logPenalty = new boolean[16];
    private static boolean logFreeKicks = true;

    public TeamInfo[] getLastTeamData() {
        return lastTeamData;
    }

    public static void main(String[] args){
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        model = new DataModel();
        history = new ActionHistory();
        gui = new MainFrame();

        // Initialize listener for GameController messages
        try {
            gcDataReceiver = new GameControlDataReceiver(true);
        } catch (SocketException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error while setting up GameController listener.",
                    "SocketException",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

        // Add this as GameControlDataListener:
        gcDataReceiver.addListener(new GameControlDataEventListener(){

            @Override
            public void gameControlDataChanged(GameControlDataEvent e) {
                EventRecorder.updateGameData(e.data);
            }

            @Override
            public void gameControlDataTimeout(GameControlDataTimeoutEvent e) {
                EventRecorder.updateGameData(null);
            }

        });

        // Start listening:
        gcDataReceiver.start();
    }


    public static void updateGameData(GameControlData data) {
        if (data != lastData && data != null) {
            // Deactivate manually running:
            model.isManuallyRunning = false;

            // Set current time:
            model.currentTime = data.secsRemaining;

            //Log goals
            if (lastData != null && data.gameState != GameControlData.STATE_INITIAL &&
                (lastData.team[0].score != data.team[0].score || lastData.team[1].score != data.team[1].score)) {

                // Insert before empty logEntries:
                int insertPlace = EventRecorder.model.logEntries.size();

                while (insertPlace > 0 && "".equals(EventRecorder.model.logEntries.get(insertPlace - 1).text)) {
                    --insertPlace;
                }

                history.execute(new EntryCreateAction(new LogEntry(capitalize(TeamInfo.getTeamColorName(data.team[0].fieldPlayerColor)) + " " + data.team[0].score + " : " + data.team[1].score + " " + capitalize(TeamInfo.getTeamColorName(data.team[1].fieldPlayerColor)), SECONDS_FORMAT.format(data.secsRemaining * 1000), LogType.Manually), insertPlace, false));
            }

            // If Gamestate is changed, add a LogEntry:
            if (lastGameState != data.gameState) {
                lastGameState = data.gameState;

                String gameStateString = GAME_STATE_NAMES[data.gameState];
                if(data.gameState == GameControlData.STATE_INITIAL){
                    gameStateString += " ( "+ (data.firstHalf == GameControlData.C_TRUE? "First Half":"Second Half")+" )";
                }

                // Insert before empty logEntries:
                int insertPlace = EventRecorder.model.logEntries.size();

                while (insertPlace > 0 && "".equals(EventRecorder.model.logEntries.get(insertPlace - 1).text)) {
                    --insertPlace;
                }

                history.execute(new EntryCreateAction(new LogEntry(gameStateString, SECONDS_FORMAT.format(data.secsRemaining * 1000),LogType.GameState), insertPlace, false));
            }

            //Log Free Kicks
            if (logFreeKicks && (lastSetPlay != data.setPlay || lastKickingTeam != data.kickingTeam)) {
                if (data.setPlay == GameControlData.SET_PLAY_NONE) {
                    String setPlayString;
                    switch (lastSetPlay) {
                        case GameControlData.SET_PLAY_GOAL_KICK:
                            setPlayString = "Goal Kick Complete";
                            break;
                        case GameControlData.SET_PLAY_PUSHING_FREE_KICK:
                            setPlayString = "Pushing Free Kick Complete";
                            break;
                        case GameControlData.SET_PLAY_CORNER_KICK:
                            setPlayString = "Corner Kick Complete";
                            break;
                        case GameControlData.SET_PLAY_KICK_IN:
                            setPlayString = "Kick In Complete";
                            break;
                        case GameControlData.SET_PLAY_PENALTY_KICK:
                            setPlayString = "Penalty Kick Complete";
                            break;
                        default:
                            setPlayString = "";
                            break;
                    }

                    // Insert before empty logEntries:
                    int insertPlace = EventRecorder.model.logEntries.size();

                    while (insertPlace > 0 && "".equals(EventRecorder.model.logEntries.get(insertPlace-1).text)) {
                        --insertPlace;
                    }

                    if (!setPlayString.isEmpty()) {
                        history.execute(new EntryCreateAction(new LogEntry(setPlayString, SECONDS_FORMAT.format(data.secsRemaining * 1000), LogType.SetPlayState), insertPlace, false));
                    }
                } else {
                    String setPlayString;
                    switch (data.setPlay) {
                        case GameControlData.SET_PLAY_GOAL_KICK:
                            setPlayString = "Goal Kick for team: ";
                            break;
                        case GameControlData.SET_PLAY_PUSHING_FREE_KICK:
                            setPlayString = "Pushing Free Kick for team: ";
                            break;
                        case GameControlData.SET_PLAY_CORNER_KICK:
                            setPlayString = "Corner Kick for team: ";
                            break;
                        case GameControlData.SET_PLAY_KICK_IN:
                            setPlayString = "Kick In for team: ";
                            break;
                        case GameControlData.SET_PLAY_PENALTY_KICK:
                            setPlayString = "Penalty Kick for team: ";
                            break;
                        default:
                            setPlayString = "";
                            break;
                    }
                    setPlayString += Byte.toString(data.kickingTeam);

                    // Insert before empty logEntries:
                    int insertPlace = EventRecorder.model.logEntries.size();

                    while (insertPlace > 0 && "".equals(EventRecorder.model.logEntries.get(insertPlace - 1).text)) {
                        --insertPlace;
                    }

                    history.execute(new EntryCreateAction(new LogEntry(setPlayString, SECONDS_FORMAT.format(data.secsRemaining * 1000), LogType.SetPlayState), insertPlace, false));
                }
                lastSetPlay = data.setPlay;
                lastKickingTeam = data.kickingTeam;
            }

            // Check for changed penalties:
            if (lastTeamData != null) {
                for (int i = 0; i < data.team.length && i < lastTeamData.length; ++i) {
                    for (int p = 0; p < data.team[i].player.length && p < lastTeamData[i].player.length; ++p) {
                        if (data.team[i].player[p].penalty != lastTeamData[i].player[p].penalty
                                && logPenalty[data.team[i].player[p].penalty]) {
                            String penaltyString = data.team[i].player[p].penalty == 0 ? "Back In Game" :
                                capitalize(PlayerInfo.getPenaltyName(data.team[i].player[p].penalty));
                            String totalString = TeamInfo.getTeamColorName(data.team[i].fieldPlayerColor).toUpperCase() + " " + (p + 1) + ": " +
                                    penaltyString;

                            // Insert before empty logEntries:
                            int insertPlace = EventRecorder.model.logEntries.size();

                            while (insertPlace > 0 && "".equals(EventRecorder.model.logEntries.get(insertPlace - 1).text)) {
                                --insertPlace;
                            }

                            history.execute(new EntryCreateAction(new LogEntry(totalString, SECONDS_FORMAT.format(data.secsRemaining * 1000),LogType.PlayerState), insertPlace, false));
                        }
                    }
                }
            }

            lastTeamData = data.team;

            // Save current timestamp:
            model.lastGameControllerInfo = System.currentTimeMillis();
        }

        lastData = data;
    }

    public static String capitalize(String string) {
        StringBuilder result = new StringBuilder();

        String[] array = string.split(" ");
        for (String s : array) {
            char[] charArray = s.trim().toCharArray();
            charArray[0] = Character.toUpperCase(charArray[0]);
            s = new String(charArray);
            result.append(s).append(" ");
        }

        return result.toString().trim();
    }

    public static void cleanExit() {
        gcDataReceiver.interrupt();

        // Try to join receiver threads
        try {
            gcDataReceiver.join(1000);
        } catch (InterruptedException ex) {

        }

        System.exit(0);
    }

    public static void setLogPenalty(int i, boolean log) {
        logPenalty[i] = log;
    }

    public static void setLogFreeKicks(boolean log) {
        logFreeKicks = log;
    }
}
