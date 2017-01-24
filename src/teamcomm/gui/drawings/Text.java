package teamcomm.gui.drawings;

import com.jogamp.opengl.util.awt.TextRenderer;
import java.awt.Font;
import java.awt.geom.Rectangle2D;

/**
 * Helper class for drawing text.
 *
 * @author Felix Thielke
 */
public class Text {

    private static TextRenderer renderer = null;

    private static final int FONT_SIZE = 72;

    /**
     * Draw the given text centered at the given position.
     *
     * @param text text to draw
     * @param centerX center X coordinate at which the text is drawn
     * @param centerY center Y coordinate at which the text is drawn
     * @param size height of the font
     */
    public static void drawText(final String text, final float centerX, final float centerY, final float size) {
        drawText(text, centerX, centerY, size, new float[]{1, 1, 1, 1});
    }

    /**
     * Draw the given text centered at the given position.
     *
     * @param text text to draw
     * @param centerX center X coordinate at which the text is drawn
     * @param centerY center Y coordinate at which the text is drawn
     * @param size height of the font
     * @param color array with rgb or rgba values describing the color of the
     * text (color values are in the range [0.0f,1.0f])
     */
    public static void drawText(final String text, final float centerX, final float centerY, final float size, final float[] color) {
        if (renderer == null) {
            renderer = new TextRenderer(new Font(Font.DIALOG, Font.BOLD, FONT_SIZE), true, true);
        }

        renderer.begin3DRendering();
        renderer.setColor(color[0], color[1], color[2], color[3]);
        final Rectangle2D bounds = renderer.getBounds(text);
        renderer.draw3D(text, centerX - (float) (bounds.getWidth() * size / FONT_SIZE) / 2, centerY - (float) (bounds.getHeight() * size / FONT_SIZE) / 2, 0, size / FONT_SIZE);
        renderer.end3DRendering();
    }
}
