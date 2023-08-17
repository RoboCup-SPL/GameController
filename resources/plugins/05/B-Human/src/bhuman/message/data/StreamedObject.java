package bhuman.message.data;

import common.Log;

import java.lang.reflect.*;
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
        stream.position(startPosition);

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
                    return reader.value().getConstructor().newInstance();
                } else if (primitive != null) {
                    try {
                        final Field readerField = NativeReaders.class.getField(primitive.value().toLowerCase() + "Reader");
                        if (Modifier.isStatic(readerField.getModifiers()) && Modifier.isPublic(readerField.getModifiers())) {
                            return StreamReader.class.cast(readerField.get(null));
                        }
                    } catch (NoSuchFieldException | SecurityException ex) {
                    }
                } else {
                    final Class<?> type = field.getType();
                    if (!type.isArray()) {
                        final StreamReader<?> nativeReader = NativeReaders.getByType(type);
                        if (nativeReader != null) {
                            return nativeReader;
                        } else if (StreamReader.class.isAssignableFrom(type)) {
                            return (StreamReader<?>) type.getConstructor().newInstance();
                        } else if (Enum.class.isAssignableFrom(type)) {
                            return new EnumReader(type);
                        } else if (EnumMap.class.isAssignableFrom(type)) {
                            try {
                                final Type[] typeArguments = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
                                final Class<?> componentType = Class.forName(typeArguments[1].getTypeName());
                                StreamReader<?> componentReader = NativeReaders.getByType(componentType);
                                if (componentReader == null) {
                                    if (StreamReader.class.isAssignableFrom(componentType)) {
                                        return new EnumMapReader(Class.forName(typeArguments[0].getTypeName()), componentType);
                                    } else if (Enum.class.isAssignableFrom(componentType)) {
                                        componentReader = new EnumReader(componentType);
                                    } else {
                                        Log.error("field " + field.getName() + " in class " + getClass().getName() + " could not be read automatically");
                                    }
                                }
                                return new EnumMapReader(Class.forName(typeArguments[0].getTypeName()), componentReader);
                            } catch (ClassNotFoundException ex) {
                                Log.error("field " + field.getName() + " in class " + getClass().getName() + " could not be read automatically because the type was not found");
                            }
                        } else if (List.class.isAssignableFrom(type)) {
                            try {
                                final Class<?> componentType = Class.forName(((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0].getTypeName());
                                final int listCountSize = field.getAnnotation(ListCountSize.class) == null ? 4 : field.getAnnotation(ListCountSize.class).value();
                                StreamReader<?> componentReader = NativeReaders.getByType(componentType);
                                if (componentReader == null) {
                                    if (StreamReader.class.isAssignableFrom(componentType)) {
                                        return new ListReader(componentType, listCountSize);
                                    } else if (Enum.class.isAssignableFrom(componentType)) {
                                        componentReader = new EnumReader(componentType);
                                    } else {
                                        Log.error("field " + field.getName() + " in class " + getClass().getName() + " could not be read automatically");
                                    }
                                }
                                return new ListReader<>(componentReader, listCountSize);
                            } catch (ClassNotFoundException ex) {
                                Log.error("field " + field.getName() + " in class " + getClass().getName() + " could not be read automatically because the type was not found");
                            }
                        } else {
                            Log.error("field " + field.getName() + " in class " + getClass().getName() + " could not be read automatically");
                        }
                    } else {
                        final Class<?> componentType = type.getComponentType();
                        final StreamReader<?> nativeArrayReader = NativeReaders.getArrayReaderByComponentType(componentType, Array.getLength(field.get(this)));
                        if (nativeArrayReader != null) {
                            return nativeArrayReader;
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
        } catch (IllegalAccessException
                 | InstantiationException
                 | InvocationTargetException
                 | NoSuchMethodException ex) {
            Log.error("Could not get reader for field " + field.getName() + " of class " + getClass().getName());
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
