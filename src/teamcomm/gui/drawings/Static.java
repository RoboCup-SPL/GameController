package teamcomm.gui.drawings;

import com.jogamp.opengl.GL2;
import java.util.Map;

/**
 *
 * @author Felix Thielke
 */
public abstract class Static extends Drawing {
    
    public abstract void draw(final GL2 gl, final Map<String, Integer> modelLists);
}
