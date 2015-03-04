package teamcomm.gui;

import com.jogamp.opengl.util.Animator;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
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

/**
 *
 * @author Felix Thielke
 */
public class FieldView implements GLEventListener {
    
    private static final float NEAR_PLANE = 1;
    private static final float FAR_PLANE = 20;

    private final GLCanvas canvas;
    private final Animator animator;
    private int fieldList = -1;
    private int ballList = -1;
    private int robotBlueList = -1;
    private int robotRedList = -1;
    private int width;
    private int height;
    private float cameraTheta = 45;
    private float cameraPhi = 0;
    private float cameraRadius = 9;

    public FieldView() {
        // Initialize GL canvas
        GLProfile glp = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(glp);
        canvas = new GLCanvas(caps);
        width = canvas.getBounds().width;
        height = canvas.getBounds().height;
        canvas.addGLEventListener(this);
        animator = new Animator(canvas);

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
                    final float factor = (float) (Math.PI / 20.0);
                    cameraPhi += (e.getX() - lastPos[0]) * factor;
                    if (cameraPhi < -90) {
                        cameraPhi = -90;
                    } else if (cameraPhi > 90) {
                        cameraPhi = 90;
                    }
                    cameraTheta += (e.getY() - lastPos[1]) * factor;
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
                cameraRadius += e.getPreciseWheelRotation() * 0.01;
                if(cameraRadius < NEAR_PLANE) {
                    cameraRadius = NEAR_PLANE;
                } else if(cameraRadius > FAR_PLANE-5) {
                    cameraRadius = FAR_PLANE-5;
                }
            }
        });

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

        //
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);

        // setup light
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, FloatBuffer.wrap(new float[]{0.2f, 0.2f, 0.2f, 1.0f}));
        gl.glLightModelf(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, GL.GL_TRUE);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, FloatBuffer.wrap(new float[]{0.5f, 0.5f, 0.5f, 1.0f}));
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, FloatBuffer.wrap(new float[]{1.0f, 1.0f, 1.0f, 1.0f}));
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, FloatBuffer.wrap(new float[]{1.0f, 1.0f, 1.0f, 1.0f}));
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, FloatBuffer.wrap(new float[]{0.0f, 0.0f, 9.0f}));
        gl.glLightf(GL2.GL_LIGHT0, GL2.GL_CONSTANT_ATTENUATION, 1.0f);
        gl.glLightf(GL2.GL_LIGHT0, GL2.GL_LINEAR_ATTENUATION, 0.0f);
        gl.glLightf(GL2.GL_LIGHT0, GL2.GL_QUADRATIC_ATTENUATION, 0.0f);
        gl.glLightf(GL2.GL_LIGHT0, GL2.GL_SPOT_CUTOFF, 180.0f);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPOT_DIRECTION, FloatBuffer.wrap(new float[]{0.f, 0.f, -1.f}));
        gl.glLightf(GL2.GL_LIGHT0, GL2.GL_SPOT_EXPONENT, 0.0f);
        gl.glEnable(GL2.GL_LIGHT0);

        gl.glClearColor(0.65f, 0.65f, 0.7f, 1.0f);

        // Load display elements
        try {
            final RoSi2Element scene = RoSi2Element.parseFile("scene/TeamComm.ros2");
            fieldList = scene.findElement("field").instantiate(gl).createDisplayList();
            ballList = scene.findElement("ball").instantiate(gl).createDisplayList();
            robotBlueList = scene.findElement("robotBlue").instantiate(gl).createDisplayList();
            robotRedList = scene.findElement("robotRed").instantiate(gl).createDisplayList();
        } catch (RoSi2Element.RoSi2ParseException ex) {
            JOptionPane.showMessageDialog(null,
                    ex.getMessage(),
                    "Error loading scene",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        } catch (XMLStreamException ex) {
            Logger.getLogger(FieldView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FieldView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void dispose(final GLAutoDrawable glad) {

    }

    @Override
    public void display(final GLAutoDrawable glad) {
        // Get last messages of all robots

        // Prepare for rendering
        final GL2 gl = glad.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL.GL_MULTISAMPLE);
        gl.glEnable(GL.GL_TEXTURE_2D);

        final GLU glu = GLU.createGLU(gl);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(40, (double) width / (double) height, NEAR_PLANE, FAR_PLANE);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslatef(0, 0, -cameraRadius);
        gl.glRotatef(-cameraTheta, 1, 0, 0);
        gl.glRotatef(cameraPhi, 0, 0, 1);

        gl.glPolygonMode(GL2.GL_FRONT, GL2.GL_FILL);
        gl.glShadeModel(GL2.GL_SMOOTH);

        gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE);

        /*
         // Draw axes
         gl.glBegin(GL.GL_LINES);
         gl.glNormal3f(0, 0, 1);
         gl.glColor3f(1, 0, 0);
         gl.glVertex3f(0, 0, 0);
         gl.glVertex3f(1, 0, 0);
         gl.glColor3f(0, 1, 0);
         gl.glVertex3f(0, 0, 0);
         gl.glVertex3f(0, 1, 0);
         gl.glColor3f(0, 0, 1);
         gl.glVertex3f(0, 0, 0);
         gl.glVertex3f(0, 0, 1);
         gl.glEnd();
         */

        // Render the field
        if (fieldList >= 0) {
            gl.glCallList(fieldList);
        }

        // Render the ball
        if (ballList >= 0) {
            gl.glCallList(ballList);
        }

        // Render the robots
        if (robotBlueList >= 0) {
            gl.glTranslatef(1, 0, 0);
            gl.glRotatef(180, 0, 0, 1);
            gl.glCallList(robotBlueList);
            gl.glRotatef(-180, 0, 0, 1);
            gl.glTranslatef(-1, 0, 0);
        }
        if (robotRedList >= 0) {
            gl.glTranslatef(-1, 0, 0);
            gl.glCallList(robotRedList);
            gl.glTranslatef(1, 0, 0);
        }
    }

    @Override
    public void reshape(final GLAutoDrawable glad, final int x, final int y, final int width, final int height) {
        this.width = width;
        this.height = height;
    }
}
