package teamcomm.gui;

import com.jogamp.nativewindow.ScalableSurface;
import com.jogamp.newt.MonitorDevice;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.awt.TextRenderer;
import data.AdvancedData;
import data.GameControlData;
import data.Rules;
import data.Teams;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import teamcomm.TeamCommunicationMonitor;
import teamcomm.data.GameState;
import teamcomm.gui.drawings.Drawing;
import teamcomm.gui.drawings.Image;
import teamcomm.gui.drawings.TextureLoader;

/**
 * Class for the 3-dimensional field view.
 *
 * @author Felix Thielke
 */
public class View3DGSV extends View3D {

    private final GLWindow window;
    private TextRenderer renderer;

    private static final int TEXT_RENDER_SIZE = 480;

    /**
     * Constructor.
     */
    public View3DGSV() {
        final GLProfile glp = GLProfile.get(GLProfile.GL2);
        final GLCapabilities caps = new GLCapabilities(glp);
        caps.setSampleBuffers(true);
        caps.setNumSamples(8);

        // Create window
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

        // Setup keyboard / mouse interaction
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
                    case KeyEvent.VK_PLUS:
                        camera.addRadius(-0.05f * ((ke.getModifiers() & KeyEvent.SHIFT_MASK) != 0 ? 2 : 1));
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_MINUS:
                        camera.addRadius(0.05f * ((ke.getModifiers() & KeyEvent.SHIFT_MASK) != 0 ? 2 : 1));
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent ke) {
                if ((ke.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
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

        glad.getGL().glClearColor(1.f, 1.f, 1.f, 1.f);

        for (final Drawing d : drawings) {
            switch (d.getClass().getName()) {
                case "teamcomm.gui.drawings.common.Ball":
                case "teamcomm.gui.drawings.common.Field":
                case "teamcomm.gui.drawings.common.Player":
                case "teamcomm.gui.drawings.common.PlayerNumber":
                case "teamcomm.gui.drawings.common.PlayerTarget":
                    d.setActive(true);
                    break;
                default:
                    d.setActive(false);
            }
        }
        camera.addRadius(1.75f);

        GameState.getInstance().setMirrored(true);

        renderer = new TextRenderer(new Font(Font.DIALOG, Font.BOLD, TEXT_RENDER_SIZE), true, true);
    }

    @Override
    public void draw(final GL2 gl) {
        // Draw Background
        switchTo2D(gl);
        try {
            final TextureLoader.Texture t = TextureLoader.getInstance().loadTexture(gl, new File("config/" + Rules.league.leagueDirectory + "/background.png"));
            Image.drawImage2DCover(gl, t, 0, 0, window.getWidth(), window.getHeight());
        } catch (IOException ex) {

        }

        // Draw 3D Drawings
        switchTo3D(gl);
        super.draw(gl);

        // Draw 2D HUD
        final GameState gs = GameState.getInstance();
        if (gs.getLastGameControlData() != null) {
            final GameControlData data = gs.getLastGameControlData();

            // Team Logos
            switchTo2D(gl);
            try {
                Image.drawImage2DContain(gl, TextureLoader.getInstance().loadTexture(gl, Teams.getIconPath(data.team[1].teamNumber)), 20, 20, window.getWidth() / 6, window.getWidth() / 6);
                Image.drawImage2DContain(gl, TextureLoader.getInstance().loadTexture(gl, Teams.getIconPath(data.team[0].teamNumber)), window.getWidth() * 5 / 6 - 20, 2, window.getWidth() / 6, window.getWidth() / 6);
            } catch (final Exception e) {
            }
            switchTo3D(gl);

            // Draw text (reversed coordinate system)
            renderer.beginRendering(window.getWidth(), window.getHeight());

            // Secondary State
            String state;
            switch (data.secGameState) {
                case GameControlData.STATE2_NORMAL:
                    if (Rules.league.dropInPlayerMode) {
                        state = "";
                    } else if (data.firstHalf == GameControlData.C_TRUE) {
                        if (data.gameState == GameControlData.STATE_FINISHED) {
                            state = "Half Time";
                        } else {
                            state = "First Half";
                        }
                    } else {
                        if (data.gameState == GameControlData.STATE_INITIAL) {
                            state = "Half Time";
                        } else {
                            state = "Second Half";
                        }
                    }
                    break;
                case GameControlData.STATE2_OVERTIME:
                    state = "Overtime";
                    break;
                case GameControlData.STATE2_PENALTYSHOOT:
                    state = "Penalty Shootout";
                    break;
                case GameControlData.STATE2_TIMEOUT:
                    state = "Time Out";
                    break;
                default:
                    state = "";
            }
            drawText2DContain(state, window.getWidth() / 2 - window.getWidth() / 6, window.getHeight() - window.getWidth() / 1920 * 64 - 10, window.getWidth() / 3, window.getWidth() / 1920 * 64, Color.black);

            // Time
            drawText2DContain(formatTime((int) data.secsRemaining), window.getWidth() / 2 - window.getWidth() / 6, window.getHeight() - window.getWidth() / 1920 * 96 - window.getWidth() / 1920 * 64 - 10, window.getWidth() / 3, window.getWidth() / 1920 * 96, Color.black);

            // Score
            drawText("" + data.team[1].score, 40 + window.getWidth() / 6, window.getHeight() - (window.getWidth() / 6.f) + window.getWidth() / 1920 * 50, window.getWidth() / 6.f, Rules.league.teamColor[data.team[1].teamColor == AdvancedData.TEAM_WHITE ? AdvancedData.TEAM_BLACK : data.team[1].teamColor]);
            drawText("" + data.team[0].score, window.getWidth() - (40 + window.getWidth() / 6 + renderer.getCharWidth((char) ('0' + data.team[0].score)) * (window.getWidth() / 6.f) / TEXT_RENDER_SIZE), window.getHeight() - (window.getWidth() / 6.f) + window.getWidth() / 1920 * 50, window.getWidth() / 6.f, Rules.league.teamColor[data.team[0].teamColor == AdvancedData.TEAM_WHITE ? AdvancedData.TEAM_BLACK : data.team[0].teamColor]);

            // Finish text drawing
            renderer.endRendering();
        }
    }

    private void drawText2DContain(final String text, final float x, final float y, final float width, final float height, final Color color) {
        final Rectangle2D bounds = renderer.getBounds(text);
        final float w = (float) (bounds.getWidth() * height / TEXT_RENDER_SIZE);
        drawText(text, x + (width - w) / 2, y, height, color);
    }

    private void drawText(final String text, final float x, final float y, final float size, final Color color) {
        renderer.setColor(color);
        renderer.draw3D(text, x, y, 0, size / TEXT_RENDER_SIZE);
    }

    private void switchTo2D(final GL2 gl) {
        final GLU glu = GLU.createGLU(gl);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluOrtho2D(0, window.getWidth(), window.getHeight(), 0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glDisable(GL2.GL_DEPTH_TEST);
        gl.glDisable(GL2.GL_CULL_FACE);
    }

    private void switchTo3D(final GL2 gl) {
        gl.glEnable(GL2.GL_CULL_FACE);
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
    }

    /**
     * Formats a time in seconds to a usual looking minutes and seconds time as
     * string.
     *
     * @param seconds Time to format in seconds.
     *
     * @return Time formated.
     */
    private String formatTime(final int seconds) {
        return (seconds < 0 ? "-" : "") + String.format("%02d:%02d", Math.abs(seconds) / 60, Math.abs(seconds) % 60);
    }
}
