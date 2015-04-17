package bhuman.message;

import bhuman.message.MessageID;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.Map;
import util.Unsigned;

/**
 *
 * @author Felix Thielke
 */
public abstract class Message {

    protected Message(final ByteBuffer data) {
    }

    private static final Map<MessageID, Class<Message>> classes = new EnumMap<MessageID, Class<Message>>(MessageID.class);

    public static Message factory(final MessageID identifier, final ByteBuffer data) {
        System.out.println(identifier);

        if (classes.isEmpty()) {
            for (final MessageID id : MessageID.values()) {
                try {
                    final Class<Message> cls = (Class<Message>) Class.forName("bhuman.message.messages." + id.toString().substring(2));

                    classes.put(id, cls);
                } catch (ClassNotFoundException ex) {
                }
            }
        }

        final Class<Message> cls = classes.get(identifier);
        if (cls != null) {
            try {
                return cls.getConstructor(ByteBuffer.class).newInstance(data);
            } catch (NoSuchMethodException ex) {
            } catch (SecurityException ex) {
            } catch (InstantiationException ex) {
            } catch (IllegalAccessException ex) {
            } catch (IllegalArgumentException ex) {
            } catch (InvocationTargetException ex) {
            }
        }

        return null;
    }

    protected static boolean readBool(final ByteBuffer data) {
        return data.get() != 0;
    }

    protected static char readChar(final ByteBuffer data) {
        return (char) data.get();
    }

    protected static byte readSChar(final ByteBuffer data) {
        return data.get();
    }

    protected static short readUChar(final ByteBuffer data) {
        return Unsigned.toUnsigned(data.get());
    }

    protected static short readShort(final ByteBuffer data) {
        return data.getShort();
    }

    protected static int readUShort(final ByteBuffer data) {
        return Unsigned.toUnsigned(data.getShort());
    }

    protected static int readInt(final ByteBuffer data) {
        return data.getInt();
    }

    protected static long readUInt(final ByteBuffer data) {
        return Unsigned.toUnsigned(data.getInt());
    }

    protected static float readFloat(final ByteBuffer data) {
        return data.getFloat();
    }

    protected static double readDouble(final ByteBuffer data) {
        return data.getDouble();
    }

    protected static String readString(final ByteBuffer data) {
        final int size = data.getInt();
        final byte[] bytes = new byte[size];
        data.get(bytes);
        try {
            return new String(bytes, "ISO-8859-1");
        } catch (UnsupportedEncodingException ex) {
            try {
                return new String(bytes, "US-ASCII");
            } catch (UnsupportedEncodingException e) {
                return new String(bytes);
            }
        }
    }

    protected static boolean[] readBoolArray(final ByteBuffer data) {
        final int size = data.getInt();
        final boolean[] arr = new boolean[size];
        for (int i = 0; i < size; i++) {
            arr[i] = readBool(data);
        }
        return arr;
    }

    protected static char[] readCharArray(final ByteBuffer data) {
        final int size = data.getInt();
        final char[] arr = new char[size];
        for (int i = 0; i < size; i++) {
            arr[i] = readChar(data);
        }
        return arr;
    }

    protected static byte[] readSCharArray(final ByteBuffer data) {
        final int size = data.getInt();
        final byte[] arr = new byte[size];
        for (int i = 0; i < size; i++) {
            arr[i] = readSChar(data);
        }
        return arr;
    }

    protected static short[] readUCharArray(final ByteBuffer data) {
        final int size = data.getInt();
        final short[] arr = new short[size];
        for (int i = 0; i < size; i++) {
            arr[i] = readUChar(data);
        }
        return arr;
    }

    protected static short[] readShortArray(final ByteBuffer data) {
        final int size = data.getInt();
        final short[] arr = new short[size];
        for (int i = 0; i < size; i++) {
            arr[i] = readShort(data);
        }
        return arr;
    }

    protected static int[] readUShortArray(final ByteBuffer data) {
        final int size = data.getInt();
        final int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = readUShort(data);
        }
        return arr;
    }

    protected static int[] readIntArray(final ByteBuffer data) {
        final int size = data.getInt();
        final int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = readInt(data);
        }
        return arr;
    }

    protected static long[] readUIntArray(final ByteBuffer data) {
        final int size = data.getInt();
        final long[] arr = new long[size];
        for (int i = 0; i < size; i++) {
            arr[i] = readUInt(data);
        }
        return arr;
    }

    protected static float[] readFloatArray(final ByteBuffer data) {
        final int size = data.getInt();
        final float[] arr = new float[size];
        for (int i = 0; i < size; i++) {
            arr[i] = readFloat(data);
        }
        return arr;
    }

    protected static double[] readDoubleArray(final ByteBuffer data) {
        final int size = data.getInt();
        final double[] arr = new double[size];
        for (int i = 0; i < size; i++) {
            arr[i] = readDouble(data);
        }
        return arr;
    }

    protected static String[] readStringArray(final ByteBuffer data) {
        final int size = data.getInt();
        final String[] arr = new String[size];
        for (int i = 0; i < size; i++) {
            arr[i] = readString(data);
        }
        return arr;
    }

    protected static ByteBuffer[] readArray(final ByteBuffer data, final int elementSize) {
        final int size = data.getInt();
        final ByteBuffer[] bufs = new ByteBuffer[size];
        for (int i = 0; i < size; i++) {
            final byte[] bytes = new byte[elementSize];
            data.get(bytes);
            bufs[i] = ByteBuffer.wrap(bytes);
        }
        return bufs;
    }

    protected static <E extends Enum<E>> E readEnum(final ByteBuffer data, Class<E> enumClass) {
        try {
            return ((E[]) enumClass.getMethod("values").invoke(null))[data.getInt()];
        } catch (NoSuchMethodException ex) {
        } catch (SecurityException ex) {
        } catch (IllegalAccessException ex) {
        } catch (IllegalArgumentException ex) {
        } catch (InvocationTargetException ex) {
        }

        return null;
    }
}
