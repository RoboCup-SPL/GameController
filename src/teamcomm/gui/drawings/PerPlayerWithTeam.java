package teamcomm.gui.drawings;

import com.jogamp.opengl.GL2;
import java.util.Collection;
import teamcomm.data.RobotState;
import teamcomm.gui.Camera;

/**
 * Abstract base class for drawings that are drawn for each robot individually, but access data from teammates.
 *
 * @author Arne Hasselbring
 */
public abstract class PerPlayerWithTeam extends Drawing {

    /**
     * Draws this drawing.
     *
     * @param gl OpenGL context
     * @param team robot states of all robots in the team
     * @param player robot state of the robot for which this drawing is drawn
     * @param camera the camera of the scene
     */
    public abstract void draw(final GL2 gl, final Collection<RobotState> team, final RobotState player, final Camera camera);
}
