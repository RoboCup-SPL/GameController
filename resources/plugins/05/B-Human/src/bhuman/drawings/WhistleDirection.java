package bhuman.drawings;

import bhuman.message.BHumanMessage;
import bhuman.message.data.Angle;
import bhuman.message.messages.WhistleDOA;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import teamcomm.data.RobotState;
import teamcomm.gui.Camera;
import teamcomm.gui.drawings.PerPlayer;

/**
 * Custom drawing for whistle directions.
 *
 * @author Arne Hasselbring
 */
public class WhistleDirection extends PerPlayer {

    private static final float ROBOT_HEAD_Z = 0.5f;

    private static final float CYLINDER_RADIUS = 0.03f;
    private static final float OPACITY = 0.75f;

    @Override
    public void draw(final GL2 gl, final RobotState rs, final Camera camera) {
        if (rs.getLastMessage() != null
                && rs.getLastMessage().valid
                && rs.getLastMessage() instanceof BHumanMessage) {
            final BHumanMessage msg = (BHumanMessage) rs.getLastMessage();
            if (msg.message.queue != null) {
                final WhistleDOA whistleDOA = msg.message.queue.getMessage(WhistleDOA.class);
                if (whistleDOA != null) {
                    final GLU glu = GLU.createGLU(gl);
                    final GLUquadric q = glu.gluNewQuadric();

                    // Enable transparency
                    gl.glEnable(GL2.GL_BLEND);
                    gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
                    gl.glColor4f(0.f, 0.f, 1.f, OPACITY);

                    gl.glPushMatrix();

                    // Translate to robot head
                    gl.glTranslatef(msg.pose[0] / 1000.f, msg.pose[1] / 1000.f, ROBOT_HEAD_Z);
                    gl.glRotatef((float) Math.toDegrees(msg.pose[2]), 0, 0, 1);

                    for (final Angle whistleDirection : whistleDOA.whistleDirections) {
                        gl.glPushMatrix();
                        gl.glRotatef(whistleDirection.toDegrees(), 0, 0, 1);
                        gl.glRotatef(90, 0, 1, 0);
                        glu.gluCylinder(q, CYLINDER_RADIUS, CYLINDER_RADIUS, 10, 16, 1);
                        gl.glPopMatrix();
                    }

                    gl.glPopMatrix();

                    // Disable transparency
                    gl.glDisable(GL2.GL_BLEND);

                    glu.gluDeleteQuadric(q);
                }
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

