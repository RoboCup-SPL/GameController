package teamcomm.gui;

import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.AnimatorBase;
import common.Log;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import teamcomm.PluginLoader;
import teamcomm.data.GameState;
import teamcomm.data.RobotState;
import teamcomm.data.event.TeamEvent;
import teamcomm.data.event.TeamEventListener;
import teamcomm.gui.drawings.Drawing;
import teamcomm.gui.drawings.PerPlayer;
import teamcomm.gui.drawings.Static;

/**
 * Abstract class for the 3-dimensional field views.
 *
 * @author Felix Thielke
 */
public abstract class View3D implements GLEventListener, TeamEventListener {

    public static final int ANIMATION_FPS = 30;

    protected AnimatorBase animator;
    protected GLAutoDrawable autoDrawable;
    protected final Camera camera = new Camera();

    protected final int[] teamNumbers = new int[]{PluginLoader.TEAMNUMBER_COMMON, PluginLoader.TEAMNUMBER_COMMON};
    protected final Set<RobotState> leftRobots = new HashSet<>();
    protected final Set<RobotState> rightRobots = new HashSet<>();

    private int width = 0;
    private int height = 0;

    protected static final Comparator<Drawing> drawingComparator = new Comparator<Drawing>() {
        @Override
        public int compare(final Drawing o1, final Drawing o2) {
            // opaque objects have priority over transparent objects
            if (o1.hasAlpha() && !o2.hasAlpha()) {
                return 1;
            }
            if (!o1.hasAlpha() && o2.hasAlpha()) {
                return -1;
            }

            // higher priorities are drawn earlier
            return o2.getPriority() - o1.getPriority();
        }
    };
    protected final List<Drawing> drawings = new LinkedList<>();

    /**
     * Terminates the field view.
     */
    public void terminate() {
        animator.stop();
        autoDrawable.destroy();
    }

    /**
     * Initializes the field view.
     *
     * @param glad drawable
     */
    @Override
    public void init(final GLAutoDrawable glad) {
        final GL2 gl = glad.getGL().getGL2();

        // enable depth test
        gl.glClearDepth(1.0f);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glEnable(GL.GL_DEPTH_TEST);

        // avoid rendering the backside of surfaces
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glCullFace(GL.GL_BACK);
        gl.glFrontFace(GL.GL_CCW);

        // Enable lighting, texturing and smooth shading
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL.GL_MULTISAMPLE);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
        gl.glShadeModel(GL2.GL_SMOOTH);
        gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE);

        //
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);

        // Initialize projection matrix
        if (autoDrawable instanceof GLCanvas) {
            final GLCanvas canvas = (GLCanvas) autoDrawable;
            reshape(glad, canvas.getBounds().x, canvas.getBounds().y, canvas.getBounds().width, canvas.getBounds().height);
        } else if (autoDrawable instanceof GLWindow) {
            final GLWindow window = (GLWindow) autoDrawable;
            reshape(glad, window.getX(), window.getY(), window.getWidth(), window.getHeight());
        } else {
            reshape(glad, 0, 0, 640, 480);
        }

        // setup light
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, FloatBuffer.wrap(new float[]{0.2f, 0.2f, 0.2f, 1.0f}));
        gl.glLightModelf(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, GL.GL_TRUE);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, FloatBuffer.wrap(new float[]{0.5f, 0.5f, 0.5f, 1.0f}));
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, FloatBuffer.wrap(new float[]{1.0f, 1.0f, 1.0f, 1.0f}));
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, FloatBuffer.wrap(new float[]{1.0f, 1.0f, 1.0f, 1.0f}));
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, FloatBuffer.wrap(new float[]{0.0f, 0.0f, 9.0f, 1.0f}));
        gl.glLightf(GL2.GL_LIGHT0, GL2.GL_CONSTANT_ATTENUATION, 1.0f);
        gl.glLightf(GL2.GL_LIGHT0, GL2.GL_LINEAR_ATTENUATION, 0.0f);
        gl.glLightf(GL2.GL_LIGHT0, GL2.GL_QUADRATIC_ATTENUATION, 0.0f);
        gl.glLightf(GL2.GL_LIGHT0, GL2.GL_SPOT_CUTOFF, 180.0f);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPOT_DIRECTION, FloatBuffer.wrap(new float[]{0.f, 0.f, -1.f}));
        gl.glLightf(GL2.GL_LIGHT0, GL2.GL_SPOT_EXPONENT, 0.0f);
        gl.glEnable(GL2.GL_LIGHT0);

        // Set clear color
        gl.glClearColor(0.6f, 0.6f, 0.65f, 1.0f);

        // Setup common drawings
        drawings.addAll(PluginLoader.getInstance().getCommonDrawings());
        Collections.sort(drawings, drawingComparator);
        for (final Drawing d : drawings) {
            d.initialize(gl);
        }

        // Listen for robot events
        GameState.getInstance().addListener(this);
    }

    /**
     * Performs cleanup after the canvas was disposed.
     *
     * @param glad drawable
     */
    @Override
    public void dispose(final GLAutoDrawable glad) {
    }

    /**
     * Draws the field view.
     *
     * @param glad drawable
     */
    @Override
    public final void display(final GLAutoDrawable glad) {
        final GL2 gl = glad.getGL().getGL2();

        // Clear buffers
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        // Draw
        draw(gl);
    }

    protected void draw(final GL2 gl) {
        // Position camera
        gl.glLoadIdentity();
        camera.positionCamera(gl);

        // Render drawings
        synchronized (drawings) {
            final Iterator<Drawing> it = drawings.iterator();
            while (it.hasNext()) {
                final Drawing d = it.next();

                // Initialize if needed
                try {
                    d.initialize(gl);
                } catch (final Throwable e) {
                    it.remove();
                    Log.error(e.getClass().getSimpleName() + " was thrown while initializing drawing " + d.getClass().getName() + ": " + e.getMessage());
                    continue;
                }

                // Draw
                if (d.isActive()) {
                    if (d instanceof Static) {
                        try {
                            ((Static) d).draw(gl, camera);
                        } catch (final Throwable e) {
                            Log.error(e.getClass().getSimpleName() + " was thrown while drawing custom drawing " + d.getClass().getName() + ": " + e.getMessage());
                        }
                    } else if (d instanceof PerPlayer) {
                        if (d.getTeamNumber() == PluginLoader.TEAMNUMBER_COMMON || d.getTeamNumber() == teamNumbers[GameState.TEAM_LEFT]) {
                            synchronized (leftRobots) {
                                for (final RobotState r : leftRobots) {
                                    try {
                                        ((PerPlayer) d).draw(gl, r, camera);
                                    } catch (final Throwable e) {
                                        Log.error(e.getClass().getSimpleName() + " was thrown while drawing custom drawing " + d.getClass().getName() + ": " + e.getMessage());
                                    }
                                }
                            }
                        }
                        if (d.getTeamNumber() == PluginLoader.TEAMNUMBER_COMMON || d.getTeamNumber() == teamNumbers[GameState.TEAM_RIGHT]) {
                            camera.flip(gl);
                            synchronized (rightRobots) {
                                for (final RobotState r : rightRobots) {
                                    try {
                                        ((PerPlayer) d).draw(gl, r, camera);
                                    } catch (final Throwable e) {
                                        Log.error(e.getClass().getSimpleName() + " was thrown while drawing custom drawing " + d.getClass().getName() + ": " + e.getMessage());
                                    }
                                }
                            }
                            camera.flip(gl);
                        }
                    }
                }
            }
        }
    }

    /**
     * Method that gets called on a reshape event of the window / AWT canvas.
     * Adjusts the viewing frustum of the field view for the new shape.
     *
     * @param glad drawable
     * @param x new x offset
     * @param y new y offset
     * @param width new width
     * @param height new height
     */
    @Override
    public void reshape(final GLAutoDrawable glad, final int x, final int y, final int width, final int height) {
        // Adjust projection matrix
        if (this.width != width || this.height != height) {
            this.width = width;
            this.height = height;
            glad.getGL().getGL2().glViewport(0, 0, width, height);
            camera.setupFrustum(glad.getGL().getGL2(), (double) width / (double) height);
        }
    }

    @Override
    public void teamChanged(final TeamEvent e) {
        if (e.side == GameState.TEAM_LEFT) {
            synchronized (leftRobots) {
                leftRobots.clear();
                leftRobots.addAll(e.players);
            }
        } else if (e.side == GameState.TEAM_RIGHT) {
            synchronized (rightRobots) {
                rightRobots.clear();
                rightRobots.addAll(e.players);
            }
        }
    }
}
