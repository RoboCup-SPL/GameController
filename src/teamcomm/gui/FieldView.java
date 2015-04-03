package teamcomm.gui;

import com.jogamp.opengl.util.Animator;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.glu.GLU;
import javax.swing.JOptionPane;
import javax.swing.event.MouseInputAdapter;
import javax.xml.stream.XMLStreamException;
import teamcomm.data.RobotData;
import teamcomm.data.RobotState;
import teamcomm.gui.drawings.BallPerPlayer;
import teamcomm.gui.drawings.Drawing;
import teamcomm.gui.drawings.Field;
import teamcomm.gui.drawings.Models;
import teamcomm.gui.drawings.PerPlayer;
import teamcomm.gui.drawings.Player;
import teamcomm.gui.drawings.PlayerNumber;
import teamcomm.gui.drawings.PlayerTarget;
import teamcomm.gui.drawings.Static;

/**
 *
 * @author Felix Thielke
 */
public class FieldView implements GLEventListener {

    private static final float NEAR_PLANE = 1;
    private static final float FAR_PLANE = 20;

    private final GLCanvas canvas;
    private final Animator animator;

    /**
     * Classes of drawings that are drawn in the FieldView. Classes that are not
     * subclasses of one of PerPlayer or Static will be ignored.
     */
    private static final Class[] DRAWINGS = {
        Player.class,
        PlayerTarget.class,
        Field.class,
        BallPerPlayer.class,
        PlayerNumber.class
    };

    private final Map<String, Integer> objectLists = new HashMap<String, Integer>();
    private final List<Drawing> drawings;

    private float cameraTheta = 45;
    private float cameraPhi = 0;
    private float cameraRadius = 9;

    public FieldView() {
        // Initialize GL canvas and animator
        GLProfile glp = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(glp);
        canvas = new GLCanvas(caps);
        canvas.addGLEventListener(this);
        animator = new Animator(canvas);

        // Setup camera movement
        final MouseInputAdapter listener = new MouseInputAdapter() {

            private int[] lastPos = null;

            @Override
            public void mousePressed(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    lastPos = new int[]{e.getX(), e.getY()};
                }
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    lastPos = null;
                }
            }

            @Override
            public void mouseDragged(final MouseEvent e) {
                if (lastPos != null) {
                    final float factor = 1.0f / 5.0f;
                    cameraPhi += (e.getX() - lastPos[0]) * factor;
                    if (cameraPhi < -90) {
                        cameraPhi = -90;
                    } else if (cameraPhi > 90) {
                        cameraPhi = 90;
                    }
                    cameraTheta -= (e.getY() - lastPos[1]) * factor;
                    if (cameraTheta < 0) {
                        cameraTheta = 0;
                    } else if (cameraTheta > 90) {
                        cameraTheta = 90;
                    }
                    lastPos = new int[]{e.getX(), e.getY()};
                }
            }

        };
        canvas.addMouseListener(listener);
        canvas.addMouseMotionListener(listener);
        canvas.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(final MouseWheelEvent e) {
                cameraRadius -= e.getWheelRotation() * 0.05;
                if (cameraRadius < NEAR_PLANE) {
                    cameraRadius = NEAR_PLANE;
                } else if (cameraRadius > FAR_PLANE - 5) {
                    cameraRadius = FAR_PLANE - 5;
                }
            }
        });

        // Load drawings
        drawings = new ArrayList<Drawing>(DRAWINGS.length);
        for (Class c : DRAWINGS) {
            try {
                final Object instance = c.newInstance();
                if (instance instanceof PerPlayer || instance instanceof Static) {
                    drawings.add((Drawing) instance);
                }
            } catch (InstantiationException ex) {
            } catch (IllegalAccessException ex) {
            }
        }

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
        final GL2 gl = glad.getGL().getGL2();

        // Enable VSync
        gl.setSwapInterval(1);

        // enable depth test
        gl.glClearDepth(1.0f);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glEnable(GL.GL_DEPTH_TEST);

        // avoid rendering the backside of surfaces
        gl.glEnable(GL.GL_CULL_FACE);
        gl.glCullFace(GL.GL_BACK);
        gl.glFrontFace(GL.GL_CCW);

        // Enable lightning, texturing and smooth shading
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL.GL_MULTISAMPLE);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
        gl.glShadeModel(GL2.GL_SMOOTH);
        gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE);

        //
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);

        // Initialize projection matrix
        final GLU glu = GLU.createGLU(gl);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(40, (double) canvas.getBounds().width / (double) canvas.getBounds().height, NEAR_PLANE, FAR_PLANE);
        gl.glMatrixMode(GL2.GL_MODELVIEW);

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
        gl.glClearColor(0.65f, 0.65f, 0.7f, 1.0f);

        // Load display elements from scene file
        final Set<String> requiredModels = new HashSet<String>();
        for (final Drawing d : drawings) {
            final Models annotation = d.getClass().getAnnotation(Models.class);
            if (annotation != null) {
                for (final String name : annotation.value()) {
                    requiredModels.add(name);
                }
            }
        }
        try {
            final RoSi2Element scene = RoSi2Element.parseFile("scene/TeamComm.ros2");
            final List<RoSi2Element> elems = scene.findElements(requiredModels);
            for (RoSi2Element elem : elems) {
                objectLists.put(elem.getName(), elem.instantiate(gl).createDisplayList());
            }
        } catch (RoSi2Element.RoSi2ParseException ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error loading scene",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        } catch (XMLStreamException ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error loading scene",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error loading scene",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }

    @Override
    public void dispose(final GLAutoDrawable glad) {

    }

    @Override
    public void display(final GLAutoDrawable glad) {
        final GL2 gl = glad.getGL().getGL2();

        // Clear buffers
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        // Position camera
        gl.glLoadIdentity();
        gl.glTranslatef(0, 0, -cameraRadius);
        gl.glRotatef(-cameraTheta, 1, 0, 0);
        gl.glRotatef(cameraPhi, 0, 0, 1);

        // Lock robot states
        RobotData.getInstance().lockForReading();

        // Render drawings
        for (final Drawing d : drawings) {
            if (d.isActive()) {
                if (d instanceof Static) {
                    ((Static) d).draw(gl, objectLists);
                } else if (d instanceof PerPlayer) {
                    for (final Iterator<RobotState> iter = RobotData.getInstance().getRobotsForTeam(RobotData.TEAM_LEFT); iter.hasNext();) {
                        ((PerPlayer) d).draw(gl, objectLists, iter.next(), false);
                    }
                    gl.glRotatef(180, 0, 0, 1);
                    for (final Iterator<RobotState> iter = RobotData.getInstance().getRobotsForTeam(RobotData.TEAM_RIGHT); iter.hasNext();) {
                        ((PerPlayer) d).draw(gl, objectLists, iter.next(), true);
                    }
                    gl.glRotatef(180, 0, 0, 1);
                }
            }
        }

        // Unlock robot states
        RobotData.getInstance().unlockForReading();
    }

    @Override
    public void reshape(final GLAutoDrawable glad, final int x, final int y, final int width, final int height) {
        // Adjust projection matrix
        final GL2 gl = glad.getGL().getGL2();
        final GLU glu = GLU.createGLU(gl);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(40, (double) width / (double) height, NEAR_PLANE, FAR_PLANE);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    public List<Drawing> getDrawings() {
        return drawings;
    }
}
