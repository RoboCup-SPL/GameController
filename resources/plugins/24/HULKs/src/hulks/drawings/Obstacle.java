package hulks.drawings;

import hulks.message.BHULKsStandardMessage;
import hulks.message.HulksMessage;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import teamcomm.data.RobotState;
import teamcomm.gui.Camera;
import teamcomm.gui.drawings.PerPlayer;

/**
 * Custom drawing for obstacles.
 *
 * @author Florian Maaß
 */
public class Obstacle extends PerPlayer {

    private static final float CROSS_RADIUS = 0.1f;
    private static final float OPACITY = 0.75f;

    @Override
    public void draw(final GL2 gl, final RobotState rs, final Camera camera) {
        if (rs.getLastMessage() != null
                && rs.getLastMessage().valid
                && rs.getLastMessage() instanceof HulksMessage) {
            final HulksMessage msg = (HulksMessage) rs.getLastMessage();
            if (msg.message.bhulks != null) {
                final GLU glu = GLU.createGLU(gl);
                final GLUquadric q = glu.gluNewQuadric();

                // Enable transparency
                gl.glEnable(GL2.GL_BLEND);
                gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

                // Draw obstacles
                for (final BHULKsStandardMessage.Obstacle obstacle : msg.message.bhulks.obstacles) {
                    // Set color
                    switch (obstacle.type) {
                        case goalpost:
                            gl.glColor4f(1.f, 1.f, 0.f, OPACITY);
                            break;
                        case fallenSomeRobot:
                        case someRobot:
                            gl.glColor4f(1.f, .5f, 0.f, OPACITY);
                            break;
                        case fallenOpponent:
                        case opponent:
                            gl.glColor4f(1.f, 0.f, 1.f, OPACITY);
                            break;
                        case fallenTeammate:
                        case teammate:
                            gl.glColor4f(0.f, 1.f, 1.f, OPACITY);
                            break;
                        default:
                            gl.glColor4f(0.f, 0.f, 1.f, OPACITY);
                            break;
                    }

                    gl.glPushMatrix();
                    gl.glTranslatef(msg.pose[0] / 1000.0f, msg.pose[1] / 1000.f, 0);
                    gl.glRotatef((float) Math.toDegrees(msg.pose[2]), 0, 0, 1);

                    // Draw line from obstacle to robot to determine which player saw that obstacle
                    gl.glBegin(GL2.GL_LINES);
                    gl.glVertex3f(0.f, 0.f, 0.f);
                    gl.glVertex3f(obstacle.center[0] / 1000.f, obstacle.center[1] / 1000.f, 0.f);
                    gl.glEnd();

                    // Translate to obstacle
                    gl.glTranslatef(obstacle.center[0] / 1000.f, obstacle.center[1] / 1000.f, 0.f);
                    gl.glBegin(GL2.GL_LINES);
                    gl.glVertex2f(-CROSS_RADIUS, -CROSS_RADIUS);
                    gl.glVertex2f(CROSS_RADIUS, CROSS_RADIUS);
                    gl.glVertex2f(-CROSS_RADIUS, CROSS_RADIUS);
                    gl.glVertex2f(CROSS_RADIUS, -CROSS_RADIUS);
                    gl.glEnd();

                    // Translate back
                    gl.glPopMatrix();
                }
                // Disable transparency
                gl.glDisable(GL2.GL_BLEND);

                glu.gluDeleteQuadric(q);
            }
        }
    }

    @Override
    public boolean hasAlpha() {
        return true;
    }

    @Override
    public int getPriority() {
        return 100;
    }
}
