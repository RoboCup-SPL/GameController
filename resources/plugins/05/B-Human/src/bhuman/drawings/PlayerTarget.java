package bhuman.drawings;

import bhuman.message.BHumanMessage;
import com.jogamp.opengl.GL2;
import data.GameControlReturnData;
import data.PlayerInfo;
import java.util.Collection;
import teamcomm.data.RobotState;
import teamcomm.gui.Camera;
import teamcomm.gui.drawings.PerPlayerWithTeam;

/**
 * Custom drawing for the walk and shoot target.
 *
 * @author Arne Hasselbring, merged from the former PlayerTarget drawing and Obstacle drawing
 */
public class PlayerTarget extends PerPlayerWithTeam {

    private static final float CROSS_RADIUS = 0.1f;

    @Override
    public void draw(final GL2 gl, final Collection<RobotState> team, final RobotState rs, final Camera camera) {
        if (rs.getLastTeamMessage() != null
                && rs.getLastTeamMessage().valid
                && rs.getLastTeamMessage() instanceof BHumanMessage) {
            final BHumanMessage msg = (BHumanMessage) rs.getLastTeamMessage();
            if (rs.getPenalty() == PlayerInfo.PENALTY_NONE) {
                final float walkingToX = msg.theBehaviorStatus.walkingTo.x / 1000.f;
                final float walkingToY = msg.theBehaviorStatus.walkingTo.y / 1000.f;
                final float shootingToX = msg.theBehaviorStatus.shootingTo.x / 1000.f;
                final float shootingToY = msg.theBehaviorStatus.shootingTo.y / 1000.f;

                gl.glColor3f(0, 0, 1);
                gl.glNormal3f(0, 0, 1);

                gl.glPushMatrix();
                gl.glTranslatef(msg.theRobotPose.translation.x / 1000.0f, msg.theRobotPose.translation.y / 1000.f, 0);
                gl.glRotatef(msg.theRobotPose.rotation.toDegrees(), 0, 0, 1);

                gl.glBegin(GL2.GL_LINES);
                gl.glVertex2f(0, 0);
                gl.glVertex2f(walkingToX, walkingToY);
                gl.glEnd();

                gl.glPushMatrix();
                gl.glTranslatef(walkingToX, walkingToY, 0);
                gl.glRotatef(-msg.theRobotPose.rotation.toDegrees(), 0, 0, 1);
                gl.glBegin(GL2.GL_LINES);
                gl.glVertex2f(-CROSS_RADIUS, -CROSS_RADIUS);
                gl.glVertex2f(CROSS_RADIUS, CROSS_RADIUS);
                gl.glVertex2f(-CROSS_RADIUS, CROSS_RADIUS);
                gl.glVertex2f(CROSS_RADIUS, -CROSS_RADIUS);
                gl.glEnd();
                gl.glPopMatrix();

                gl.glColor3f(1, 0, 0);

                gl.glBegin(GL2.GL_LINES);
                gl.glVertex2f(0, 0);
                gl.glVertex2f(shootingToX, shootingToY);
                gl.glEnd();

                gl.glPushMatrix();
                gl.glTranslatef(shootingToX, shootingToY, 0);
                gl.glRotatef(-msg.theRobotPose.rotation.toDegrees(), 0, 0, 1);
                gl.glBegin(GL2.GL_LINES);
                gl.glVertex2f(-CROSS_RADIUS, -CROSS_RADIUS);
                gl.glVertex2f(CROSS_RADIUS, CROSS_RADIUS);
                gl.glVertex2f(-CROSS_RADIUS, CROSS_RADIUS);
                gl.glVertex2f(CROSS_RADIUS, -CROSS_RADIUS);
                gl.glEnd();
                gl.glPopMatrix();

                gl.glPopMatrix();

                if (msg.theBehaviorStatus.passTarget != -1) {
                    for (final RobotState teammate : team) {
                        if (teammate.getPlayerNumber() == msg.theBehaviorStatus.passTarget
                                && teammate.getPenalty() == PlayerInfo.PENALTY_NONE
                                && teammate.getLastGCRDMessage() != null
                                && teammate.getLastGCRDMessage().valid) {
                            final GameControlReturnData teammateMsg = teammate.getLastGCRDMessage();

                            gl.glColor3f(1, 1, 1);

                            gl.glBegin(GL2.GL_LINES);
                            gl.glVertex2f(msg.theRobotPose.translation.x / 1000.f, msg.theRobotPose.translation.y / 1000.f);
                            gl.glVertex2f(teammateMsg.pose[0] / 1000.f, teammateMsg.pose[1] / 1000.f);
                            gl.glEnd();

                            gl.glPushMatrix();
                            gl.glTranslatef(teammateMsg.pose[0] / 1000.f, teammateMsg.pose[1] / 1000.f, 0);
                            gl.glRotatef((float) Math.toDegrees(Math.atan2(teammateMsg.pose[1] - msg.theRobotPose.translation.y, teammateMsg.pose[0] - msg.theRobotPose.translation.x)), 0, 0, 1);
                            gl.glBegin(GL2.GL_LINES);
                            gl.glVertex2f(-CROSS_RADIUS, -CROSS_RADIUS);
                            gl.glVertex2f(0.f, 0.f);
                            gl.glVertex2f(-CROSS_RADIUS, CROSS_RADIUS);
                            gl.glVertex2f(0.f, 0.f);
                            gl.glEnd();
                            gl.glPopMatrix();

                            break;
                        }
                    }
                }
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
