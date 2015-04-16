package teamcomm.gui.drawings;

import com.jogamp.opengl.GL2;
import java.util.Map;
import teamcomm.data.RobotState;

/**
 * Abstract base class for drawings that are drawn for each robot individually.
 *
 * @author Felix Thielke
 */
public abstract class PerPlayer extends Drawing {

    /**
     * Draws this drawing.
     *
     * @param gl OpenGL context
     * @param modelLists OpenGL display list IDs of loaded models
     * @param player robot state of the robot for which this drawing is drawn
     * @param side indicates the side the robot is playing on
     * (RobotData#TEAM_LEFT or RobotData#TEAM_RIGHT). For the right side, the
     * field is rotated so that the y axis points towards the camera, thus a
     * rotation has to be performed in order to draw text that is readable by
     * the viewer.
     */
    public abstract void draw(final GL2 gl, final Map<String, Integer> modelLists, final RobotState player, final int side);
}
