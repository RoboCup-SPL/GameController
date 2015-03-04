package teamcomm.gui.drawings;

import java.util.Map;
import javax.media.opengl.GL2;
import teamcomm.data.RobotState;

@Models({"robotBlue", "robotRed"})
/**
 *
 * @author Felix Thielke
 */
public class Player extends PerPlayer {

    @Override
    public void draw(final GL2 gl, final Map<String, Integer> modelLists, final RobotState player) {
        gl.glPushMatrix();

        gl.glTranslatef(player.getLastMessage().pose[0] / 1000.f, player.getLastMessage().pose[1] / 1000.f, 0);
        gl.glRotatef((float) Math.toDegrees(player.getLastMessage().pose[2]), 0, 0, 1);

        gl.glCallList(player.getLastMessage().teamColor == 0 ? modelLists.get("robotBlue") : modelLists.get("robotRed"));

        gl.glPopMatrix();
    }

}
