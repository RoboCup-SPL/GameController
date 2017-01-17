package teamcomm.gui;

import com.jogamp.nativewindow.ScalableSurface;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.AnimatorBase;
import teamcomm.TeamCommunicationMonitor;
import teamcomm.gui.drawings.Drawing;
import teamcomm.gui.drawings.common.GameControllerInfo;

/**
 * Class for the 3-dimensional field view.
 *
 * @author Felix Thielke
 */
public class View3DGSV extends View3D {

    private final GLWindow window;

    /**
     * Constructor.
     */
    public View3DGSV() {
        final GLProfile glp = GLProfile.get(GLProfile.GL2);
        final GLCapabilities caps = new GLCapabilities(glp);
        caps.setSampleBuffers(true);
        caps.setNumSamples(8);

        window = GLWindow.create(caps);
        autoDrawable = window;
        autoDrawable.addGLEventListener(this);
        window.setSurfaceScale(new float[]{ScalableSurface.AUTOMAX_PIXELSCALE, ScalableSurface.AUTOMAX_PIXELSCALE});
        window.setTitle("GameStateVisualizer");
        window.setUndecorated(true);
        window.setResizable(false);
        window.setFullscreen(true);
        window.setPointerVisible(false);
        window.confinePointer(false);

        // Setup camera movement
        window.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(final MouseEvent me) {
                camera.addRadius(-me.getRotation()[1] * 0.05f);
            }
        });
        window.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent ke) {
                if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    TeamCommunicationMonitor.shutdown();
                }
            }
        });

        // Start rendering
        animator = new Animator();
        animator.setModeBits(false, AnimatorBase.MODE_EXPECT_AWT_RENDERING_THREAD);
        animator.setExclusiveContext(true);
        animator.setUpdateFPSFrames(ANIMATION_FPS, null);
        animator.add(window);

        window.setVisible(true);
        animator.start();
    }

    @Override
    protected void updateDrawingsMenu() {
    }

    @Override
    protected void initProjection(final GLAutoDrawable glad) {
        reshape(glad, window.getX(), window.getY(), window.getWidth(), window.getHeight());
    }

    @Override
    public void init(final GLAutoDrawable glad) {
        super.init(glad);
        for (final Drawing d : drawings) {
            if (d instanceof GameControllerInfo) {
                d.setActive(true);
            }
        }
    }
}
