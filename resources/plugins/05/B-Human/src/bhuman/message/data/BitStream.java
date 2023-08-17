package bhuman.message.data;

import java.nio.ByteBuffer;
import util.Unsigned;

/**
 * A bitwise stream from which basic data types can be read.
 *
 * @author Arne Hasselbring
 */
public class BitStream {

    private final ByteBuffer source;
    private short currentByte;
    private long offset = 0;

    public BitStream(final ByteBuffer source) {
        this.source = source;
    }

    public long readBits(long bits) {
        long result = 0;
        for (long i = 0; i < bits; ++i, ++offset) {
            if ((offset % 8) == 0) {
                currentByte = Unsigned.toUnsigned(source.get());
            }
            if((currentByte & (1 << (offset % 8))) != 0) {
                result |= (1L << i);
            }
        }
        return result;
    }

    public boolean readBoolean() {
        return readBits(1) != 0;
    }

    public short readChar(long min, long max, int bits) {
        if (bits != 0) {
            return (short) (readBits(bits) + min);
        } else {
            return (short) readBits(8);
        }
    }

    public byte readSignedChar(long min, long max, int bits) {
        if (bits != 0) {
            return (byte) (readBits(bits) + min);
        } else {
            return (byte) readBits(8);
        }
    }

    public short readUnsignedChar(long min, long max, int bits) {
        if (bits != 0) {
            return (short) (readBits(bits) + min);
        } else {
            return (short) readBits(8);
        }
    }

    public short readShort(long min, long max, int bits) {
        if (bits != 0) {
            return (short) (readBits(bits) + min);
        } else {
            return (short) readBits(16);
        }
    }

    public int readUnsignedShort(long min, long max, int bits) {
        if (bits != 0) {
            return (int) (readBits(bits) + min);
        } else {
            return (int) readBits(16);
        }
    }

    public int readInt(long min, long max, int bits) {
        if (bits != 0) {
            return (int) (readBits(bits) + min);
        } else {
            return (int) readBits(32);
        }
    }

    public long readUnsignedInt(long min, long max, int bits) {
        if (bits != 0) {
            return readBits(bits) + min;
        } else {
            return readBits(32);
        }
    }

    public float readFloat(double min, double max, int bits) {
        if (bits != 0) {
            final long integerValue = readBits(bits);
            return (float) (integerValue / (double) ((((long) 1) << bits) - 1) * (max - min) + min);
        } else {
            return Float.intBitsToFloat((int) readBits(32));
        }
    }

    public double readDouble(double min, double max, int bits) {
        if (bits != 0) {
            final long integerValue = readBits(bits);
            return (integerValue / (double) ((((long) 1) << bits) - 1) * (max - min) + min);
        } else {
            return Double.longBitsToDouble(readBits(64));
        }
    }

    public Timestamp readTimestamp(long base, int bits, int shift, int sign, boolean noclip) {
        if (sign != 0) {
            long integerValue = readBits(bits);
            if (integerValue == (((long) 1) << bits) - 1 && noclip) {
                return new Timestamp((sign < 0) ? 0 : Long.MAX_VALUE);
            } else {
                integerValue <<= shift;
                return new Timestamp(base + sign * integerValue);
            }
        } else {
            return new Timestamp(readUnsignedInt(0, 0, bits));
        }
    }

    public Angle readAngle(int bits) {
        return new Angle(readFloat(-Math.PI, Math.PI, bits));
    }
}
