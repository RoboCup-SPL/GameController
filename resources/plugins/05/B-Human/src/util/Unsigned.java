package util;

/**
 * Utility class for getting unsigned from signed values.
 *
 * @author Felix Thielke
 */
public class Unsigned {

    public static short toUnsigned(final byte b) {
        return (short) (b < 0 ? b + (1 << Byte.SIZE) : b);
    }

    public static int toUnsigned(final short s) {
        return (s < 0 ? s + (1 << Short.SIZE) : s);
    }

    public static long toUnsigned(final int i) {
        return i < 0 ? i + (1l << (long) Integer.SIZE) : i;
    }
}
