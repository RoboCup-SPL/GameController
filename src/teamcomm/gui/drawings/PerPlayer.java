package teamcomm.gui.drawings;

import com.jogamp.opengl.GL2;
import java.util.Map;
import teamcomm.data.RobotState;

/**
 *
 * @author Felix Thielke
 */
public abstract class PerPlayer extends Drawing {

    public abstract void draw(final GL2 gl, final Map<String, Integer> modelLists, final RobotState player, final boolean inverted);
}
