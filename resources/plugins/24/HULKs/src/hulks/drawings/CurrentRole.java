package hulks.drawings;

import com.jogamp.opengl.GL2;
import common.Log;
import hulks.message.HulksMessage;
import hulks.message.HulksMessageParts;
import teamcomm.data.RobotState;
import teamcomm.gui.Camera;
import teamcomm.gui.drawings.Image;
import teamcomm.gui.drawings.PerPlayer;
import teamcomm.gui.drawings.TextureLoader;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * Custom drawing for visualizing the current performing role.
 *
 * @author Georg Felbinger
 */
public class CurrentRole extends PerPlayer {

    @Override
    public void draw(final GL2 gl, final RobotState rs, final Camera camera) {
        if (rs.getLastMessage() != null
                && rs.getLastMessage().valid
                && rs.getLastMessage() instanceof HulksMessage) {
            final HulksMessage msg = (HulksMessage) rs.getLastMessage();
            if (msg.message.bhulks != null) {
                final String imageName = getImageNameForRole(msg);
                if (imageName == null) {
                    return;
                }
                gl.glPushMatrix();
                gl.glTranslatef(msg.pose[0] / 1000.f, msg.pose[1] / 1000.f, 1);
                camera.turnTowardsCamera(gl);
                try {
                    final File f = new File("plugins/" + (rs.getTeamNumber() < 10 ? "0" + rs.getTeamNumber() : String.valueOf(rs.getTeamNumber())) + "/resources/" + imageName).getAbsoluteFile();
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

    private String getImageNameForRole(final HulksMessage msg) {
        switch (msg.message.bhulks.currentlyPerfomingRole) {
            case King:
                return "king.png";
            case Queen:
                return "queen.png";
            case Knight:
                return "knight.png";
            case Rook:
                return "rook.png";
            case Bishop:
                return "bishop.png";
        }
        return null;
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
