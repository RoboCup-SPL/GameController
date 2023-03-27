package bhuman.drawings;

import bhuman.message.BHumanMessage;
import com.jogamp.opengl.GL2;
import data.PlayerInfo;
import data.Rules;
import data.SPL;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import teamcomm.data.RobotState;
import teamcomm.gui.Camera;
import teamcomm.gui.drawings.Image;
import teamcomm.gui.drawings.PerPlayer;
import teamcomm.gui.drawings.TextureLoader;

/**
 * Custom drawing for visualizing when a whistle was heard.
 *
 * @author Felix Thielke
 */
public class WhistleHeard extends PerPlayer {

    @Override
    public void draw(final GL2 gl, final RobotState rs, final Camera camera) {
        if (rs.getLastTeamMessage() != null
                && rs.getLastTeamMessage().valid
                && rs.getLastTeamMessage() instanceof BHumanMessage) {
            final BHumanMessage msg = (BHumanMessage) rs.getLastTeamMessage();
            if (msg.theWhistle.confidenceOfLastWhistleDetection > 0
                    && msg.theWhistle.channelsUsedForWhistleDetection > 0
                    && msg.theFrameInfo.time.getTimeSince(msg.theWhistle.lastTimeWhistleDetected.timestamp) <= 1100) {
                gl.glPushMatrix();

                if (rs.getPenalty() != PlayerInfo.PENALTY_NONE && !(Rules.league instanceof SPL && rs.getPenalty() == PlayerInfo.PENALTY_SPL_ILLEGAL_MOTION_IN_SET)) {
                    gl.glTranslatef(-msg.playerNumber, -3.5f, 1.f);
                } else {
                    gl.glTranslatef(msg.theRobotPose.translation.x / 1000.f, msg.theRobotPose.translation.y / 1000.f, 1.f);
                }

                camera.turnTowardsCamera(gl);
                try {
                    final File f = new File("plugins/" + (rs.getTeamNumber() < 10 ? "0" + rs.getTeamNumber() : String.valueOf(rs.getTeamNumber())) + "/resources/whistle.png").getAbsoluteFile();
                    Image.drawImage(gl, TextureLoader.getInstance().loadTexture(gl, f), 0, 0, 0.2f);
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

    @Override
    public boolean hasAlpha() {
        return true;
    }

    @Override
    public int getPriority() {
        return 8;
    }

}
