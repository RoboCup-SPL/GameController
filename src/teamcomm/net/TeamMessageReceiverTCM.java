package teamcomm.net;

import common.Log;
import common.net.TeamMessagePackage;
import common.net.TeamMessageReceiver;
import data.TeamMessage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import javax.swing.JOptionPane;
import teamcomm.PluginLoader;
import teamcomm.data.AdvancedMessage;
import teamcomm.data.GameState;
import teamcomm.net.logging.LogReplayer;

/**
 * Singleton class for the thread which handles messages from the robots. It
 * spawns one thread for listening on each team port up to team number 100 and
 * processes the messages received by these threads.
 *
 * @author Felix Thielke
 */
public class TeamMessageReceiverTCM extends TeamMessageReceiver {

    private static TeamMessageReceiverTCM instance;

    public TeamMessageReceiverTCM(final boolean multicast) throws IOException {
        super(multicast, null);
    }

    /**
     * Creates the only instance of the TeamMessageReceiver.
     * @param multicast Should it also listen to multicast packets? This also means
     *                  that ip adresses are computed based on the player number.
     *
     * @return instance
     */
    public static TeamMessageReceiverTCM createInstance(final boolean multicast) {
        try {
            instance = new TeamMessageReceiverTCM(multicast);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error while setting up packet listeners: " + ex.getMessage(),
                    "IOException",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
        return instance;
    }

    /**
     * Returns the only instance of the TeamMessageReceiver.
     *
     * @return instance
     */
    public static TeamMessageReceiverTCM getInstance() {
        return instance;
    }

    @Override
    protected boolean processPackets() {
        return !LogReplayer.getInstance().isReplaying();
    }

    @Override
    protected void handleMessage(final TeamMessagePackage p) {
        final TeamMessage message;
        final Class<? extends TeamMessage> c = PluginLoader.getInstance().getMessageClass(p.team);

        try {
            message = c.getDeclaredConstructor().newInstance();
            message.fromByteArray(ByteBuffer.wrap(p.message));

            TeamMessage m = message;
            if (message instanceof AdvancedMessage) {
                if (message.valid) {
                    try {
                        ((AdvancedMessage) message).init();
                    } catch (final Throwable e) {
                        m = TeamMessage.createFrom(message);
                        Log.error(e.getClass().getSimpleName() + " was thrown while initializing custom message class " + c.getSimpleName() + ": " + e.getMessage());
                    }
                } else {
                    m = TeamMessage.createFrom(message);
                }
            }

            GameState.getInstance().receiveMessage(p.host, p.team, m);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            Log.error("a problem occured while instantiating custom message class " + c.getSimpleName() + ": " + ex.getMessage());
        }
    }

}
