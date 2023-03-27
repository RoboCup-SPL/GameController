package teamcomm.net;

import common.Log;
import common.net.SPLTeamMessagePackage;
import common.net.SPLTeamMessageReceiver;
import data.SPLTeamMessage;
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
public class SPLTeamMessageReceiverTCM extends SPLTeamMessageReceiver {

    private static SPLTeamMessageReceiverTCM instance;

    public SPLTeamMessageReceiverTCM(final boolean multicast) throws IOException {
        super(multicast, null);
    }

    /**
     * Creates the only instance of the SPLTeamMessageReceiver.
     * @param multicast Should it also listen to multicast packets? This also means
     *                  that ip adresses are computed based on the player number.
     *
     * @return instance
     */
    public static SPLTeamMessageReceiverTCM createInstance(final boolean multicast) {
        try {
            instance = new SPLTeamMessageReceiverTCM(multicast);
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
     * Returns the only instance of the SPLTeamMessageReceiver.
     *
     * @return instance
     */
    public static SPLTeamMessageReceiverTCM getInstance() {
        return instance;
    }

    @Override
    protected boolean processPackets() {
        return !LogReplayer.getInstance().isReplaying();
    }

    @Override
    protected void handleMessage(final SPLTeamMessagePackage p) {
        final SPLTeamMessage message;
        final Class<? extends SPLTeamMessage> c = PluginLoader.getInstance().getMessageClass(p.team);

        try {
            message = c.getDeclaredConstructor().newInstance();
            message.fromByteArray(ByteBuffer.wrap(p.message));

            SPLTeamMessage m = message;
            if (message instanceof AdvancedMessage) {
                if (message.valid) {
                    try {
                        ((AdvancedMessage) message).init();
                    } catch (final Throwable e) {
                        m = SPLTeamMessage.createFrom(message);
                        Log.error(e.getClass().getSimpleName() + " was thrown while initializing custom message class " + c.getSimpleName() + ": " + e.getMessage());
                    }
                } else {
                    m = SPLTeamMessage.createFrom(message);
                }
            }

            GameState.getInstance().receiveMessage(p.host, p.team, m);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            Log.error("a problem occured while instantiating custom message class " + c.getSimpleName() + ": " + ex.getMessage());
        }
    }

}
