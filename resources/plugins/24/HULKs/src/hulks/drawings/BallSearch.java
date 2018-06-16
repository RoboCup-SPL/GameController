package hulks.drawings;

import com.jogamp.opengl.GL2;
import hulks.message.HulksMessage;
import hulks.message.data.Eigen;
import hulks.message.data.SearchPosition;
import teamcomm.data.RobotState;
import teamcomm.gui.Camera;
import teamcomm.gui.drawings.Image;
import teamcomm.gui.drawings.PerPlayer;
import teamcomm.gui.drawings.Text;
import teamcomm.gui.drawings.TextureLoader;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom drawing for visualizing the current performing role.
 *
 * @author Georg Felbinger
 */
public class BallSearch extends PerPlayer {

    private BallSearchState state = new BallSearchState();

    @Override
    public void draw(final GL2 gl, final RobotState rs, final Camera camera) {
        if (rs.getLastMessage() != null
                && rs.getLastMessage().valid
                && rs.getLastMessage() instanceof HulksMessage) {
            final HulksMessage msg = (HulksMessage) rs.getLastMessage();
            if (msg.message.bhulks != null && msg.message.hulks != null && msg.message.hulks.isValid()) {
                state.update(msg);
                for (final SearchPosition searchPosition : state.getCurrentSearchPoses()) {
                    Eigen.Vector2f position = searchPosition.getPosition();
                    gl.glPushMatrix();
                    gl.glTranslatef(position.x, position.y, 0.1f);
                    camera.turnTowardsCamera(gl);
                    try {
                        final File f = new File("plugins/" + (rs.getTeamNumber() < 10 ? "0" + rs.getTeamNumber() : String.valueOf(rs.getTeamNumber())) + "/resources/" + "search.png").getAbsoluteFile();
                        Image.drawImage(gl, TextureLoader.getInstance().loadTexture(gl, f), 0, 0, 0.2f);
                        Text.drawText("" + searchPosition.getPlayer(), 0.1f, 0.1f, 0.15f, new float[]{0, 0, 0});
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null,
                                "Error loading texture: " + ex.getMessage(),
                                ex.getClass().getSimpleName(),
                                JOptionPane.ERROR_MESSAGE);
                    }
                    gl.glPopMatrix();
                }
            }
        }
    }

    @Override
    public boolean hasAlpha() {
        return true;
    }

    @Override
    public int getPriority() {
        return 8;
    }

    private static final class BallSearchState {
        private LocalDateTime lastUpdate = LocalDateTime.MIN;
        private int leader = -1;
        private int searcher = -1;
        private List<SearchPosition> currentSearchPoses = new ArrayList<>();

        void update(final HulksMessage message) {
            // Reset leader after 3 Seconds
            if (lastUpdate.plusSeconds(3).isAfter(LocalDateTime.now())) {
                leader = -1;
                searcher = -1;
            }
            // Leader election: Smallest PlayerNumber
            if (leader == -1 || leader <= message.playerNum) {
                lastUpdate = LocalDateTime.now();
                leader = message.playerNum;
                searcher = message.message.hulks.getMostWisePlayerNumber();
            }
            // Update Search Positions
            if (searcher == message.playerNum) {
                final Eigen.Vector2f currentSearchPos = message.message.hulks.getCurrentSearchPosition();
                currentSearchPoses = message.message.hulks.getSearchPositionSuggestions();
            }
        }

        List<SearchPosition> getCurrentSearchPoses() {
            return currentSearchPoses;
        }
    }
}
