package hulks.drawings;

import com.jogamp.opengl.GL2;
import common.Log;
import data.Rules;
import hulks.message.HulksMessage;
import hulks.message.HulksMessageParts;
import teamcomm.data.GameState;
import teamcomm.data.RobotState;
import teamcomm.gui.Camera;
import teamcomm.gui.drawings.Image;
import teamcomm.gui.drawings.Text;
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
public class TimeToReachBall extends PerPlayer {

    @Override
    public void draw(final GL2 gl, final RobotState rs, final Camera camera) {
        if (rs.getLastMessage() != null
                && rs.getLastMessage().valid
                && rs.getLastMessage() instanceof HulksMessage) {
            final HulksMessage msg = (HulksMessage) rs.getLastMessage();
            if (msg.message.bhulks != null) {
                gl.glPushMatrix();
                gl.glTranslatef(msg.pose[0] / 1000.f, msg.pose[1] / 1000.f - 0.3f, 0.5f);
                camera.turnTowardsCamera(gl);
                    final long ttrb = msg.message.bhulks.timeWhenReachBall.getTimeSince(msg.message.bhulks.timestamp);
                    Text.drawText(""+ttrb, 0, 0, 0.15f, new float[]{0,0,0});
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
