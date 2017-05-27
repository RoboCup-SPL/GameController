package bhuman.drawings;

import bhuman.message.BHumanMessage;
import bhuman.message.messages.RobotHealth;
import com.jogamp.opengl.GL2;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import javax.swing.JOptionPane;
import teamcomm.data.RobotState;
import teamcomm.gui.Camera;
import teamcomm.gui.drawings.Image;
import teamcomm.gui.drawings.PerPlayer;
import teamcomm.gui.drawings.TextureLoader;

/**
 * Custom drawing for visualizing when a robot's joints are hot.
 *
 * @author Felix Thielke
 */
public class Heat extends PerPlayer {

    private final EnumMap<RobotHealth.TemperatureStatus, String> filenames = new EnumMap<>(RobotHealth.TemperatureStatus.class);

    @Override
    protected void init(final GL2 gl) {
        if (filenames.isEmpty()) {
            filenames.put(RobotHealth.TemperatureStatus.hot, "heat_icon.png");
            filenames.put(RobotHealth.TemperatureStatus.veryHot, "fire_icon.png");
            filenames.put(RobotHealth.TemperatureStatus.criticallyHot, "fire!_icon.png");
        }
    }

    @Override
    public void draw(final GL2 gl, final RobotState rs, final Camera camera) {
        if (rs.getLastMessage() != null
                && rs.getLastMessage().valid
                && rs.getLastMessage() instanceof BHumanMessage) {
            final BHumanMessage msg = (BHumanMessage) rs.getLastMessage();
            final RobotHealth health;
            final String image;
            if (msg.message.queue != null && (health = msg.message.queue.getCachedMessage(RobotHealth.class)) != null && (image = filenames.get(health.maxJointTemperatureStatus)) != null) {
                gl.glPushMatrix();
                gl.glTranslatef(msg.pose[0] / 1000.f, msg.pose[1] / 1000.f, 1);
                camera.turnTowardsCamera(gl);
                try {
                    final File f = new File("plugins/" + (rs.getTeamNumber() < 10 ? "0" + rs.getTeamNumber() : String.valueOf(rs.getTeamNumber())) + "/resources/" + image).getAbsoluteFile();
                    Image.drawImage(gl, TextureLoader.getInstance().loadTexture(gl, f), 0, 0, 0.2f);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null,
                            "Error loading texture: " + ex.getMessage(),
                            ex.getClass().getSimpleName(),
                            JOptionPane.ERROR_MESSAGE);
                }
                gl.glPopMatrix();
            }
        }
    }

    @Override
    public boolean hasAlpha() {
        return true;
    }

    @Override
    public int getPriority() {
        return 9;
    }

}
