package teamcomm.gui.drawings;

import java.util.Map;
import javax.media.opengl.GL2;
import teamcomm.data.RobotState;

@Models({"ballTransparent"})
/**
 *
 * @author Felix Thielke
 */
public class BallPerPlayer extends PerPlayer {

    @Override
    public void draw(final GL2 gl, final Map<String, Integer> modelLists, final RobotState player) {
        gl.glPushMatrix();

        gl.glTranslatef(player.getLastMessage().pose[0] / 1000.f, player.getLastMessage().pose[1] / 1000.f, 0);
        gl.glRotatef((float) Math.toDegrees(player.getLastMessage().pose[2]), 0, 0, 1);
        gl.glTranslatef(player.getLastMessage().ball[0] / 1000.f, player.getLastMessage().ball[1] / 1000.f, 0);
        
        gl.glCallList(modelLists.get("ballTransparent"));

        gl.glPopMatrix();
    }

}
