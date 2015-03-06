package teamcomm.gui.drawings;

import data.SPLStandardMessage;
import java.util.Map;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import teamcomm.data.RobotState;

@Models({"ball"})
/**
 *
 * @author Felix Thielke
 */
public class BallPerPlayer extends PerPlayer {

    private static final float ROBOT_HEAD_Z = 0.5f;
    private static final float BALL_RADIUS = 0.0325f;

    @Override
    public void draw(final GL2 gl, final Map<String, Integer> modelLists, final RobotState player, final boolean inverted) {
        final SPLStandardMessage msg = player.getLastMessage();
        if (msg != null && msg.ballAge > -1 && msg.ballAge < 5000) {
            final float[] ball = {msg.ball[0] / 1000.f, msg.ball[1] / 1000.f};

            gl.glPushMatrix();

            // Translate to robot
            gl.glTranslatef(msg.pose[0] / 1000.f, msg.pose[1] / 1000.f, 0);
            gl.glRotatef((float) Math.toDegrees(msg.pose[2]), 0, 0, 1);

            // Translate to ball
            gl.glTranslatef(ball[0], ball[1], 0);

            // Draw ball
            gl.glCallList(modelLists.get("ball"));

            // Draw ball velocity
            gl.glBegin(GL.GL_LINES);
            gl.glColor3f(1, 0, 0);
            gl.glNormal3f(0, 0, 1);
            gl.glVertex3f(0, 0, BALL_RADIUS);
            gl.glVertex3f(msg.ballVel[0] / 1000.f, msg.ballVel[1] / 1000.f, BALL_RADIUS);
            gl.glEnd();

            // Translate back to robot
            gl.glTranslatef(-ball[0], -ball[1], 0);

            // Draw cylinder from robot to ball
            gl.glTranslatef(0, 0, ROBOT_HEAD_Z);
            gl.glRotatef((float) Math.toDegrees(Math.atan2(ball[1], ball[0])), 0, 0, 1);
            gl.glRotatef((float) Math.toDegrees(Math.atan2(Math.sqrt(ball[0] * ball[0] + ball[1] * ball[1]), BALL_RADIUS - ROBOT_HEAD_Z)), 0, 1, 0);
            gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE);
            gl.glColor4f(1, 0, 0, 0.25f);
            gl.glEnable(GL2.GL_BLEND);
            gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
            final GLU glu = GLU.createGLU(gl);
            final GLUquadric q = glu.gluNewQuadric();
            glu.gluCylinder(q, BALL_RADIUS, BALL_RADIUS, Math.sqrt(ball[0] * ball[0] + ball[1] * ball[1] + (BALL_RADIUS - ROBOT_HEAD_Z) * (BALL_RADIUS - ROBOT_HEAD_Z)), 16, 1);
            glu.gluDeleteQuadric(q);
            gl.glDisable(GL2.GL_BLEND);

            gl.glPopMatrix();
        }
    }

}
