package bhuman.message.data;

import common.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Felix Thielke
 */
public class StreamedObject<T extends StreamedObject> implements ProbablySimpleStreamReader<T> {

    private static final Map<Class<? extends StreamedObject>, Integer> streamedSizes = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public T read(final ByteBuffer stream) {
        for (final Field field : getClass().getFields()) {
            readField(stream, field);
        }

        return (T) this;
    }

    @Override
    public boolean isSimpleStreamReader() {
        if (!streamedSizes.containsKey(getClass())) {
            getStreamedSize(ByteBuffer.allocateDirect(0));
        }
        return streamedSizes.get(getClass()) != -1;
    }

    @Override
    public int getStreamedSize(final ByteBuffer stream) {
        final Integer cachedSize = streamedSizes.get(getClass());
        if (cachedSize != null && cachedSize != -1) {
            return cachedSize;
        }

        boolean simple = true;
        int size = 0;
        final int startPosition = stream.position();
        for (final Field field : getClass().getFields()) {
            final StreamReader<?> reader = getFieldReader(field);
            if (reader != null) {
                if (SimpleStreamReader.class.isInstance(reader)) {
                    size += SimpleStreamReader.class.cast(reader).getStreamedSize();
                } else if (ComplexStreamReader.class.isInstance(reader)) {
                    if (ProbablySimpleStreamReader.class.isInstance(reader) && ProbablySimpleStreamReader.class.cast(reader).isSimpleStreamReader()) {
                        size += ProbablySimpleStreamReader.class.cast(reader).getStreamedSize(stream);
                    } else {
                        simple = false;
                        if (startPosition + size > stream.limit()) {
                            break;
                        }
                        stream.position(startPosition + size);
                        size += ComplexStreamReader.class.cast(reader).getStreamedSize(stream);
                    }
                } else {
                    Log.error("Wrong type of reader: " + reader.getClass().getName());
                }
            }
        }

        streamedSizes.put(getClass(), simple ? size : -1);

        return size;
    }

    @SuppressWarnings("unchecked")
    protected StreamReader<?> getFieldReader(final Field field) {
        try {
            if (!Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) {
                final Reader reader = field.getAnnotation(Reader.class);
                final Primitive primitive = field.getAnnotation(Primitive.class);
                if (reader != null) {
                    return reader.value().newInstance();
                } else if (primitive != null) {
                    switch (primitive.value().toLowerCase()) {
                        case "bool":
                            return NativeReaders.boolReader;
                        case "char":
                            return NativeReaders.charReader;
                        case "schar":
                            return NativeReaders.scharReader;
                        case "uchar":
                            return NativeReaders.ucharReader;
                        case "short":
                            return NativeReaders.shortReader;
                        case "ushort":
                            return NativeReaders.ushortReader;
                        case "int":
                            return NativeReaders.intReader;
                        case "uint":
                            return NativeReaders.uintReader;
                        case "float":
                            return NativeReaders.floatReader;
                        case "double":
                            return NativeReaders.doubleReader;
                    }
                } else {
                    final Class<?> type = field.getType();
                    if (!type.isArray()) {
                        if (boolean.class.isAssignableFrom(type)) {
                            return NativeReaders.boolReader;
                        } else if (byte.class.isAssignableFrom(type)) {
                            return NativeReaders.scharReader;
                        } else if (char.class.isAssignableFrom(type)) {
                            return NativeReaders.charReader;
                        } else if (short.class.isAssignableFrom(type)) {
                            return NativeReaders.shortReader;
                        } else if (int.class.isAssignableFrom(type)) {
                            return NativeReaders.intReader;
                        } else if (long.class.isAssignableFrom(type)) {
                            return NativeReaders.uintReader;
                        } else if (float.class.isAssignableFrom(type)) {
                            return NativeReaders.floatReader;
                        } else if (double.class.isAssignableFrom(type)) {
                            return NativeReaders.doubleReader;
                        } else if (String.class.isAssignableFrom(type)) {
                            return NativeReaders.stringReader;
                        } else if (StreamReader.class.isAssignableFrom(type)) {
                            return (StreamReader<?>) type.newInstance();
                        } else if (Enum.class.isAssignableFrom(type)) {
                            return new EnumReader(type);
                        } else if (EnumMap.class.isAssignableFrom(type)) {
                            try {
                                final Type[] typeArguments = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
                                return new EnumMapReader(Class.forName(typeArguments[0].getTypeName()), Class.forName(typeArguments[1].getTypeName()));
                            } catch (ClassNotFoundException ex) {
                                Log.error("field " + field.getName() + " in class " + getClass().getName() + " could not be read automatically because the type was not found");
                            }
                        } else if (List.class.isAssignableFrom(type)) {
                            try {
                                final Class<?> componentType = Class.forName(((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0].getTypeName());
                                final int listCountSize = field.getAnnotation(ListCountSize.class) == null ? 4 : field.getAnnotation(ListCountSize.class).value();
                                if (boolean.class.isAssignableFrom(type)) {
                                    return new ListReader(NativeReaders.boolReader, listCountSize);
                                } else if (byte.class.isAssignableFrom(type)) {
                                    return new ListReader(NativeReaders.scharReader, listCountSize);
                                } else if (char.class.isAssignableFrom(type)) {
                                    return new ListReader(NativeReaders.charReader, listCountSize);
                                } else if (short.class.isAssignableFrom(type)) {
                                    return new ListReader(NativeReaders.shortReader, listCountSize);
                                } else if (int.class.isAssignableFrom(type)) {
                                    return new ListReader(NativeReaders.intReader, listCountSize);
                                } else if (long.class.isAssignableFrom(type)) {
                                    return new ListReader(NativeReaders.uintReader, listCountSize);
                                } else if (float.class.isAssignableFrom(type)) {
                                    return new ListReader(NativeReaders.floatReader, listCountSize);
                                } else if (double.class.isAssignableFrom(type)) {
                                    return new ListReader(NativeReaders.doubleReader, listCountSize);
                                } else if (Enum.class.isAssignableFrom(type)) {
                                    return new ListReader(new EnumReader(type), listCountSize);
                                } else if (StreamReader.class.isAssignableFrom(componentType)) {
                                    return new ListReader(componentType, listCountSize);
                                } else {
                                    Log.error("field " + field.getName() + " in class " + getClass().getName() + " could not be read automatically");
                                }
                            } catch (ClassNotFoundException ex) {
                                Log.error("field " + field.getName() + " in class " + getClass().getName() + " could not be read automatically because the type was not found");
                            }
                        } else {
                            Log.error("field " + field.getName() + " in class " + getClass().getName() + " could not be read automatically");
                        }
                    } else {
                        final Class<?> componentType = type.getComponentType();
                        if (boolean.class.isAssignableFrom(componentType)) {
                            return new NativeReaders.BoolArrayReader(boolean[].class.cast(field.get(this)).length);
                        } else if (byte.class.isAssignableFrom(componentType)) {
                            return new NativeReaders.SCharArrayReader(byte[].class.cast(field.get(this)).length);
                        } else if (char.class.isAssignableFrom(componentType)) {
                            return new NativeReaders.CharArrayReader(char[].class.cast(field.get(this)).length);
                        } else if (short.class.isAssignableFrom(componentType)) {
                            return new NativeReaders.ShortArrayReader(short[].class.cast(field.get(this)).length);
                        } else if (int.class.isAssignableFrom(componentType)) {
                            return new NativeReaders.IntArrayReader(int[].class.cast(field.get(this)).length);
                        } else if (long.class.isAssignableFrom(componentType)) {
                            return new NativeReaders.UIntArrayReader(long[].class.cast(field.get(this)).length);
                        } else if (float.class.isAssignableFrom(componentType)) {
                            return new NativeReaders.FloatArrayReader(float[].class.cast(field.get(this)).length);
                        } else if (double.class.isAssignableFrom(componentType)) {
                            return new NativeReaders.DoubleArrayReader(double[].class.cast(field.get(this)).length);
                        } else if (Enum.class.isAssignableFrom(componentType)) {
                            return new ArrayReader<>(new EnumReader(componentType), Enum[].class.cast(field.get(this)));
                        } else if (StreamReader.class.isAssignableFrom(componentType)) {
                            return new ArrayReader(componentType, StreamReader[].class.cast(field.get(this)));
                        } else {
                            Log.error("field " + field.getName() + " in class " + getClass().getName() + " could not be read automatically");
                        }
                    }
                }
            }
        } catch (IllegalAccessException | InstantiationException ex) {
            Log.error("Could not set field " + field.getName() + " of class " + getClass().getName());
        }
        return null;
    }

    protected void readField(final ByteBuffer stream, final Field field) {
        final StreamReader<?> reader = getFieldReader(field);
        if (reader != null) {
            try {
                field.set(this, reader.read(stream));
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Log.error("Could not set field " + field.getName() + " of class " + getClass().getName());
            }
        }
    }

}
