package teamcomm.gui.drawings;

import java.util.Map;
import javax.media.opengl.GL2;

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
