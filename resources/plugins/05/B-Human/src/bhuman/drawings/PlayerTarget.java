package bhuman.drawings;

import bhuman.message.BHumanMessage;
import com.jogamp.opengl.GL2;
import data.PlayerInfo;
import teamcomm.data.RobotState;
import teamcomm.gui.Camera;
import teamcomm.gui.drawings.PerPlayer;

/**
 * Custom drawing for the walk and shoot target.
 *
 * @author Arne Hasselbring, merged from the former PlayerTarget drawing and Obstacle drawing
 */
public class PlayerTarget extends PerPlayer {

    private static final float CROSS_RADIUS = 0.1f;

    @Override
    public void draw(final GL2 gl, final RobotState rs, final Camera camera) {
        if (rs.getLastMessage() != null
                && rs.getLastMessage().valid
                && rs.getLastMessage() instanceof BHumanMessage) {
            final BHumanMessage msg = (BHumanMessage) rs.getLastMessage();
            if (msg.message.bhuman != null && msg.poseValid && rs.getPenalty() == PlayerInfo.PENALTY_NONE) {
                final float poseX = msg.pose[0] / 1000.f;
                final float poseY = msg.pose[1] / 1000.f;
                final float walkingToX = msg.message.bhuman.walkingToX / 1000.f;
                final float walkingToY = msg.message.bhuman.walkingToY / 1000.f;
                final float shootingToX = msg.message.bhuman.shootingToX / 1000.f;
                final float shootingToY = msg.message.bhuman.shootingToY / 1000.f;

                gl.glColor3f(0, 0, 1);
                gl.glNormal3f(0, 0, 1);

                gl.glBegin(GL2.GL_LINES);
                gl.glVertex2f(poseX, poseY);
                gl.glVertex2f(walkingToX, walkingToY);
                gl.glEnd();

                gl.glPushMatrix();
                gl.glTranslatef(walkingToX, walkingToY, 0);
                gl.glBegin(GL2.GL_LINES);
                gl.glVertex2f(-CROSS_RADIUS, -CROSS_RADIUS);
                gl.glVertex2f(CROSS_RADIUS, CROSS_RADIUS);
                gl.glVertex2f(-CROSS_RADIUS, CROSS_RADIUS);
                gl.glVertex2f(CROSS_RADIUS, -CROSS_RADIUS);
                gl.glEnd();
                gl.glPopMatrix();

                gl.glColor3f(1, 0, 0);

                gl.glBegin(GL2.GL_LINES);
                gl.glVertex2f(poseX, poseY);
                gl.glVertex2f(shootingToX, shootingToY);
                gl.glEnd();

                gl.glPushMatrix();
                gl.glTranslatef(shootingToX, shootingToY, 0);
                gl.glBegin(GL2.GL_LINES);
                gl.glVertex2f(-CROSS_RADIUS, -CROSS_RADIUS);
                gl.glVertex2f(CROSS_RADIUS, CROSS_RADIUS);
                gl.glVertex2f(-CROSS_RADIUS, CROSS_RADIUS);
                gl.glVertex2f(CROSS_RADIUS, -CROSS_RADIUS);
                gl.glEnd();
                gl.glPopMatrix();
            }
        }
    }

    @Override
    public boolean hasAlpha() {
        return false;
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
