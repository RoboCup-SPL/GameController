package teamcomm.gui.drawings;

import com.jogamp.opengl.GL2;
import java.util.Map;

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

}
