package teamcomm.gui.drawings.common;

import com.jogamp.opengl.GL2;
import data.PlayerInfo;
import data.SPLStandardMessage;
import java.util.Map;
import teamcomm.data.RobotData;
import teamcomm.data.RobotState;
import teamcomm.gui.drawings.PerPlayer;
import teamcomm.gui.drawings.Text;

/**
 *
 * @author Felix Thielke
 */
public class PlayerNumber extends PerPlayer {

    @Override
    public void draw(final GL2 gl, final Map<String, Integer> modelLists, final RobotState player, final int side) {
        final SPLStandardMessage msg = player.getLastMessage();
        if (msg != null) {
            gl.glPushMatrix();

            if(player.getPenalty() != PlayerInfo.PENALTY_NONE) {
                gl.glTranslatef(-msg.playerNum, -3.5f, 0.7f);
            } else {
                gl.glTranslatef(msg.pose[0] / 1000.f, msg.pose[1] / 1000.f, 0.7f);
            }
            
            if(side == RobotData.TEAM_RIGHT) {
                gl.glRotatef(180, 0, 0, 1);
            }
            gl.glRotatef(90, 1, 0, 0);
            Text.drawText(gl, "" + msg.playerNum, 0, 0, 0.25f);

            gl.glPopMatrix();
        }
    }

    @Override
    public boolean hasAlpha() {
        return true;
    }

    @Override
    public int getPriority() {
        return 10;
    }
}
