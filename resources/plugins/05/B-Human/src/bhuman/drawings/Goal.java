package bhuman.drawings;

import bhuman.message.BHumanMessage;
import bhuman.message.messages.GoalPercept;
import bhuman.message.messages.GoalPercept.GoalPost;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import teamcomm.gui.Camera;
import teamcomm.data.RobotState;
import teamcomm.gui.drawings.PerPlayer;

/**
 *
 * @author Felix Thielke
 */
public class Goal extends PerPlayer {

    public static final float OPACITY = 0.5f;
    public static final double GOALPOST_RADIUS = 0.05;
    public static final double GOALPOST_HEIGHT = 0.85;

    @Override
    public void draw(final GL2 gl, final RobotState rs, final Camera camera) {
        if (rs.getLastMessage() != null && rs.getLastMessage() instanceof BHumanMessage) {
            final BHumanMessage msg = (BHumanMessage) rs.getLastMessage();
            final GoalPercept goalPercept = msg.queue.getMessage(GoalPercept.class);
            if (goalPercept != null) {
                for (final GoalPost post : goalPercept.goalPosts) {
                    // Set color
                    if (post.position == GoalPost.Position.IS_LEFT) {
                        gl.glColor4f(0, 0, 1, OPACITY);
                    } else if (post.position == GoalPost.Position.IS_RIGHT) {
                        gl.glColor4f(1, 0, 0, OPACITY);
                    } else {
                        gl.glColor4f(1, 1, 1, OPACITY);
                    }

                    // Enable transparency
                    gl.glEnable(GL2.GL_BLEND);
                    gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

                    // Translate to goalpost
                    gl.glPushMatrix();
                    gl.glTranslatef(post.positionOnField.x / 1000.0f, post.positionOnField.y / 1000.0f, 0);

                    // Draw post
                    final GLU glu = GLU.createGLU(gl);
                    final GLUquadric q = glu.gluNewQuadric();
                    glu.gluCylinder(q, GOALPOST_RADIUS, GOALPOST_RADIUS, GOALPOST_HEIGHT, 16, 1);

                    // Draw cylinder from goalpost to robot
                    gl.glColor4f(1, 1, 1, OPACITY/3.0f);
                    gl.glRotatef((float) Math.toDegrees(Math.atan2(msg.pose[1] - post.positionOnField.y, msg.pose[0] - post.positionOnField.x)), 0, 0, 1);
                    gl.glRotatef(90, 0, 1, 0);
                    final double distance = Math.sqrt(Math.pow(msg.pose[0]/1000.0f - post.positionOnField.x/1000.0f, 2) + Math.pow(msg.pose[1]/1000.0f - post.positionOnField.y/1000.0f, 2));
                    glu.gluCylinder(q, GOALPOST_RADIUS, GOALPOST_RADIUS, distance, 16, 1);
                    glu.gluDeleteQuadric(q);

                    // Disable transparency
                    gl.glDisable(GL2.GL_BLEND);

                    // Translate back
                    gl.glPopMatrix();
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
