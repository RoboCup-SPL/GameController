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
                && rs.getLastTeamMessage() instanceof BHumanMessage
                && rs.getLastGCRDMessage() != null
                && rs.getLastGCRDMessage().valid) {
            final BHumanMessage bhMsg = (BHumanMessage) rs.getLastTeamMessage();
            final GameControlReturnData gcMsg = rs.getLastGCRDMessage();
            if (rs.getPenalty() == PlayerInfo.PENALTY_NONE) {
                final float bhWalkingToX = bhMsg.theBehaviorStatus.walkingTo.x / 1000.f;
                final float bhWalkingToY = bhMsg.theBehaviorStatus.walkingTo.y / 1000.f;
                final float bhSin = (float) Math.sin(bhMsg.theRobotPose.rotation.radians);
                final float bhCos = (float) Math.cos(bhMsg.theRobotPose.rotation.radians);
                final float tmpWalkingToX = bhWalkingToX * bhCos - bhWalkingToY * bhSin
                        + (bhMsg.theRobotPose.translation.x - gcMsg.pose[0]) / 1000.f;
                final float tmpWalkingToY = bhWalkingToX * bhSin + bhWalkingToY * bhCos
                        + (bhMsg.theRobotPose.translation.y - gcMsg.pose[1]) / 1000.f;
                final float gcSin = (float) Math.sin(-gcMsg.pose[2]);
                final float gcCos = (float) Math.cos(-gcMsg.pose[2]);
                final float gcWalkingToX = tmpWalkingToX * gcCos - tmpWalkingToY * gcSin;
                final float gcWalkingToY = tmpWalkingToX * gcSin + tmpWalkingToY * gcCos;

                gl.glColor3f(0, 0, 1);
                gl.glNormal3f(0, 0, 1);

                gl.glPushMatrix();
                gl.glTranslatef(gcMsg.pose[0] / 1000.0f, gcMsg.pose[1] / 1000.f, 0);
                gl.glRotatef((float) Math.toDegrees(gcMsg.pose[2]), 0, 0, 1);

                gl.glBegin(GL2.GL_LINES);
                gl.glVertex2f(0, 0);
                gl.glVertex2f(gcWalkingToX, gcWalkingToY);
                gl.glEnd();

                gl.glPushMatrix();
                gl.glTranslatef(gcWalkingToX, gcWalkingToY, 0);
                gl.glRotatef(-(float) Math.toDegrees(gcMsg.pose[2]), 0, 0, 1);
                gl.glBegin(GL2.GL_LINES);
                gl.glVertex2f(-CROSS_RADIUS, -CROSS_RADIUS);
                gl.glVertex2f(CROSS_RADIUS, CROSS_RADIUS);
                gl.glVertex2f(-CROSS_RADIUS, CROSS_RADIUS);
                gl.glVertex2f(CROSS_RADIUS, -CROSS_RADIUS);
                gl.glEnd();
                gl.glPopMatrix();

                if (!bhMsg.theBehaviorStatus.shootingTo.isEmpty()) {
                    final float bhShootingToX = bhMsg.theBehaviorStatus.shootingTo.get(0).x / 1000.f;
                    final float bhShootingToY = bhMsg.theBehaviorStatus.shootingTo.get(0).y / 1000.f;
                    final float tmpShootingToX = bhShootingToX * bhCos - bhShootingToY * bhSin
                            + (bhMsg.theRobotPose.translation.x - gcMsg.pose[0]) / 1000.f;
                    final float tmpShootingToY = bhShootingToX * bhSin + bhShootingToY * bhCos
                            + (bhMsg.theRobotPose.translation.y - gcMsg.pose[1]) / 1000.f;
                    final float gcShootingToX = tmpShootingToX * gcCos - tmpShootingToY * gcSin;
                    final float gcShootingToY = tmpShootingToX * gcSin + tmpShootingToY * gcCos;

                    gl.glColor3f(1, 0, 0);

                    gl.glBegin(GL2.GL_LINES);
                    gl.glVertex2f(0, 0);
                    gl.glVertex2f(gcShootingToX, gcShootingToY);
                    gl.glEnd();

                    gl.glPushMatrix();
                    gl.glTranslatef(gcShootingToX, gcShootingToY, 0);
                    gl.glRotatef(-(float) Math.toDegrees(gcMsg.pose[2]), 0, 0, 1);
                    gl.glBegin(GL2.GL_LINES);
                    gl.glVertex2f(-CROSS_RADIUS, -CROSS_RADIUS);
                    gl.glVertex2f(CROSS_RADIUS, CROSS_RADIUS);
                    gl.glVertex2f(-CROSS_RADIUS, CROSS_RADIUS);
                    gl.glVertex2f(CROSS_RADIUS, -CROSS_RADIUS);
                    gl.glEnd();
                    gl.glPopMatrix();
                }

                gl.glPopMatrix();

                if (bhMsg.theBehaviorStatus.passTarget != -1) {
                    for (final RobotState teammate : team) {
                        if (teammate.getPlayerNumber() == bhMsg.theBehaviorStatus.passTarget
                                && teammate.getPenalty() == PlayerInfo.PENALTY_NONE
                                && teammate.getLastGCRDMessage() != null
                                && teammate.getLastGCRDMessage().valid) {
                            final GameControlReturnData teammateMsg = teammate.getLastGCRDMessage();

                            gl.glColor3f(1, 1, 1);

                            gl.glBegin(GL2.GL_LINES);
                            gl.glVertex2f(gcMsg.pose[0] / 1000.f, gcMsg.pose[1] / 1000.f);
                            gl.glVertex2f(teammateMsg.pose[0] / 1000.f, teammateMsg.pose[1] / 1000.f);
                            gl.glEnd();

                            gl.glPushMatrix();
                            gl.glTranslatef(teammateMsg.pose[0] / 1000.f, teammateMsg.pose[1] / 1000.f, 0);
                            gl.glRotatef((float) Math.toDegrees(Math.atan2(teammateMsg.pose[1] - gcMsg.pose[1], teammateMsg.pose[0] - gcMsg.pose[0])), 0, 0, 1);
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
