package teamcomm.gui;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 *
 * @author Felix
 */
public class TextureLoader {

    private static final TextureLoader instance = new TextureLoader();
    private final Map<GL, Map<String, Texture>> textures = new HashMap<GL, Map<String, Texture>>();

    private TextureLoader() {
    }

    public static TextureLoader getInstance() {
        return instance;
    }

    public Texture loadTexture(final GL gl, final File filename) throws IOException {
        Map<String, Texture> map = textures.get(gl);
        if(map == null) {
            map = new HashMap<String, Texture>();
            textures.put(gl, map);
        }

        Texture tex = map.get(filename.getAbsolutePath());
        if (tex != null) {
            return tex;
        }
        final BufferedImage img = ImageIO.read(filename);
        final int[] imageData = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
        final boolean hasAlpha = img.getColorModel().hasAlpha();
        final ByteBuffer buffer = ByteBuffer.allocate(imageData.length * (hasAlpha ? 4 : 3));
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (final int c : imageData) {
            if (hasAlpha) {
                buffer.putInt(c);
            } else {
                buffer.put((byte) (c & 0xFF));
                buffer.put((byte) ((c >> 8) & 0xFF));
                buffer.put((byte) ((c >> 16) & 0xFF));
            }
        }
        buffer.rewind();
        // Allocate texture
        final IntBuffer texIds = IntBuffer.allocate(1);
        gl.glGenTextures(1, texIds);
        final int textureId = texIds.get(0);
        // Load texture into GL
        gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, hasAlpha ? 4 : 3, img.getWidth(), img.getHeight(), 0, hasAlpha ? GL.GL_BGRA : GL2.GL_BGR, GL.GL_UNSIGNED_BYTE, buffer);
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        tex = new Texture(textureId, hasAlpha);
        map.put(filename.getAbsolutePath(), tex);
        return tex;
    }

    public static class Texture {

        public final int id;
        public final boolean hasAlpha;

        public Texture(final int id, final boolean hasAlpha) {
            this.id = id;
            this.hasAlpha = hasAlpha;
        }

    }
}
