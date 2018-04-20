package util;

/**
 * Utility class for getting unsigned from signed values.
 *
 * @author Felix Thielke
 */
public class Unsigned {

    /**
     * Convert the given byte value to an unsigned byte.
     *
     * @param b signed value
     * @return unsigned value
     */
    public static short toUnsigned(final byte b) {
        return (short) (b < 0 ? b + (1 << Byte.SIZE) : b);
    }

    /**
     * Convert the given short value to an unsigned short.
     *
     * @param s signed value
     * @return unsigned value
     */
    public static int toUnsigned(final short s) {
        return (s < 0 ? s + (1 << Short.SIZE) : s);
    }

    /**
     * Convert the given int value to an unsigned int.
     *
     * @param i signed value
     * @return unsigned value
     */
    public static long toUnsigned(final int i) {
        return i < 0 ? i + (1l << (long) Integer.SIZE) : i;
    }
}
