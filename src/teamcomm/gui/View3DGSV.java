package teamcomm.gui;

import com.jogamp.nativewindow.ScalableSurface;
import com.jogamp.newt.MonitorDevice;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.AnimatorBase;
import com.jogamp.opengl.util.awt.TextRenderer;
import data.AdvancedData;
import data.GameControlData;
import data.Rules;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
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

    private static final int RENDERER_SECSTATE = 0;
    private static final int RENDERER_STATE = 1;
    private static final int RENDERER_TIME = 2;
    private static final int RENDERER_SCORE = 3;

    private final GLWindow window;
    private final TextRenderer[] textRenderers = new TextRenderer[4];
    private final int[] textRendererSizes = new int[4];

    private static final float NEAR_FIELD_BORDER_Y = -3.7f;

    private enum BackgroundAlign {
        NONE(""),
        BOTTOM("b"),
        LEFT("l"),
        RIGHT("r"),
        TOP("t");

        public final String suffix;

        BackgroundAlign(final String s) {
            suffix = s;
        }
    }

    private TextureLoader.Texture background;
    private BackgroundAlign backgroundAlign;

    /**
     * Constructor.
     *
     * @param forceWindowed force the GSV window into windowed mode
     */
    public View3DGSV(final boolean forceWindowed) {
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
        if (!forceWindowed) {
            // Display on external display if possible.
            final GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            final GraphicsDevice device = devices[devices[0].equals(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()) ? devices.length - 1 : 0];
            final GraphicsConfiguration configuration = device.getDefaultConfiguration();
            window.setPosition((int) configuration.getBounds().getX(), (int) configuration.getBounds().getY());
            window.setUndecorated(true);
            window.setResizable(false);
            window.setFullscreen(true);
            window.setPointerVisible(false);
            window.confinePointer(false);
        } else {
            window.setSize(640, 480);
            window.setUndecorated(false);
            //window.setResizable(true);
            window.setFullscreen(false);
            window.setPointerVisible(true);
            window.confinePointer(false);
        }

        // Setup keyboard / mouse interaction
        window.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(final MouseEvent me) {
                camera.addRadius(-me.getRotation()[1] * 0.05f);
                camera.shiftToBottom(NEAR_FIELD_BORDER_Y);
            }
        });
        window.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent ke) {
                switch (ke.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE:
                        TeamCommunicationMonitor.shutdown();
                        window.setVisible(false);
                        break;
                    case KeyEvent.VK_F2:
                        TeamCommunicationMonitor.switchToTCM();
                        window.setVisible(false);
                        break;
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_PLUS:
                        camera.addRadius(-0.05f * ((ke.getModifiers() & KeyEvent.SHIFT_MASK) != 0 ? 2 : 1));
                        camera.shiftToBottom(NEAR_FIELD_BORDER_Y);
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_MINUS:
                        camera.addRadius(0.05f * ((ke.getModifiers() & KeyEvent.SHIFT_MASK) != 0 ? 2 : 1));
                        camera.shiftToBottom(NEAR_FIELD_BORDER_Y);
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent ke) {
                if ((ke.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
                    if (!forceWindowed && (ke.getKeyCode() == KeyEvent.VK_LEFT || ke.getKeyCode() == KeyEvent.VK_RIGHT)) {
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
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDestroyed(final WindowEvent we) {
                TeamCommunicationMonitor.shutdown();
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
            if (d.getClass().getName().startsWith("teamcomm.gui.drawings.common")) {
                switch (d.getClass().getSimpleName()) {
                    case "Ball":
                    case "Field":
                    case "Player":
                    case "PlayerNumber":
                        d.setActive(true);
                        break;
                    default:
                        d.setActive(false);
                }
            } else {
                d.setActive(true);
            }
        }
        camera.addRadius(4.f);
        camera.shiftToBottom(NEAR_FIELD_BORDER_Y);

        GameState.getInstance().setMirrored(true);

        loaded:
        for (final BackgroundAlign align : BackgroundAlign.values()) {
            for (final String format : new String[]{".png", ".jpeg", ".jpg"}) {
                try {
                    background = TextureLoader.getInstance().loadTexture(glad.getGL(), new File("config/" + Rules.league.leagueDirectory + "/background" + align.suffix + format));
                    backgroundAlign = align;
                    break loaded;
                } catch (final IOException e) {
                } catch (final Exception e) {
                    System.err.println("The background image " + "config/" + Rules.league.leagueDirectory + "/background" + align.suffix + format + " could not be loaded.\nUsually this happens if its width or height is not an even number.");
                }
            }
        }
    }

    @Override
    public void reshape(final GLAutoDrawable glad, final int x, final int y, final int width, final int height) {
        super.reshape(glad, x, y, width, height);

        textRendererSizes[RENDERER_STATE] = 60 * window.getWidth() / 1920;
        textRendererSizes[RENDERER_SECSTATE] = 80 * window.getWidth() / 1920;
        textRendererSizes[RENDERER_TIME] = 120 * window.getWidth() / 1920;
        textRendererSizes[RENDERER_SCORE] = window.getWidth() / 6;
        for (int i = 0; i < textRenderers.length; i++) {
            textRenderers[i] = new TextRenderer(new Font(Font.DIALOG, 0, textRendererSizes[i]), true, true);
        }
    }

    @Override
    public void terminate() {
        super.terminate();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (final WindowListener wl : window.getWindowListeners()) {
                    window.removeWindowListener(wl);
                }
                window.destroy();
            }
        });
    }

    @Override
    public void draw(final GL2 gl) {
        // Draw Background
        switchTo2D(gl);
        if (background != null) {
            switch (backgroundAlign) {
                case NONE:
                    Image.drawImage2DCover(gl, background, 0, 0, window.getWidth(), window.getHeight());
                    break;
                case LEFT:
                    Image.drawImage2DContain(gl, background, 0, 0, background.width * window.getHeight() / background.height, window.getHeight());
                    break;
                case RIGHT:
                    Image.drawImage2DContain(gl, background, window.getWidth() - background.width * window.getHeight() / background.height, 0, background.width * window.getHeight() / background.height, window.getHeight());
                    break;
                case TOP:
                    Image.drawImage2DContain(gl, background, 0, 0, window.getWidth(), background.height * window.getWidth() / background.width);
                    break;
                case BOTTOM:
                    Image.drawImage2DContain(gl, background, 0, window.getHeight() - background.height * window.getWidth() / background.width, window.getWidth(), background.height * window.getWidth() / background.width);
                    break;
            }
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
                Image.drawImage2DContain(gl, TeamLogoLoader.getInstance().getTeamLogoTexture(gl, data.team[1].teamNumber), 20 * window.getWidth() / 1920, 20 * window.getWidth() / 1920, window.getWidth() / 6, window.getWidth() / 6);
            } catch (final Exception e) {
            }
            try {
                Image.drawImage2DContain(gl, TeamLogoLoader.getInstance().getTeamLogoTexture(gl, data.team[0].teamNumber), window.getWidth() - window.getWidth() / 6 - 20 * window.getWidth() / 1920, 20 * window.getWidth() / 1920, window.getWidth() / 6, window.getWidth() / 6);
            } catch (final Exception e) {
            }
            switchTo3D(gl);

            // Secondary state
            String state;
            switch (data.secGameState) {
                case GameControlData.STATE2_NORMAL:
                    if (data.firstHalf == GameControlData.C_TRUE) {
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
            textRenderers[RENDERER_SECSTATE].beginRendering(window.getWidth(), window.getHeight());
            drawTextCenter(textRenderers[RENDERER_SECSTATE], state, window.getHeight() - textRendererSizes[RENDERER_SECSTATE], Color.black);
            textRenderers[RENDERER_SECSTATE].endRendering();

            // Time
            textRenderers[RENDERER_TIME].beginRendering(window.getWidth(), window.getHeight());
            drawText(textRenderers[RENDERER_TIME], formatTime((int) data.secsRemaining), (int) Math.round((window.getWidth() - textRenderers[RENDERER_TIME].getBounds("00:00").getWidth()) / 2 - (data.secsRemaining < 0 ? textRenderers[RENDERER_TIME].getCharWidth('-') : 0)), window.getHeight() - textRendererSizes[RENDERER_SECSTATE] - textRendererSizes[RENDERER_TIME], Color.black);
            textRenderers[RENDERER_TIME].endRendering();

            // State
            switch (data.gameState) {
                case GameControlData.STATE_INITIAL:
                    state = "Initial";
                    break;
                case GameControlData.STATE_READY:
                    state = "Ready";
                    break;
                case GameControlData.STATE_SET:
                    state = "Set";
                    break;
                case GameControlData.STATE_PLAYING:
                    state = "Playing";
                    break;
                case GameControlData.STATE_FINISHED:
                    state = "Finished";
                    break;
                default:
                    state = "";
            }
            textRenderers[RENDERER_STATE].beginRendering(window.getWidth(), window.getHeight());
            drawTextCenter(textRenderers[RENDERER_STATE], state, window.getHeight() - textRendererSizes[RENDERER_SECSTATE] - textRendererSizes[RENDERER_TIME] - window.getHeight() * 15 / 1080 - textRendererSizes[RENDERER_STATE], Color.black);

            // Secondary time
            if (data.secondaryTime > 0) {
                drawTextCenter(textRenderers[RENDERER_STATE], formatTime(data.secondaryTime), window.getHeight() - textRendererSizes[RENDERER_SECSTATE] - textRendererSizes[RENDERER_TIME] - window.getHeight() * 15 / 1080 - textRendererSizes[RENDERER_STATE] * 2, Color.black);
            }
            textRenderers[RENDERER_STATE].endRendering();

            if (data.gameState == GameControlData.STATE_READY || data.gameState == GameControlData.STATE_SET) {
                switchTo2D(gl);
                final float iconSize = textRendererSizes[RENDERER_STATE] * 2 / 3;
                try {
                    final float x;
                    if (data.kickingTeam == data.team[1].teamNumber) {
                        x = (float) ((window.getWidth() - textRenderers[RENDERER_STATE].getBounds(state).getWidth()) / 2) - window.getWidth() * 20 / 1920 - iconSize;
                    } else if (data.kickingTeam == data.team[0].teamNumber) {
                        x = (float) ((window.getWidth() + textRenderers[RENDERER_STATE].getBounds(state).getWidth()) / 2) + window.getWidth() * 20 / 1920;
                    } else {
                        x = -iconSize * 2;
                    }
                    Image.drawImage2D(gl, TextureLoader.getInstance().loadTexture(gl, new File("scene/Textures/ball_icon.png")), x, textRendererSizes[RENDERER_SECSTATE] + textRendererSizes[RENDERER_TIME] + window.getHeight() * 15 / 1080 + textRendererSizes[RENDERER_STATE] * 2 / 3 - iconSize / 2, iconSize, iconSize);
                } catch (final IOException ex) {
                }
                switchTo3D(gl);
            }

            // Score
            textRenderers[RENDERER_SCORE].beginRendering(window.getWidth(), window.getHeight());
            drawText(textRenderers[RENDERER_SCORE], "" + data.team[1].score, window.getWidth() / 6 + 40 * window.getWidth() / 1920, window.getHeight() - 20 * window.getWidth() / 1920 - (textRendererSizes[RENDERER_SCORE] + (int) textRenderers[RENDERER_SCORE].getBounds("0").getHeight()) / 2, Rules.league.teamColor[data.team[1].teamColor == AdvancedData.TEAM_WHITE ? AdvancedData.TEAM_BLACK : data.team[1].teamColor]);
            drawText(textRenderers[RENDERER_SCORE], "" + data.team[0].score, window.getWidth() - window.getWidth() / 6 - 40 * window.getWidth() / 1920 - (int) Math.max(textRenderers[RENDERER_SCORE].getBounds("" + data.team[0].score).getWidth(), textRenderers[RENDERER_SCORE].getCharWidth('0')), window.getHeight() - 20 * window.getWidth() / 1920 - (textRendererSizes[RENDERER_SCORE] + (int) textRenderers[RENDERER_SCORE].getBounds("0").getHeight()) / 2, Rules.league.teamColor[data.team[0].teamColor == AdvancedData.TEAM_WHITE ? AdvancedData.TEAM_BLACK : data.team[0].teamColor]);
            textRenderers[RENDERER_SCORE].endRendering();

            // Penalty shots
            if (data.team[0].penaltyShot > 0 || data.team[1].penaltyShot > 0) {
                switchTo2D(gl);
                final GLU glu = GLU.createGLU(gl);
                final GLUquadric q = glu.gluNewQuadric();
                for (int i = 0; i < 2; i++) {
                    gl.glColor4fv(Rules.league.teamColor[data.team[i].teamColor].getComponents(new float[4]), 0);
                    if (i == 0) {
                        gl.glPushMatrix();
                        gl.glTranslatef(window.getWidth() * 11 / 12, window.getWidth() / 6 + window.getHeight() * 20 / 1080, 0);
                    } else {
                        gl.glTranslatef(window.getWidth() / 12, window.getWidth() / 6 + window.getHeight() * 20 / 1080, 0);
                    }
                    for (int j = 0; j < data.team[i].penaltyShot; j++) {
                        gl.glTranslatef(0, 2.5f * window.getWidth() * 32 / 1920, 0);
                        if ((data.team[i].singleShots & (1 << j)) != 0) {
                            glu.gluDisk(q, 0, window.getWidth() * 32 / 1920, 16, 16);
                        } else {
                            glu.gluDisk(q, window.getWidth() * 28 / 1920, window.getWidth() * 32 / 1920, 16, 16);
                        }
                    }
                    if (i == 0) {
                        gl.glPopMatrix();
                    }
                }
                glu.gluDeleteQuadric(q);
                switchTo3D(gl);
            }
        }
    }

    private void drawTextCenter(final TextRenderer renderer, final String text, final int y, final Color color) {
        drawText(renderer, text, (int) Math.round((window.getWidth() - renderer.getBounds(text).getWidth()) / 2), y, color);
    }

    private void drawText(final TextRenderer renderer, final String text, final int x, final int y, final Color color) {
        renderer.setColor(color);
        renderer.draw(text, x, y);
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
