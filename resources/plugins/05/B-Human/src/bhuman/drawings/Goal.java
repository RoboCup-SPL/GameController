package bhuman.drawings;

import bhuman.message.BHumanMessage;
import bhuman.message.data.Eigen;
import bhuman.message.messages.GoalPercept;
import bhuman.message.messages.GoalPercept.GoalPost;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import teamcomm.data.RobotState;
import teamcomm.gui.Camera;
import teamcomm.gui.drawings.PerPlayer;
import teamcomm.gui.drawings.Text;

/**
 * Custom drawing for seen goalposts.
 *
 * @author Felix Thielke
 */
public class Goal extends PerPlayer {

    private static final float OPACITY = 0.5f;
    private static final double GOALPOST_RADIUS = 0.05;
    private static final double GOALPOST_HEIGHT = 0.85;

    @Override
    public void draw(final GL2 gl, final RobotState rs, final Camera camera) {
        if (rs.getLastMessage() != null
                && rs.getLastMessage().valid
                && rs.getLastMessage() instanceof BHumanMessage) {
            final BHumanMessage msg = (BHumanMessage) rs.getLastMessage();
            final GoalPercept goalPercept = msg.queue.getMessage(GoalPercept.class);
            if (goalPercept != null) {
                final GLU glu = GLU.createGLU(gl);
                final GLUquadric q = glu.gluNewQuadric();
                final float rotation = (float) Math.toDegrees(msg.pose[2]);

                // Enable transparency
                gl.glEnable(GL2.GL_BLEND);
                gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

                gl.glPushMatrix();
                gl.glTranslatef(msg.pose[0] / 1000.f, msg.pose[1] / 1000.f, 0);
                gl.glRotatef(rotation, 0, 0, 1);

                // Draw goalposts
                for (final GoalPost post : goalPercept.goalPosts) {
                    // Translate to goalpost
                    gl.glPushMatrix();
                    final Eigen.Vector2f postPos = post.positionOnField.invScale(1000);
                    gl.glTranslatef(postPos.x, postPos.y, 0);

                    // Draw post
                    gl.glColor4f(1, 1, 1, OPACITY);
                    glu.gluCylinder(q, GOALPOST_RADIUS, GOALPOST_RADIUS, GOALPOST_HEIGHT, 16, 1);

                    // Draw cylinder from goalpost to robot
                    gl.glColor4f(1, 1, 1, OPACITY / 3.0f);
                    gl.glRotatef((float) Math.toDegrees(Math.atan2(-postPos.y, -postPos.x)), 0, 0, 1);
                    gl.glRotatef(90, 0, 1, 0);
                    glu.gluCylinder(q, GOALPOST_RADIUS, GOALPOST_RADIUS, postPos.norm(), 16, 1);

                    // Translate back
                    gl.glPopMatrix();
                }

                // Draw crossbar
                if (goalPercept.goalPosts.size() == 2) {
                    final Eigen.Vector2f leftPos;
                    final Eigen.Vector2f diff;
                    if (goalPercept.goalPosts.get(0).position == GoalPost.Position.IS_LEFT && goalPercept.goalPosts.get(1).position == GoalPost.Position.IS_RIGHT) {
                        GoalPost post = goalPercept.goalPosts.get(0);
                        leftPos = post.positionOnField.invScale(1000);
                        post = goalPercept.goalPosts.get(1);
                        diff = leftPos.diff(post.positionOnField.invScale(1000));
                    } else if (goalPercept.goalPosts.get(0).position == GoalPost.Position.IS_RIGHT && goalPercept.goalPosts.get(1).position == GoalPost.Position.IS_LEFT) {
                        GoalPost post = goalPercept.goalPosts.get(1);
                        leftPos = post.positionOnField.invScale(1000);
                        post = goalPercept.goalPosts.get(0);
                        diff = leftPos.diff(post.positionOnField.invScale(1000));
                    } else {
                        return;
                    }

                    // Translate to left goalpost
                    gl.glPushMatrix();
                    gl.glTranslatef(leftPos.x, leftPos.y, (float) (GOALPOST_HEIGHT - GOALPOST_RADIUS));

                    // Draw cylinder from left goalpost to right goalpost
                    gl.glColor4f(1, 1, 1, OPACITY);
                    gl.glRotatef((float) Math.toDegrees(Math.atan2(diff.y, diff.x)), 0, 0, 1);
                    gl.glRotatef(90, 0, 1, 0);
                    glu.gluCylinder(q, GOALPOST_RADIUS, GOALPOST_RADIUS, diff.norm(), 16, 1);
                    glu.gluDeleteQuadric(q);

                    // Translate back
                    gl.glPopMatrix();
                }
                gl.glPopMatrix();

                // Disable transparency
                gl.glDisable(GL2.GL_BLEND);

                glu.gluDeleteQuadric(q);

                for (final GoalPost post : goalPercept.goalPosts) {
                    if (post.position == GoalPost.Position.IS_LEFT) {
                        gl.glPushMatrix();
                        final Eigen.Vector2f pos = post.positionOnField.invScale(1000);
                        gl.glTranslatef(msg.pose[0] / 1000.f, msg.pose[1] / 1000.f, 0);
                        gl.glRotatef(rotation, 0, 0, 1);
                        gl.glTranslatef(pos.x, pos.y, 1.2f);
                        gl.glRotatef(-rotation, 0, 0, 1);
                        camera.turnTowardsCamera(gl);
                        Text.drawText("L", 0, 0, 0.25f);
                        gl.glPopMatrix();
                    } else if (post.position == GoalPost.Position.IS_RIGHT) {
                        gl.glPushMatrix();
                        final Eigen.Vector2f pos = post.positionOnField.invScale(1000);
                        gl.glTranslatef(msg.pose[0] / 1000.f, msg.pose[1] / 1000.f, 0);
                        gl.glRotatef(rotation, 0, 0, 1);
                        gl.glTranslatef(pos.x, pos.y, 1.2f);
                        gl.glRotatef(-rotation, 0, 0, 1);
                        camera.turnTowardsCamera(gl);
                        Text.drawText("R", 0, 0, 0.25f);
                        gl.glPopMatrix();
                    }
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
