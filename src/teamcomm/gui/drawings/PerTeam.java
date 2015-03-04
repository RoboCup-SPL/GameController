package teamcomm.gui.drawings;

import java.util.Map;
import javax.media.opengl.GL2;
import teamcomm.data.TeamState;

/**
 *
 * @author Felix Thielke
 */
public abstract class PerTeam extends Drawing {
    
    public abstract void draw(final GL2 gl, final Map<String, Integer> modelLists, final TeamState team);
}
