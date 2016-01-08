package bhuman.message;

import bhuman.message.messages.NetworkThumbnail;
import com.jogamp.opengl.GL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import teamcomm.gui.drawings.TextureLoader;
import util.Unsigned;

/**
 * Singleton class for a small thumbnail image from the upper camera of a robot.
 *
 * @author Felix Thielke
 */
public class Thumbnail {

    private static final Map<String, Thumbnail> instances = new HashMap<>();

    private byte sequence = -1;
    private short width;
    private short height;
    private final ByteBuffer imageData = ByteBuffer.allocate(256 * 256);

    private short lastWidth = -1;
    private short lastHeight = -1;
    private final ByteBuffer lastImageData = ByteBuffer.allocate(256 * 256);

    private int textureId = -1;

    private boolean newImage = false;

    public static Thumbnail getInstance(final String robot) {
        Thumbnail instance = instances.get(robot);
        if (instance == null) {
            instance = new Thumbnail();
            instances.put(robot, instance);
        }
        return instance;
    }

    public void handleMessage(final NetworkThumbnail msg) {
        synchronized (this) {
            if (msg.sequence == 0) {
                sequence = -1;
            }
            if (msg.sequence == sequence + 1) {
                sequence++;
                int i = 0;
                if (sequence == 0) {
                    imageData.clear();
                    width = Unsigned.toUnsigned(msg.data[0]);
                    height = Unsigned.toUnsigned(msg.data[1]);
                    i = 2;
                }
                for (; i < msg.data.length; i++) {
                    final byte value = (byte) (msg.data[i] & 0xF0);
                    int count = msg.data[i] & 0x0F;
                    if (count == 0) {
                        count = 16;
                    }
                    for (int c = 0; c < count; c++) {
                        imageData.put(value);
                    }
                }

                if (imageData.position() == width * height) {
                    sequence = -1;
                    newImage = true;

                    lastWidth = width;
                    lastHeight = height;
                    lastImageData.clear();
                    imageData.limit(imageData.position());
                    imageData.rewind();
                    lastImageData.put(imageData);
                    lastImageData.rewind();
                }
            }
        }
    }

    public TextureLoader.Texture getLastImage(final GL gl) {
        synchronized (this) {
            if (newImage) {
                // Allocate texture if needed
                if (textureId == -1) {
                    final IntBuffer texIds = IntBuffer.allocate(1);
                    gl.glGenTextures(1, texIds);
                    textureId = texIds.get(0);
                }

                // Load texture into GL
                gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
                gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_LUMINANCE8, lastWidth, lastHeight, 0, GL.GL_LUMINANCE, GL.GL_UNSIGNED_BYTE, lastImageData);
                gl.glBindTexture(GL.GL_TEXTURE_2D, 0);

                newImage = false;
            }

            return textureId == -1 ? null : new TextureLoader.Texture(textureId, false, lastWidth, lastHeight);
        }
    }
}
