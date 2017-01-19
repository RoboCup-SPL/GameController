package teamcomm.gui;

import com.jogamp.nativewindow.ScalableSurface;
import com.jogamp.newt.MonitorDevice;
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
import java.util.ArrayList;
import java.util.List;
import teamcomm.TeamCommunicationMonitor;
import teamcomm.gui.drawings.Drawing;

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
                switch (ke.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE:
                        TeamCommunicationMonitor.shutdown();
                        break;
                    case KeyEvent.VK_UP:
                        camera.addRadius(-0.05f);
                        break;
                    case KeyEvent.VK_DOWN:
                        camera.addRadius(0.05f);
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent ke) {
                if ((ke.getModifiers() & (KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK)) != 0) {
                    if (ke.getKeyCode() == KeyEvent.VK_LEFT || ke.getKeyCode() == KeyEvent.VK_RIGHT) {
                        final List<MonitorDevice> devices = window.getScreen().getMonitorDevices();
                        if (devices.size() > 1) {
                            int i;
                            for (i = 0; i < devices.size(); i++) {
                                if (devices.get(i).equals(window.getMainMonitor())) {
                                    break;
                                }
                            }
                            final List<MonitorDevice> fullscreenDevice = new ArrayList<>(1);
                            fullscreenDevice.add(devices.get((i + (ke.getKeyCode() == KeyEvent.VK_LEFT ? -1 : 1) + devices.size()) % devices.size()));
                            animator.pause();
                            window.setFullscreen(false);
                            window.setFullscreen(fullscreenDevice);
                            animator.resume();
                        }
                    }
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
    public void init(final GLAutoDrawable glad) {
        super.init(glad);
        for (final Drawing d : drawings) {
            switch (d.getClass().getName()) {
                case "teamcomm.gui.drawings.common.Ball":
                case "teamcomm.gui.drawings.common.Field":
                case "teamcomm.gui.drawings.common.GameControllerInfo":
                case "teamcomm.gui.drawings.common.Player":
                case "teamcomm.gui.drawings.common.PlayerNumber":
                case "teamcomm.gui.drawings.common.PlayerTarget":
                    d.setActive(true);
                    break;
                default:
                    d.setActive(false);
            }
        }
    }
}
