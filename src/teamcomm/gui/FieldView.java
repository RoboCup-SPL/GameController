package teamcomm.gui;

import com.jogamp.opengl.util.Animator;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

/**
 *
 * @author Felix Thielke
 */
public class FieldView implements GLEventListener {

    private final GLCanvas canvas;
    private final Animator animator;

    public FieldView() {
        // Initialize GL canvas
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        canvas = new GLCanvas(caps);
        canvas.addGLEventListener(this);
        animator = new Animator(canvas);
        
        // Load display elements
        
        
        // Start rendering
        animator.start();
    }
    
    public void terminate() {
        animator.stop();
        canvas.destroy();
    }

    public GLCanvas getCanvas() {
        return canvas;
    }

    @Override
    public void init(final GLAutoDrawable glad) {
        // Enable VSync
        glad.getGL().setSwapInterval(1);
    }

    @Override
    public void dispose(final GLAutoDrawable glad) {

    }

    @Override
    public void display(final GLAutoDrawable glad) {
        // Get last messages of all robots
        
        
        // Prepare for rendering
        final GL gl = glad.getGL();
        gl.glClearColor(0.8f, 0.8f, 0.8f, 1.0f);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        
        // Render the field
        
    }

    @Override
    public void reshape(final GLAutoDrawable glad, final int i, final int i1, final int i2, final int i3) {

    }
}
