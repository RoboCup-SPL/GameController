package bhuman.drawings;

import com.jogamp.opengl.GL2;
import data.SPLStandardMessage;
import teamcomm.gui.Camera;
import teamcomm.data.RobotState;
import teamcomm.gui.drawings.TextureLoader;
import teamcomm.gui.drawings.Image;
import teamcomm.gui.drawings.PerPlayer;

/**
 * Custom drawing for a small camera image.
 *
 * @author Felix Thielke
 */
public class Thumbnail extends PerPlayer {

    public Thumbnail() {
        super();
        setActive(false);
    }

    @Override
    protected void init(final GL2 gl) {
        setActive(false);
    }

    @Override
    public void draw(final GL2 gl, final RobotState rs, final Camera camera) {
        if (rs.getLastMessage() != null) {
            final SPLStandardMessage msg = rs.getLastMessage();
            final TextureLoader.Texture tex = bhuman.message.Thumbnail.getInstance(msg.teamNum + "," + msg.playerNum).getLastImage(gl);

            if (tex != null) {
                gl.glPushMatrix();
                gl.glTranslatef(msg.pose[0] / 1000.f, msg.pose[1] / 1000.f, 1.5f);
                camera.turnTowardsCamera(gl);
                Image.drawImage(gl, tex, 0, 0, 0.75f);
                gl.glPopMatrix();
            }
        }
    }

    @Override
    public boolean hasAlpha() {
        return false;
    }

    @Override
    public int getPriority() {
        return 9;
    }

}
