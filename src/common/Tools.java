package common;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

/**
 *
 * @author: Thomas Röfer
 *
 * This class provides general tool methods.
 */
public class Tools
{
    /**
     * Clone any object that is Serializable.
     * @param object The object that is cloned.
     * @return A deep copy of the object.
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(T object)
    {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            new ObjectOutputStream(out).writeObject(object);
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            return (T) new ObjectInputStream(in).readObject();
        } catch (ClassNotFoundException e) {
            System.out.println(e.getClass().getName() + ": " + e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return null; // Should never be reached
    }
}