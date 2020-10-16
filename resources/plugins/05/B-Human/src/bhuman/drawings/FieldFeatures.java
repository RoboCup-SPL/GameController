package bhuman.drawings;

import bhuman.message.BHumanMessage;
import bhuman.message.messages.FieldFeatureOverview;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import teamcomm.data.RobotState;
import teamcomm.gui.Camera;
import teamcomm.gui.drawings.PerPlayer;

/**
 * Custom drawing for FieldFeatures.
 *
 * @author Felix Thielke
 */
public class FieldFeatures extends PerPlayer {

    private static final float PENALTY_AREA_WIDTH = 2.2f;
    private static final float PENALTY_AREA_DEPTH = 0.6f;
    private static final float CENTER_CIRCLE_RADIUS = 0.75f;

    private static final float FIELD_FEATURE_SIZE = 0.75f;

    private static final float LINE_RADIUS = 0.01f;
    private static final float OPACITY = 0.75f;

    @Override
    public void draw(final GL2 gl, final RobotState rs, final Camera camera) {
        if (rs.getLastMessage() != null
                && rs.getLastMessage().valid
                && rs.getLastMessage() instanceof BHumanMessage) {
            final BHumanMessage msg = (BHumanMessage) rs.getLastMessage();
            if (msg.message.queue != null) {
                final FieldFeatureOverview fieldFeatures = msg.message.queue.getMessage(FieldFeatureOverview.class);
                if (fieldFeatures != null) {
                    final GLU glu = GLU.createGLU(gl);
                    final GLUquadric q = glu.gluNewQuadric();

                    // Enable transparency
                    gl.glEnable(GL2.GL_BLEND);
                    gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
                    gl.glColor4f(0.f, 0.f, 1.f, OPACITY);

                    // Apply player pose
                    gl.glPushMatrix();
                    gl.glTranslatef(msg.pose[0] / 1000.0f, msg.pose[1] / 1000.f, 0);
                    gl.glRotatef((float) Math.toDegrees(msg.pose[2]), 0, 0, 1);

                    // Draw field features
                    for (final FieldFeatureOverview.Feature feature : FieldFeatureOverview.Feature.values()) {
                        final FieldFeatureOverview.FieldFeatureStatus status = fieldFeatures.statuses.get(feature);
                        if (status.isValid) {
                            // Draw line from robot to field feature
                            gl.glBegin(GL2.GL_LINES);
                            gl.glVertex3f(0.f, 0.f, 0.f);
                            gl.glVertex3f(status.translation.x / 1000.0f, status.translation.y / 1000.f, 0.f);
                            gl.glEnd();

                            // Draw field feature
                            gl.glPushMatrix();
                            gl.glTranslatef(status.translation.x / 1000.0f, status.translation.y / 1000.f, 0);
                            gl.glRotatef(status.rotation.toDegrees(), 0, 0, 1);
                            switch (feature) {
                                case PenaltyArea: {
                                    final float halfWidth = PENALTY_AREA_WIDTH / 2;
                                    final float halfDepth = PENALTY_AREA_DEPTH / 2;
                                    gl.glBegin(GL2.GL_QUADS);
                                    // Back left to back right
                                    gl.glVertex2f(-halfDepth + LINE_RADIUS, halfWidth);
                                    gl.glVertex2f(-halfDepth - LINE_RADIUS, halfWidth);
                                    gl.glVertex2f(-halfDepth - LINE_RADIUS, -halfWidth);
                                    gl.glVertex2f(-halfDepth + LINE_RADIUS, -halfWidth);
                                    // Back right to front right
                                    gl.glVertex2f(-halfDepth, -halfWidth + LINE_RADIUS);
                                    gl.glVertex2f(-halfDepth, -halfWidth - LINE_RADIUS);
                                    gl.glVertex2f(halfDepth, -halfWidth - LINE_RADIUS);
                                    gl.glVertex2f(halfDepth, -halfWidth + LINE_RADIUS);
                                    // front left to front right
                                    gl.glVertex2f(halfDepth + LINE_RADIUS, halfWidth);
                                    gl.glVertex2f(halfDepth - LINE_RADIUS, halfWidth);
                                    gl.glVertex2f(halfDepth - LINE_RADIUS, -halfWidth);
                                    gl.glVertex2f(halfDepth + LINE_RADIUS, -halfWidth);
                                    // Back left to front left
                                    gl.glVertex2f(-halfDepth, halfWidth + LINE_RADIUS);
                                    gl.glVertex2f(-halfDepth, halfWidth - LINE_RADIUS);
                                    gl.glVertex2f(halfDepth, halfWidth - LINE_RADIUS);
                                    gl.glVertex2f(halfDepth, halfWidth + LINE_RADIUS);
                                    // Front center to center
                                    gl.glVertex2f(halfDepth, -LINE_RADIUS);
                                    gl.glVertex2f(halfDepth, LINE_RADIUS);
                                    gl.glVertex2f(0, LINE_RADIUS);
                                    gl.glVertex2f(0, -LINE_RADIUS);
                                    gl.glEnd();
                                }
                                break;
                                case MidCircle:
                                    glu.gluDisk(q, CENTER_CIRCLE_RADIUS - LINE_RADIUS, CENTER_CIRCLE_RADIUS + LINE_RADIUS, 16, 16);
                                    gl.glBegin(GL2.GL_QUADS);
                                    gl.glVertex2f(LINE_RADIUS, CENTER_CIRCLE_RADIUS);
                                    gl.glVertex2f(-LINE_RADIUS, CENTER_CIRCLE_RADIUS);
                                    gl.glVertex2f(-LINE_RADIUS, -CENTER_CIRCLE_RADIUS);
                                    gl.glVertex2f(LINE_RADIUS, -CENTER_CIRCLE_RADIUS);
                                    gl.glEnd();
                                    break;
                                case OuterCorner: {
                                    final float hypotsize = (float) (Math.sqrt(2) * FIELD_FEATURE_SIZE);
                                    gl.glBegin(GL2.GL_QUADS);
                                    if (status.isRightSided) {
                                        gl.glVertex2f(-LINE_RADIUS, 0);
                                        gl.glVertex2f(LINE_RADIUS, 0);
                                        gl.glVertex2f(LINE_RADIUS, FIELD_FEATURE_SIZE);
                                        gl.glVertex2f(-LINE_RADIUS, FIELD_FEATURE_SIZE);
                                    } else {
                                        gl.glVertex2f(-LINE_RADIUS, -FIELD_FEATURE_SIZE);
                                        gl.glVertex2f(LINE_RADIUS, -FIELD_FEATURE_SIZE);
                                        gl.glVertex2f(LINE_RADIUS, 0);
                                        gl.glVertex2f(-LINE_RADIUS, 0);
                                    }
                                    gl.glVertex2f(0, LINE_RADIUS);
                                    gl.glVertex2f(0, -LINE_RADIUS);
                                    gl.glVertex2f(FIELD_FEATURE_SIZE, -LINE_RADIUS);
                                    gl.glVertex2f(FIELD_FEATURE_SIZE, LINE_RADIUS);
                                    gl.glEnd();
                                    if (status.isRightSided) {
                                        gl.glRotatef(45, 0, 0, 1);
                                    } else {
                                        gl.glRotatef(-45, 0, 0, 1);
                                    }
                                    gl.glBegin(GL2.GL_QUADS);
                                    gl.glVertex2f(0, LINE_RADIUS);
                                    gl.glVertex2f(0, -LINE_RADIUS);
                                    gl.glVertex2f(FIELD_FEATURE_SIZE / 2, -LINE_RADIUS);
                                    gl.glVertex2f(FIELD_FEATURE_SIZE / 2, LINE_RADIUS);
                                    gl.glEnd();
                                    if (status.isRightSided) {
                                        gl.glRotatef(-45, 0, 0, 1);
                                        gl.glTranslatef(0.f, FIELD_FEATURE_SIZE, 0.f);
                                        gl.glRotatef(-45, 0, 0, 1);
                                    } else {
                                        gl.glRotatef(45, 0, 0, 1);
                                        gl.glTranslatef(0.f, -FIELD_FEATURE_SIZE, 0.f);
                                        gl.glRotatef(45, 0, 0, 1);
                                    }
                                    gl.glBegin(GL2.GL_QUADS);
                                    gl.glVertex2f(0, LINE_RADIUS);
                                    gl.glVertex2f(0, -LINE_RADIUS);
                                    gl.glVertex2f(hypotsize, -LINE_RADIUS);
                                    gl.glVertex2f(hypotsize, LINE_RADIUS);
                                    gl.glEnd();
                                }
                                break;
                                case MidCorner: {
                                    final float hypotsize = (float) (Math.sqrt(2) * FIELD_FEATURE_SIZE);
                                    gl.glBegin(GL2.GL_QUADS);
                                    gl.glVertex2f(0, LINE_RADIUS);
                                    gl.glVertex2f(0, -LINE_RADIUS);
                                    gl.glVertex2f(FIELD_FEATURE_SIZE / 2, -LINE_RADIUS);
                                    gl.glVertex2f(FIELD_FEATURE_SIZE / 2, LINE_RADIUS);
                                    gl.glVertex2f(LINE_RADIUS, FIELD_FEATURE_SIZE);
                                    gl.glVertex2f(-LINE_RADIUS, FIELD_FEATURE_SIZE);
                                    gl.glVertex2f(-LINE_RADIUS, -FIELD_FEATURE_SIZE);
                                    gl.glVertex2f(LINE_RADIUS, -FIELD_FEATURE_SIZE);
                                    gl.glEnd();
                                    gl.glTranslatef(0.f, FIELD_FEATURE_SIZE, 0.f);
                                    gl.glRotatef(-45, 0, 0, 1);
                                    gl.glBegin(GL2.GL_QUADS);
                                    gl.glVertex2f(0, LINE_RADIUS);
                                    gl.glVertex2f(0, -LINE_RADIUS);
                                    gl.glVertex2f(hypotsize, -LINE_RADIUS);
                                    gl.glVertex2f(hypotsize, LINE_RADIUS);
                                    gl.glEnd();
                                    gl.glTranslatef(hypotsize, 0.f, 0.f);
                                    gl.glRotatef(270, 0, 0, 1);
                                    gl.glBegin(GL2.GL_QUADS);
                                    gl.glVertex2f(0, LINE_RADIUS);
                                    gl.glVertex2f(0, -LINE_RADIUS);
                                    gl.glVertex2f(hypotsize, -LINE_RADIUS);
                                    gl.glVertex2f(hypotsize, LINE_RADIUS);
                                    gl.glEnd();
                                }
                                break;
                                case PenaltyMarkWithPenaltyAreaLine: {
                                    final float hypotsize = (float) (Math.sqrt(2) * FIELD_FEATURE_SIZE);
                                    gl.glBegin(GL2.GL_QUADS);
                                    gl.glVertex2f(LINE_RADIUS, FIELD_FEATURE_SIZE);
                                    gl.glVertex2f(-LINE_RADIUS, FIELD_FEATURE_SIZE);
                                    gl.glVertex2f(-LINE_RADIUS, -FIELD_FEATURE_SIZE);
                                    gl.glVertex2f(LINE_RADIUS, -FIELD_FEATURE_SIZE);
                                    gl.glVertex2f(FIELD_FEATURE_SIZE + LINE_RADIUS, FIELD_FEATURE_SIZE);
                                    gl.glVertex2f(FIELD_FEATURE_SIZE - LINE_RADIUS, FIELD_FEATURE_SIZE);
                                    gl.glVertex2f(FIELD_FEATURE_SIZE - LINE_RADIUS, -FIELD_FEATURE_SIZE);
                                    gl.glVertex2f(FIELD_FEATURE_SIZE + LINE_RADIUS, -FIELD_FEATURE_SIZE);
                                    gl.glVertex2f(0, -FIELD_FEATURE_SIZE + LINE_RADIUS);
                                    gl.glVertex2f(0, -FIELD_FEATURE_SIZE - LINE_RADIUS);
                                    gl.glVertex2f(FIELD_FEATURE_SIZE, -FIELD_FEATURE_SIZE - LINE_RADIUS);
                                    gl.glVertex2f(FIELD_FEATURE_SIZE, -FIELD_FEATURE_SIZE + LINE_RADIUS);
                                    gl.glVertex2f(0, FIELD_FEATURE_SIZE + LINE_RADIUS);
                                    gl.glVertex2f(0, FIELD_FEATURE_SIZE - LINE_RADIUS);
                                    gl.glVertex2f(FIELD_FEATURE_SIZE, FIELD_FEATURE_SIZE - LINE_RADIUS);
                                    gl.glVertex2f(FIELD_FEATURE_SIZE, FIELD_FEATURE_SIZE + LINE_RADIUS);
                                    gl.glEnd();
                                    gl.glTranslatef(FIELD_FEATURE_SIZE, 0.f, 0.f);
                                    gl.glRotatef(-45, 0, 0, 1);
                                    gl.glBegin(GL2.GL_QUADS);
                                    gl.glVertex2f(0, -LINE_RADIUS);
                                    gl.glVertex2f(0, LINE_RADIUS);
                                    gl.glVertex2f(-hypotsize, LINE_RADIUS);
                                    gl.glVertex2f(-hypotsize, -LINE_RADIUS);
                                    gl.glEnd();
                                    gl.glRotatef(90, 0, 0, 1);
                                    gl.glBegin(GL2.GL_QUADS);
                                    gl.glVertex2f(0, -LINE_RADIUS);
                                    gl.glVertex2f(0, LINE_RADIUS);
                                    gl.glVertex2f(-hypotsize, LINE_RADIUS);
                                    gl.glVertex2f(-hypotsize, -LINE_RADIUS);
                                    gl.glEnd();
                                }
                                break;

                            }

                            gl.glPopMatrix();
                        }
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
