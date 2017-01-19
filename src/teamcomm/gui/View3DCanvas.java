package teamcomm.gui;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.event.MouseInputAdapter;
import teamcomm.PluginLoader;
import teamcomm.data.GameState;
import teamcomm.data.event.TeamEvent;
import teamcomm.gui.drawings.Drawing;

/**
 * Class for the 3-dimensional field view.
 *
 * @author Felix Thielke
 */
public class View3DCanvas extends View3D {

    private final GLCanvas canvas;
    private final JMenu drawingsMenu = new JMenu("Drawings");

    /**
     * Constructor.
     */
    public View3DCanvas() {
        // Initialize GL canvas and animator
        final GLProfile glp = GLProfile.get(GLProfile.GL2);
        final GLCapabilities caps = new GLCapabilities(glp);
        caps.setSampleBuffers(true);
        caps.setNumSamples(8);
        canvas = new GLCanvas(caps);
        autoDrawable = canvas;
        autoDrawable.addGLEventListener(this);

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
                    camera.addPhi((e.getX() - lastPos[0]) * factor);
                    camera.addTheta(-(e.getY() - lastPos[1]) * factor);
                    lastPos = new int[]{e.getX(), e.getY()};
                }
            }

        };
        canvas.addMouseListener(listener);
        canvas.addMouseMotionListener(listener);
        canvas.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(final MouseWheelEvent e) {
                camera.addRadius(-e.getWheelRotation() * 0.05f);
            }
        });

        // Start rendering
        animator = new FPSAnimator(autoDrawable, ANIMATION_FPS);
        animator.start();
    }

    @Override
    public void init(GLAutoDrawable glad) {
        super.init(glad);

        updateDrawingsMenu();
    }

    /**
     * Returns the AWT canvas the field view is drawn on.
     *
     * @return AWT canvas
     */
    public GLCanvas getCanvas() {
        return canvas;
    }

    private void updateDrawingsMenu() {
        // Clear the current menu
        drawingsMenu.removeAll();

        // Create submenus for teams
        final HashMap<Integer, JMenu> submenus = new HashMap<>();
        for (final int teamNumber : teamNumbers) {
            if (teamNumber != PluginLoader.TEAMNUMBER_COMMON) {
                final String name = GameState.getInstance().getTeamName(teamNumber, false, false);
                if (!name.equals("Unknown")) {
                    final JMenu submenu = new JMenu(name);
                    submenus.put(teamNumber, submenu);
                    drawingsMenu.add(submenu);
                }
            }
        }

        // Create menu items for drawings
        for (final Drawing d : drawings) {
            final JCheckBoxMenuItem m = new JCheckBoxMenuItem(d.getClass().getSimpleName(), d.isActive());
            m.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(final ItemEvent e) {
                    d.setActive(e.getStateChange() == ItemEvent.SELECTED);
                }
            });
            final JMenu submenu = submenus.get(d.getTeamNumber());
            if (submenu != null) {
                submenu.add(m);
            } else {
                drawingsMenu.add(m);
            }
        }

        // Remove empty submenus
        for (final JMenu submenu : submenus.values()) {
            if (submenu.getMenuComponentCount() == 0) {
                drawingsMenu.remove(submenu);
            }
        }
    }

    /**
     * Returns a JMenu controlling which drawings are visible.
     *
     * @return menu
     */
    public JMenu getDrawingsMenu() {
        return drawingsMenu;
    }

    @Override
    public void teamChanged(final TeamEvent e) {
        if (e.side != GameState.TEAM_OTHER) {
            if (teamNumbers[e.side] != (e.teamNumber == 0 ? PluginLoader.TEAMNUMBER_COMMON : e.teamNumber)) {
                teamNumbers[e.side] = e.teamNumber == 0 ? PluginLoader.TEAMNUMBER_COMMON : e.teamNumber;

                synchronized (drawings) {
                    drawings.clear();
                    drawings.addAll(PluginLoader.getInstance().getCommonDrawings());
                    if (teamNumbers[0] != PluginLoader.TEAMNUMBER_COMMON) {
                        drawings.addAll(PluginLoader.getInstance().getDrawings(teamNumbers[0]));
                    }
                    if (teamNumbers[1] != PluginLoader.TEAMNUMBER_COMMON) {
                        drawings.addAll(PluginLoader.getInstance().getDrawings(teamNumbers[1]));
                    }
                    Collections.sort(drawings, drawingComparator);
                    updateDrawingsMenu();
                }
            }
        }
        super.teamChanged(e);
    }
}
