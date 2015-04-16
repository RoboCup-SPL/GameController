package teamcomm.gui.drawings;

import com.jogamp.opengl.GL2;
import java.util.Map;

/**
 * Abstract base class for drawings that are drawn once.
 *
 * @author Felix Thielke
 */
public abstract class Static extends Drawing {

    /**
     * Draws this drawing.
     *
     * @param gl OpenGL context
     * @param modelLists OpenGL display list IDs of loaded models
     */
    public abstract void draw(final GL2 gl, final Map<String, Integer> modelLists);
}
