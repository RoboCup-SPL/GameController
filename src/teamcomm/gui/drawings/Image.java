package teamcomm.gui.drawings;

import com.jogamp.opengl.GL2;
import java.nio.FloatBuffer;

/**
 * Helper class for drawing images.
 *
 * @author Felix Thielke
 */
public class Image {

    /**
     * Draw the given texture at the given position.
     *
     * @param gl OpenGL context
     * @param texture texture to draw
     * @param centerX center X coordinate at which the image is drawn
     * @param centerY center Y coordinate at which the image is drawn
     * @param size height of the image
     */
    public static void drawImage(final GL2 gl, final TextureLoader.Texture texture, final float centerX, final float centerY, final float size) {
        final float imageWidth = (float) ((double) size * (double) texture.width / (double) texture.height);
        drawImage(gl, texture, centerX - imageWidth / 2, centerY - size / 2, imageWidth, size, false);
    }

    /**
     * Draw the given texture at the given position in the current XY-plane.
     *
     * @param gl OpenGL context
     * @param texture texture to draw
     * @param x left coordinate at which the image is drawn
     * @param y bottom coordinate at which the image is drawn
     * @param width width of the drawn image
     * @param height height of the drawn image
     */
    public static void drawImage(final GL2 gl, final TextureLoader.Texture texture, final float x, final float y, final float width, final float height) {
        drawImage(gl, texture, x, y, width, height, false);
    }

    /**
     * Draw the given texture at the given position in 2D mode.
     *
     * @param gl OpenGL context
     * @param texture texture to draw
     * @param x left coordinate at which the image is drawn
     * @param y top coordinate at which the image is drawn
     * @param width width of the drawn image
     * @param height height of the drawn image
     */
    public static void drawImage2D(final GL2 gl, final TextureLoader.Texture texture, final float x, final float y, final float width, final float height) {
        drawImage(gl, texture, x, y, width, height, true);
    }

    /**
     * Draw the given texture at the given position in 2D mode, scaling it so it
     * covers the given area while mainting the original aspect ratio.
     *
     * @param gl OpenGL context
     * @param texture texture to draw
     * @param x left coordinate at which the image is drawn
     * @param y top coordinate at which the image is drawn
     * @param width width of the drawn image
     * @param height height of the drawn image
     */
    public static void drawImage2DCover(final GL2 gl, final TextureLoader.Texture texture, final float x, final float y, final float width, final float height) {
        Image.drawImage2D(gl, texture, x, y, texture.width > texture.height ? height * texture.width / texture.height : width, texture.height > texture.width ? width * texture.height / texture.width : height);
    }

    /**
     * Draw the given texture at the given position in 2D mode, scaling it so it
     * is completely contained in the given area while mainting the original
     * aspect ratio.
     *
     * @param gl OpenGL context
     * @param texture texture to draw
     * @param x left coordinate at which the image is drawn
     * @param y top coordinate at which the image is drawn
     * @param width width of the drawn image
     * @param height height of the drawn image
     */
    public static void drawImage2DContain(final GL2 gl, final TextureLoader.Texture texture, final float x, final float y, final float width, final float height) {
        final float w = texture.height > texture.width ? height * texture.width / texture.height : width;
        final float h = texture.width > texture.height ? width * texture.height / texture.width : height;
        Image.drawImage2D(gl, texture, x + (width - w) / 2, y + (height - h) / 2, w, h);
    }

    private static void drawImage(final GL2 gl, final TextureLoader.Texture texture, final float x, final float y, final float width, final float height, final boolean flip) {
        // Set material
        gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, FloatBuffer.wrap(new float[]{0.0f, 0.0f, 0.0f, 1.0f}));
        gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 0.0f);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_EMISSION, FloatBuffer.wrap(new float[]{0.0f, 0.0f, 0.0f, 1.0f}));
        gl.glBindTexture(GL2.GL_TEXTURE_2D, texture.id);
        if (texture.hasAlpha) {
            gl.glEnable(GL2.GL_BLEND);
            gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
        }

        gl.glNormal3f(0, 0, 1);
        gl.glColor3f(1, 1, 1);
        gl.glBegin(GL2.GL_QUADS);
        gl.glTexCoord2f(0, flip ? 1 : 0);
        gl.glVertex2f(x, y + height);

        gl.glTexCoord2f(0, flip ? 0 : 1);
        gl.glVertex2f(x, y);

        gl.glTexCoord2f(1, flip ? 0 : 1);
        gl.glVertex2f(x + width, y);

        gl.glTexCoord2f(1, flip ? 1 : 0);
        gl.glVertex2f(x + width, y + height);
        gl.glEnd();

        // Unset material
        gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
        if (texture.hasAlpha) {
            gl.glDisable(GL2.GL_BLEND);
        }
    }
}
