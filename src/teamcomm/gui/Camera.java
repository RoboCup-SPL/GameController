package teamcomm.gui;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;

/**
 *
 * @author Felix Thielke
 */
public class Camera {

    private static final float NEAR_PLANE = 1;
    private static final float FAR_PLANE = 20;

    private float theta = 45;
    private float phi = 0;
    private float radius = 9;
    private boolean flipped = false;

    public void flip() {
        flipped = !flipped;
    }

    public void addTheta(final float amount) {
        theta += amount;
        if (theta < 0) {
            theta = 0;
        } else if (theta > 90) {
            theta = 90;
        }
    }

    public void addPhi(final float amount) {
        phi += amount;
        if (phi < -90) {
            phi = -90;
        } else if (phi > 90) {
            phi = 90;
        }
    }

    public void addRadius(final float amount) {
        radius += amount;
        if (radius < NEAR_PLANE) {
            radius = NEAR_PLANE;
        } else if (radius > FAR_PLANE - 5) {
            radius = FAR_PLANE - 5;
        }
    }

    public float getTheta() {
        return theta;
    }

    public float getPhi() {
        return phi;
    }

    public float getRadius() {
        return radius;
    }

    public boolean isFlipped() {
        return flipped;
    }

    public void positionCamera(final GL2 gl) {
        gl.glTranslatef(0, 0, -radius);
        gl.glRotatef(-theta, 1, 0, 0);
        gl.glRotatef(phi, 0, 0, 1);
    }
    
    public void turnTowardsCamera(final GL2 gl) {
        gl.glRotatef((flipped ? 180 : 0) - phi, 0, 0, 1);
        gl.glRotatef(theta, 1, 0, 0);
    }

    public void setupFrustum(final GL2 gl, final double displayRatio) {
        final GLU glu = GLU.createGLU(gl);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(40, displayRatio, NEAR_PLANE, FAR_PLANE);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }
}
