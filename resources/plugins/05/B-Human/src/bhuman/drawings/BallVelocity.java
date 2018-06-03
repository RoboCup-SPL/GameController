package bhuman.drawings;

import bhuman.message.BHumanMessage;
import com.jogamp.opengl.GL2;
import data.PlayerInfo;
import teamcomm.data.RobotState;
import teamcomm.gui.Camera;
import teamcomm.gui.drawings.PerPlayer;

/**
 * Custom drawing for the ball velocity.
 *
 * @author Arne Hasselbring, merged from the former Ball drawing and Obstacle drawing
 */
public class BallVelocity extends PerPlayer {

    private static final float BALL_RADIUS = 0.0325f;
    private static final float MAX_BALLAGE = 5.0f;

    @Override
    public void draw(final GL2 gl, final RobotState rs, final Camera camera) {
        if (rs.getLastMessage() != null
                && rs.getLastMessage().valid
                && rs.getLastMessage() instanceof BHumanMessage) {
            final BHumanMessage msg = (BHumanMessage) rs.getLastMessage();
            if (msg.message.bhuman != null && msg.poseValid && msg.ballValid && msg.ballAge > -1 && msg.ballAge < MAX_BALLAGE && rs.getPenalty() == PlayerInfo.PENALTY_NONE) {
                gl.glPushMatrix();

                gl.glTranslatef(msg.pose[0] / 1000.0f, msg.pose[1] / 1000.f, 0);
                gl.glRotatef((float) Math.toDegrees(msg.pose[2]), 0, 0, 1);
                gl.glTranslatef(msg.ball[0] / 1000.0f, msg.ball[1] / 1000.f, 0);

                gl.glBegin(GL2.GL_LINES);
                gl.glColor3f(1, 0, 0);
                gl.glNormal3f(0, 0, 1);
                gl.glVertex3f(0, 0, BALL_RADIUS);
                gl.glVertex3f(msg.message.bhuman.ballVelocity.x / 1000.f, msg.message.bhuman.ballVelocity.y / 1000.f, BALL_RADIUS);
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
        return 500;
    }
}
