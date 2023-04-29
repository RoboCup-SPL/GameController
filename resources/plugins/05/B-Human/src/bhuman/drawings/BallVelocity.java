package bhuman.drawings;

import bhuman.message.BHumanMessage;
import com.jogamp.opengl.GL2;
import data.GameControlReturnData;
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
    private static final float MAX_BALLAGE = 5.f;

    @Override
    public void draw(final GL2 gl, final RobotState rs, final Camera camera) {
        if (rs.getLastTeamMessage() != null
                && rs.getLastTeamMessage().valid
                && rs.getLastTeamMessage() instanceof BHumanMessage
                && rs.getLastGCRDMessage() != null
                && rs.getLastGCRDMessage().valid) {
            final BHumanMessage bhMsg = (BHumanMessage) rs.getLastTeamMessage();
            final GameControlReturnData gcMsg = rs.getLastGCRDMessage();
            if (gcMsg.ballAge != -1.f && gcMsg.ballAge < MAX_BALLAGE && rs.getPenalty() == PlayerInfo.PENALTY_NONE) {
                gl.glPushMatrix();

                gl.glTranslatef(gcMsg.pose[0] / 1000.0f, gcMsg.pose[1] / 1000.f, 0);
                gl.glRotatef((float) Math.toDegrees(gcMsg.pose[2]), 0, 0, 1);
                gl.glTranslatef(gcMsg.ball[0] / 1000.0f, gcMsg.ball[1] / 1000.f, 0);
                gl.glRotatef((float) Math.toDegrees(bhMsg.theRobotPose.rotation.radians - gcMsg.pose[2]), 0, 0, 1);

                gl.glBegin(GL2.GL_LINES);
                gl.glColor3f(1, 0, 0);
                gl.glNormal3f(0, 0, 1);
                gl.glVertex3f(0, 0, BALL_RADIUS);
                gl.glVertex3f(bhMsg.theBallModel.estimate.velocity.x / 1000.f, bhMsg.theBallModel.estimate.velocity.y / 1000.f, BALL_RADIUS);
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
