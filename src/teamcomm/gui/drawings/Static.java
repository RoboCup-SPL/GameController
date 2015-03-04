package teamcomm.gui.drawings;

import java.util.Map;
import javax.media.opengl.GL2;

/**
 *
 * @author Felix Thielke
 */
public abstract class Static extends Drawing {
    
    public abstract void draw(final GL2 gl, final Map<String, Integer> modelLists);
}
