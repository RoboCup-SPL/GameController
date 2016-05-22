package teamcomm;

import common.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Singleton class containing arbitrary config data for the TCM.
 *
 * @author Felix Thielke
 */
public class Config {

    private static final String CONFIG_FILE = "config/TCM.cfg";

    private static Config instance = null;

    private final HashMap<String, Serializable> map;

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private Config() {
        HashMap<String, Serializable> m = new HashMap<>();
        try {
            final Object o = new ObjectInputStream(new BufferedInputStream(new FileInputStream(CONFIG_FILE))).readObject();
            if (m.getClass().isInstance(o)) {
                m = (HashMap<String, Serializable>) o;
            }
        } catch (final IOException | ClassNotFoundException ex) {
        }
        map = m;
    }

    public Object get(final String key) {
        return map.get(key);
    }

    public void set(final String key, final Serializable config) {
        map.put(key, config);
    }

    public void flush() {
        if (!map.isEmpty()) {
            try (final ObjectOutputStream stream = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(CONFIG_FILE)))) {
                stream.writeObject(map);
                stream.flush();
            } catch (IOException ex) {
                Log.error("Could not write TCM config file " + new File(CONFIG_FILE).getAbsolutePath());
            }
        }
    }
}
