package hulks.drawings;

import com.jogamp.opengl.GL2;
import common.Log;
import hulks.message.BHULKsStandardMessage;
import hulks.message.HulksMessage;
import hulks.message.data.Eigen;
import teamcomm.data.RobotState;
import teamcomm.gui.Camera;
import teamcomm.gui.drawings.Image;
import teamcomm.gui.drawings.PerPlayer;
import teamcomm.gui.drawings.Text;
import teamcomm.gui.drawings.TextureLoader;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * Custom drawing for visualizing the current performing role.
 *
 * @author Georg Felbinger
 */
public class BallSearch extends PerPlayer {

    @Override
    public void draw(final GL2 gl, final RobotState rs, final Camera camera) {
        if (rs.getLastMessage() != null
                && rs.getLastMessage().valid
                && rs.getLastMessage() instanceof HulksMessage) {
            final HulksMessage msg = (HulksMessage) rs.getLastMessage();
            if (msg.message.bhulks != null && msg.message.hulks != null && msg.message.hulks.isValid()) {
                if (msg.message.bhulks.currentlyPerfomingRole == BHULKsStandardMessage.Role.King) {
                    int playerNumber=1;
                    for (final Eigen.Vector2f searchPose : msg.message.hulks.getPositionSuggestions()) {
                        if (searchPose.x == -0.5f && searchPose.y == 0.f) {
                            continue; // TODO: Remove after GO 2018
                        }
                        gl.glPushMatrix();
                        gl.glTranslatef(searchPose.x, searchPose.y, 0.1f);
                        camera.turnTowardsCamera(gl);
                        try {
                            final File f = new File("plugins/" + (rs.getTeamNumber() < 10 ? "0" + rs.getTeamNumber() : String.valueOf(rs.getTeamNumber())) + "/resources/" + "search.png").getAbsoluteFile();
                            Image.drawImage(gl, TextureLoader.getInstance().loadTexture(gl, f), 0, 0, 0.2f);
                            Text.drawText(""+(playerNumber++), 0.1f, 0.1f, 0.15f, new float[]{0,0,0});
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
