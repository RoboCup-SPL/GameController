package teamcomm.net;

import common.net.GameControlReturnDataPackage;
import common.net.GameControlReturnDataReceiver;
import data.GameControlReturnData;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.swing.JOptionPane;
import teamcomm.data.GameState;
import teamcomm.net.logging.LogReplayer;

/**
 * Singleton class for the thread which handles GameController return messages from the robots.
 *
 * @author Felix Thielke
 * @author Arne Hasselbring
 */
public class GameControlReturnDataReceiverTCM extends GameControlReturnDataReceiver {

    private static GameControlReturnDataReceiverTCM instance;

    private GameControlReturnDataReceiverTCM() throws IOException {
        super();
    }

    /**
     * Returns the only instance of the GameControlReturnDataReceiver.
     *
     * @return instance
     */
    public static GameControlReturnDataReceiverTCM getInstance() {
        if (instance == null) {
            try {
                instance = new GameControlReturnDataReceiverTCM();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null,
                        "Error while setting up packet listeners: " + ex.getMessage(),
                        "IOException",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(-1);
            }
        }
        return instance;
    }

    @Override
    protected boolean processPackets() {
        return !LogReplayer.getInstance().isReplaying();
    }

    @Override
    protected void handleMessage(final GameControlReturnDataPackage p) {
        final GameControlReturnData message = new GameControlReturnData();
        message.fromByteArray(ByteBuffer.wrap(p.message));
        if (!(message.headerValid && message.versionValid && message.playerNumValid && message.teamNumValid)) {
            return;
        }

        GameState.getInstance().receiveMessage(p.host, message);
    }

}
