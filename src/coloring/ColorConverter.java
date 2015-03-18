package coloring;

/**
 * Defines method {@link #getColor(InternalColor)} to convert
 * {@link InternalColor} to value E.<br />
 *
 * To get the color call:
 * <pre>
 * Color red = new IntenalColor(){...}.getColor(InternalColor.RED);
 * </pre>
 *
 * @param <E>
 *            is the output of the Converter
 *
 * @author Richard Stiller
 *
 * @see InternalColor
 */
public interface ColorConverter<E> {

    /**
     * Convert a InternalColor to value E.<br />
     *
     * @param color
     *            the input InternalColor
     * @return E from the color
     */
    E getColor(InternalColor color);
}
