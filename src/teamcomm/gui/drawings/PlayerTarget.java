package teamcomm.gui.drawings;

import com.jogamp.opengl.GL2;
import data.SPLStandardMessage;
import java.util.Map;
import teamcomm.data.RobotState;

/**
 *
 * @author Felix Thielke
 */
public class PlayerTarget extends PerPlayer {

    private static final float CROSS_RADIUS = 0.1f;

    @Override
    public void draw(final GL2 gl, final Map<String, Integer> modelLists, final RobotState player, final boolean inverted) {
        final SPLStandardMessage msg = player.getLastMessage();
        if (msg != null) {
            final float poseX = msg.pose[0] / 1000.f;
            final float poseY = msg.pose[1] / 1000.f;
            final float walkingToX = msg.walkingTo[0] / 1000.f;
            final float walkingToY = msg.walkingTo[1] / 1000.f;

            // Set color and normal
            gl.glColor3f(0, 0, 0);
            gl.glNormal3f(0, 0, 1);

            // Draw line from player to target
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex3f(poseX, poseY, 0);
            gl.glVertex3f(walkingToX, walkingToY, 0);
            gl.glEnd();

            // Draw cross on target
            gl.glTranslatef(walkingToX, walkingToY, 0);
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex3f(-CROSS_RADIUS, -CROSS_RADIUS, 0);
            gl.glVertex3f(CROSS_RADIUS, CROSS_RADIUS, 0);
            gl.glEnd();
            gl.glBegin(GL2.GL_LINES);
            gl.glVertex3f(-CROSS_RADIUS, CROSS_RADIUS, 0);
            gl.glVertex3f(CROSS_RADIUS, -CROSS_RADIUS, 0);
            gl.glEnd();
            gl.glTranslatef(-walkingToX, -walkingToY, 0);
        }
    }

}
