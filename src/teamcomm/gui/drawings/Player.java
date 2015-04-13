package teamcomm.gui.drawings;

import com.jogamp.opengl.GL2;
import data.GameControlData;
import data.PlayerInfo;
import data.SPLStandardMessage;
import java.util.Map;
import teamcomm.data.RobotData;
import teamcomm.data.RobotState;

@Models({"robotBlue", "robotRed", "robotBlack", "robotYellow"})
/**
 *
 * @author Felix Thielke
 */
public class Player extends PerPlayer {

    @Override
    public void draw(final GL2 gl, final Map<String, Integer> modelLists, final RobotState player, final int side) {
        final SPLStandardMessage msg = player.getLastMessage();
        if (msg != null) {
            gl.glPushMatrix();

            if (player.getPenalty() != PlayerInfo.PENALTY_NONE) {
                gl.glTranslatef(-msg.playerNum, -3.5f, 0);
            } else {
                gl.glTranslatef(msg.pose[0] / 1000.f, msg.pose[1] / 1000.f, 0);
                gl.glRotatef((float) Math.toDegrees(msg.pose[2]), 0, 0, 1);
            }

            if (msg.fallen) {
                gl.glTranslatef(0, 0, 0.05f);
                gl.glRotatef(90, 0, 1, 0);
            }

            switch (RobotData.getInstance().getTeamColor(side)) {
                case GameControlData.TEAM_BLUE:
                    gl.glCallList(modelLists.get("robotBlue"));
                    break;
                case GameControlData.TEAM_RED:
                    gl.glCallList(modelLists.get("robotRed"));
                    break;
                case GameControlData.TEAM_BLACK:
                    gl.glCallList(modelLists.get("robotBlack"));
                    break;
                case GameControlData.TEAM_YELLOW:
                    gl.glCallList(modelLists.get("robotYellow"));
                    break;
            }

            gl.glPopMatrix();
        }
    }

}
