package common;

import data.AdvancedData;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;


/**
 * @author Michel Bartsch
 *
 * This class should be used to log into a log file. A new file will be created
 * every time the GameController is started.
 * At the end of an actions the Log should be used to add a state into the
 * timeline, that is provided by this class too.
 *
 * This class is a singleton!
 */
public class Log
{
    /** The instance of the singleton. */
    private static final Log instance = new Log();

    /** The file to write into. */
    private FileWriter file;
    /** The error-file to write into. */
    private FileWriter errorFile;
    /** The file to write into. */
    private final String errorPath = "error.txt";
    /** The timeline. */
    private final LinkedList<AdvancedData> states = new LinkedList<>();
    /** If != null, the next log entry will use this message. */
    private String message = null;

    /** The format of timestamps. */
    public static final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy.M.dd-kk.mm.ss");

    /**
     * Creates a new Log.
     */
    private Log() {}

    /**
     * Must be called once at the very beginning to allow Log to work.
     *
     * @param path  The path where the log-file should be created.
     */
    public synchronized static void init(String path)
    {
        if (instance.file != null) {
            throw new IllegalStateException("logger already initialized");
        }
        try{
            instance.file = new FileWriter(path);
        } catch (IOException e) {
            error("cannot write to logfile "+path);
        }

        // Write version number if application is GameController
        try {
            toFile((String)Class.forName("controller.GameController").getField("version").get(null));
        } catch (ClassNotFoundException
                 | NoSuchFieldException
                 | SecurityException
                 | IllegalArgumentException
                 | IllegalAccessException ex) {
        }
    }

    /**
     * Simply writes a line, beginning with a timestamp, in the file.
     * May be used to log something that should not be in the timeline.
     *
     * @param s     The string to be written in the file.
     */
    public static void toFile(String s)
    {
        try{
            instance.file.write(timestampFormat.format(new Date(System.currentTimeMillis()))+": "+s+"\n");
            instance.file.flush();
        } catch (IOException e) {
            error("cannot write to logfile!");
        }
    }

    /**
     * Specify the message that will be used for the next log entry. It will
     * replace the one that is specified during that log entry. This allows
     * to replace rather generic messages by more specific ones if an
     * action calls another action to perform its task.
     * @param message The message that will be used for the next log entry.
     */
    public static void setNextMessage(String message)
    {
        instance.message = message;
    }

    /**
     * Puts a copy of the given data into the timeline, attaching the message
     * to it and writing it to the file using toFile method.
     * This should be used at the very end of all actions that are meant to be
     * in the timeline.
     *
     * @param data  The current data that have just been changed and should
     *              go into the timeline.
     * @param message   A message describing what happened to the data.
     */
    public static void state(AdvancedData data, String message)
    {
        AdvancedData state = (AdvancedData) data.clone();
        if (!instance.states.isEmpty()) {
            state.timeSinceCurrentGameStateBegan = state.getTime() - instance.states.getLast().whenCurrentGameStateBegan;
            state.timeSinceCurrentSetPlayBegan = state.getTime() - instance.states.getLast().whenCurrentSetPlayBegan;
        }
        if (instance.message == null) {
            state.message = message;
        } else {
            state.message = instance.message;
            toFile(state.message);
            instance.message = null;
        }
        instance.states.add(state);
        toFile(message);
    }

    /**
     * Changes the data used in all actions via the EventHandler to a data from
     * the timeline. So this is the undo function.
     * If a game state change is undone, the time when it was left is restored.
     * Thereby, there whole remaining log is moved into the new timeframe.
     *
     * @param data      The current data. Only used to determine whether
     *                  a change of game state is reverted.
     * @param states    How far you want to go back, how many states.
     *
     * @return The message that was attached to the data you went back to.
     */
    public static String goBack(AdvancedData data, int states)
    {
        if (states >= instance.states.size()) {
            states = instance.states.size()-1;
        }
        long timeSinceCurrentGameStateBegan = 0;
        long nextTimeSinceCurrentGameStateBegan = 0;
        long timeSinceCurrentSetPlayBegan = 0;

        boolean gameStateChanged = false;
        byte nextGameState = data.gameState;
        for (int i=0; i<states; i++) {
            final AdvancedData lastState = instance.states.getLast();
            if (lastState.gameState != nextGameState) {
                gameStateChanged = true;
                nextGameState = lastState.gameState;
                timeSinceCurrentGameStateBegan = nextTimeSinceCurrentGameStateBegan;
            }
            if (lastState.setPlay == AdvancedData.SET_PLAY_NONE) {
                timeSinceCurrentSetPlayBegan = lastState.timeSinceCurrentSetPlayBegan;
            }
            nextTimeSinceCurrentGameStateBegan = lastState.timeSinceCurrentGameStateBegan;
            instance.states.removeLast();
        }
        final AdvancedData lastState = instance.states.getLast();
        if (lastState.gameState != nextGameState) {
            gameStateChanged = true;
            timeSinceCurrentGameStateBegan = nextTimeSinceCurrentGameStateBegan;
        }
        if (gameStateChanged) {
            long timeOffset = data.getTime() - timeSinceCurrentGameStateBegan
                    - lastState.whenCurrentGameStateBegan;
            for (AdvancedData state : instance.states) {
                state.whenCurrentGameStateBegan += timeOffset;
                for (int i = 0; i < state.whenPenalized.length; i++) {
                    for (int j = 0; j < data.whenPenalized[i].length; j++) {
                        if (state.whenPenalized[i][j] != 0) {
                            state.whenPenalized[i][j] += timeOffset;
                        }
                    }
                }
            }
        }
        if (lastState.setPlay != AdvancedData.SET_PLAY_NONE && timeSinceCurrentSetPlayBegan != 0) {
            long timeOffset = data.getTime() - timeSinceCurrentSetPlayBegan
                    - lastState.whenCurrentSetPlayBegan;
            for (AdvancedData state : instance.states) {
                state.whenCurrentSetPlayBegan += timeOffset;
            }
        }
        AdvancedData state = (AdvancedData) lastState.clone();

        // Do not undo states that have to do with the team message budget.
        for (int i=0; i<2; ++i) {
            state.team[i].messageBudget = data.team[i].messageBudget;
            state.sentIllegalMessages[i] = data.sentIllegalMessages[i];
        }

        // Write state to EventHandler if application is GameController
        try {
            final Class<?> eventHandlerClass = Class.forName("controller.EventHandler");
            eventHandlerClass.getField("data").set(eventHandlerClass.getMethod("getInstance").invoke(null), state);
        } catch (ClassNotFoundException
                 | NoSuchFieldException
                 | SecurityException
                 | IllegalArgumentException
                 | IllegalAccessException
                 | NoSuchMethodException
                 | InvocationTargetException ex) {
        }
        return state.message;
    }

    /**
     * Gives you the messages attached to the latest data in the timeline.
     *
     * @param states    Of how many datas back you want to have the messages.
     *
     * @return The messages attached to the data, beginning with the latest.
     *         The arrays length equals the states parameter.
     */
    public static String[] getLast(int states)
    {
        String[] out = new String[states];
        for (int i=0; i<states; i++) {
            if (instance.states.size()-1-i >= 0) {
                out[i] = instance.states.get(instance.states.size()-1-i).message;
            } else {
                out[i] = "";
            }
        }
        return out;
    }

    /**
     * Writes a line, beginning with a timestamp, in the error-file and creates
     * a new one, if it does not yet exist.
     *
     * This can be used before initialising the log!
     *
     * @param s     The string to be written in the error-file.
     */
    public static void error(String s)
    {
        System.err.println(s);
        try{
            if (instance.errorFile == null) {
                instance.errorFile = new FileWriter(instance.errorPath);
            }
            instance.errorFile.write(timestampFormat.format(new Date(System.currentTimeMillis()))+": "+s+"\n");
            instance.errorFile.flush();
        } catch (IOException e) {
             System.err.println("cannot write to error file!");
        }
    }

    /**
     * Closes the Log
     *
     * @throws IOException if an error occurred while trying to close the FileWriters
     */
    public static void close() throws IOException {
        if (instance.errorFile != null) {
            instance.errorFile.close();
        }
        instance.file.close();
    }
}
