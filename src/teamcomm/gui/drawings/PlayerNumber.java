package teamcomm.gui.drawings;

import com.jogamp.opengl.GL2;
import data.SPLStandardMessage;
import java.util.Map;
import teamcomm.data.RobotState;

@Models({"number1", "number2", "number3", "number4", "number5"})
/**
 *
 * @author Felix Thielke
 */
public class PlayerNumber extends PerPlayer {

    @Override
    public void draw(final GL2 gl, final Map<String, Integer> modelLists, final RobotState player, final boolean inverted) {
        final SPLStandardMessage msg = player.getLastMessage();
        if (msg != null) {
            gl.glPushMatrix();

            gl.glTranslatef(msg.pose[0] / 1000.f, msg.pose[1] / 1000.f, 0.8f);
            
            if(inverted) {
                gl.glRotatef(180, 0, 0, 1);
            }
            gl.glCallList(modelLists.get("number" + msg.playerNum));

            gl.glPopMatrix();
        }
    }

}
