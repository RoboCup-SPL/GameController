package teamcomm.gui.drawings;

import java.util.Map;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import teamcomm.data.RobotState;

@Models({"ballTransparent"})
/**
 *
 * @author Felix Thielke
 */
public class BallPerPlayer extends PerPlayer {

    @Override
    public void draw(final GL2 gl, final Map<String, Integer> modelLists, final RobotState player, final boolean inverted) {
        if(player.getLastMessage() != null && player.getLastMessage().ballAge > -1 && player.getLastMessage().ballAge < 5000) {
            gl.glPushMatrix();

            gl.glTranslatef(player.getLastMessage().pose[0] / 1000.f, player.getLastMessage().pose[1] / 1000.f, 0);
            gl.glRotatef((float) Math.toDegrees(player.getLastMessage().pose[2]), 0, 0, 1);
            gl.glTranslatef(player.getLastMessage().ball[0] / 1000.f, player.getLastMessage().ball[1] / 1000.f, 0);

            gl.glCallList(modelLists.get("ballTransparent"));
            
            // Ball velocity
            gl.glBegin(GL.GL_LINES);
            gl.glColor3f(1, 0, 0);
            gl.glNormal3f(0, 0, 1);
            gl.glVertex3f(0, 0, 0.0325f);
            gl.glVertex3f(player.getLastMessage().ballVel[0] / 1000.f, player.getLastMessage().ballVel[1] / 1000.f, 0.0325f);
            gl.glEnd();

            gl.glPopMatrix();
        }
    }

}
