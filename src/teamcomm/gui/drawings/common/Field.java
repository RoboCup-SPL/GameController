package teamcomm.gui.drawings.common;

import com.jogamp.opengl.GL2;
import java.util.Map;
import teamcomm.gui.drawings.Models;
import teamcomm.gui.drawings.Static;

@Models({"field"})
/**
 *
 * @author Felix Thielke
 */
public class Field extends Static {

    @Override
    public void draw(final GL2 gl, final Map<String, Integer> modelLists) {
        gl.glCallList(modelLists.get("field"));
    }
    
    @Override
    public boolean hasAlpha() {
        return true;
    }

    @Override
    public int getPriority() {
        return 1000;
    }

}
