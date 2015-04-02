package teamcomm.gui.drawings;

import data.SPLStandardMessage;
import java.util.Map;
import javax.media.opengl.GL2;
import teamcomm.data.RobotState;

@Models({"robotBlue", "robotRed"})
/**
 *
 * @author Felix Thielke
 */
public class Player extends PerPlayer {
    
    private static final float CROSS_RADIUS = 0.1f;

    @Override
    public void draw(final GL2 gl, final Map<String, Integer> modelLists, final RobotState player, final boolean inverted) {
        final SPLStandardMessage msg = player.getLastMessage();
        if (msg != null) {
            gl.glPushMatrix();

            gl.glTranslatef(msg.pose[0] / 1000.f, msg.pose[1] / 1000.f, 0);
            gl.glRotatef((float) Math.toDegrees(msg.pose[2]), 0, 0, 1);

            if (msg.fallen) {
                gl.glTranslatef(0, 0, 0.05f);
                gl.glRotatef(90, 0, 1, 0);
            }

            gl.glCallList(msg.teamNum >= 5 ? modelLists.get("robotBlue") : modelLists.get("robotRed"));
            
            if (msg.fallen) {
                gl.glRotatef(-90, 0, 1, 0);
                gl.glTranslatef(0, 0, -0.05f);
            }
            gl.glColor3f(0, 0, 0);
            gl.glNormal3f(0, 0, 1);
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex3f(0, 0, 0);
            final float walkingToX = msg.walkingTo[0] / 1000.f;
            final float walkingToY = msg.walkingTo[1] / 1000.f;
            gl.glVertex3f(walkingToX, walkingToY, 0);
            gl.glEnd();
            gl.glTranslatef(walkingToX, walkingToY, 0);
            gl.glRotatef((float) -Math.toDegrees(msg.pose[2]), 0, 0, 1);
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex3f(-CROSS_RADIUS, -CROSS_RADIUS, 0);
            gl.glVertex3f(CROSS_RADIUS, CROSS_RADIUS, 0);
            gl.glEnd();
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex3f(-CROSS_RADIUS, CROSS_RADIUS, 0);
            gl.glVertex3f(CROSS_RADIUS, -CROSS_RADIUS, 0);
            gl.glEnd();
            
            gl.glPopMatrix();
        }
    }

}
