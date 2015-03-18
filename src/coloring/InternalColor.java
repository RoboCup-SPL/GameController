package coloring;

/**
 * Enum with self defined Colors for compatibility with Android.<br />
 * <br />
 * To remove Awt-Color (not support in Android) form the Rules use this Enum for
 * predefined Colors.<br />
 * With a {@link ColorConverter} you can get other Values based on IntenalColor.<br />
 * <br />
 * Additional implements to Methods
 * {@link #getColor(InternalColor, ColorConverter)} (static) and
 * {@link #getColor(ColorConverter)} (intern).<br />
 * To get Awt Color
 * <pre>
 * ColorConverter&lt;Color&gt; converter = new AwtColorConverter();
 * Color red = InternalColor.RED.getColor(converter);
 * // or
 * Color red2 = InternalColor.getColor(RED, converter);
 * </pre>
 *
 * @author Richard Stiller
 *
 * @see ColorConverter
 *
 */
public enum InternalColor {

    /** Teamcolor Red */
    RED,

    /** Teamcolor Blue */
    BLUE;

    /**
     * Return the output of the InternalColor from the ColorConverter.
     *
     * @param color
     *            is a InternalColor to convert a other output
     * @param converter
     *            to convert the InternalColor
     * @return Output E defined by the Converter
     */
    public static <E> E getColor(InternalColor color,
            ColorConverter<E> converter) {
        return converter.getColor(color);
    }

    /**
     * Return the output E of this Color from the ColorConverter.
     *
     * @param converter
     *            to convert the InternalColor in a other Value E
     * @return Output E defined by the Converter
     */
    public <E> E getColor(ColorConverter<E> converter) {
        return converter.getColor(this);
    }
}
