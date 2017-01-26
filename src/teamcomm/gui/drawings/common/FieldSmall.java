package teamcomm.gui.drawings.common;

import com.jogamp.opengl.GL2;
import teamcomm.gui.Camera;
import teamcomm.gui.drawings.RoSi2Loader;
import teamcomm.gui.drawings.Static;

/**
 * Drawing for the field.
 *
 * @author Felix Thielke
 */
public class FieldSmall extends Static {

    @Override
    protected void init(GL2 gl) {
        RoSi2Loader.getInstance().cacheModels(gl, new String[]{"fieldSmall"});
        setActive(false);
    }

    @Override
    public void draw(final GL2 gl, final Camera camera) {
        gl.glDepthFunc(GL2.GL_LESS);
        gl.glCallList(RoSi2Loader.getInstance().loadModel(gl, "fieldSmall"));
        gl.glDepthFunc(GL2.GL_LEQUAL);
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
