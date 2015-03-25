package coloring;

import java.awt.Color;

/**
 * To convert InternalColor to a Awt {@link Color}.<br />
 *<br />
 *To get the color call:
 *<pre>
 * Color red = new AwtColorConverter().getColor(InternalColor.RED);
 *</pre>
 *
 * @author Richard Stiller
 *
 */
public class AwtColorConverter implements ColorConverter<Color> {

    @Override
    public Color getColor(InternalColor color) {
        switch (color) {
        case RED:
            return Color.RED;
        case BLUE:
            return Color.BLUE;
        default:
            return Color.BLACK;
        }
    }
}
