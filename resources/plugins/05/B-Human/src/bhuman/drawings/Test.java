package bhuman.drawings;

import com.jogamp.opengl.GL2;
import java.util.Map;
import teamcomm.data.RobotState;
import teamcomm.gui.drawings.PerPlayer;

/**
 *
 * @author Felix Thielke
 */
public class Test extends PerPlayer {

    @Override
    public void draw(GL2 gl2, Map<String, Integer> map, RobotState rs, int side) {
        
    }

    @Override
    public boolean hasAlpha() {
        return true;
    }

    @Override
    public int getPriority() {
       return 10;
    }
    
}
