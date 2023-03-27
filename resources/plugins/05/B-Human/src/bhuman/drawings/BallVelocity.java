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
    private static final long MAX_BALLAGE = 5000;

    @Override
    public void draw(final GL2 gl, final RobotState rs, final Camera camera) {
        if (rs.getLastTeamMessage() != null
                && rs.getLastTeamMessage().valid
                && rs.getLastTeamMessage() instanceof BHumanMessage) {
            final BHumanMessage msg = (BHumanMessage) rs.getLastTeamMessage();
            if (msg.theFrameInfo.time.getTimeSince(msg.theBallModel.timeWhenLastSeen.timestamp) < MAX_BALLAGE && rs.getPenalty() == PlayerInfo.PENALTY_NONE) {
                gl.glPushMatrix();

                gl.glTranslatef(msg.theRobotPose.translation.x / 1000.0f, msg.theRobotPose.translation.y / 1000.f, 0);
                gl.glRotatef(msg.theRobotPose.rotation.toDegrees(), 0, 0, 1);
                gl.glTranslatef(msg.theBallModel.estimate.position.x / 1000.0f, msg.theBallModel.estimate.position.y / 1000.f, 0);

                gl.glBegin(GL2.GL_LINES);
                gl.glColor3f(1, 0, 0);
                gl.glNormal3f(0, 0, 1);
                gl.glVertex3f(0, 0, BALL_RADIUS);
                gl.glVertex3f(msg.theBallModel.estimate.velocity.x / 1000.f, msg.theBallModel.estimate.velocity.y / 1000.f, BALL_RADIUS);
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
