package teamcomm;

import common.Log;
import data.SPLStandardMessage;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import teamcomm.data.messages.AdvancedMessage;
import teamcomm.gui.drawings.Drawing;
import teamcomm.gui.drawings.PerPlayer;
import teamcomm.gui.drawings.Static;

/**
 *
 * @author Felix Thielke
 */
public class PluginLoader {

    private static final String PLUGIN_PATH = "plugins/";
    private static final String COMMON_DRAWINGS_PLUGIN = "common.jar";

    public static final int TEAMNUMBER_COMMON = -1;

    private static final PluginLoader instance = new PluginLoader();

    private final File pluginDir = new File(PLUGIN_PATH);
    private final Map<Integer, Class<? extends AdvancedMessage>> messageClasses = new HashMap<Integer, Class<? extends AdvancedMessage>>();
    private final Map<Integer, Collection<Drawing>> drawings = new HashMap<Integer, Collection<Drawing>>();

    private PluginLoader() {
        scanJar(new File(pluginDir, COMMON_DRAWINGS_PLUGIN), TEAMNUMBER_COMMON);
    }

    public static PluginLoader getInstance() {
        return instance;
    }

    public Class<? extends SPLStandardMessage> getMessageClass(final int teamNumber) {
        final Class<? extends AdvancedMessage> c = messageClasses.get(teamNumber);

        return c != null ? c : SPLStandardMessage.class;
    }

    public Collection<Drawing> getCommonDrawings() {
        return getDrawings(TEAMNUMBER_COMMON);
    }

    public Collection<Drawing> getDrawings(final int teamNumber) {
        final Collection<Drawing> ds = drawings.get(teamNumber);

        return ds != null ? ds : new ArrayList<Drawing>(0);
    }

    public void update(final Integer... teamNumbers) {
        update(new HashSet<Integer>(Arrays.asList(teamNumbers)));
    }

    public void update(final Set<Integer> teamNumbers) {
        // Find dirs that correspond to team numbers
        final File[] pluginDirs = pluginDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                try {
                    return teamNumbers.contains(Integer.parseInt(name));
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        });

        // Find jar files
        for (final File pDir : pluginDirs) {
            if (!pDir.isDirectory()) {
                continue;
            }

            final int teamNumber = Integer.parseInt(pDir.getName());
            final LinkedList<File> dirs = new LinkedList<File>();
            dirs.add(pDir);
            final List<File> jars = new LinkedList<File>();

            // Scan plugin directory
            while (!dirs.isEmpty()) {
                final File dir = dirs.pollFirst();
                for (final File file : dir.listFiles()) {
                    if (file.isDirectory()) {
                        dirs.addLast(file);
                    } else if (file.isFile() && file.getName().endsWith(".jar")) {
                        jars.add(file);
                    }
                }
            }

            // Load jars
            for (final File file : jars) {
                scanJar(file, teamNumber);
            }
        }
    }

    private final void scanJar(final File file, final int teamNumber) {
        try {
            final JarFile jar = new JarFile(file);
            final Set<String> classNames = new HashSet<String>();
            final Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    classNames.add(entry.getName().substring(0, entry.getName().length() - 6).replaceAll("/", "\\."));
                }
            }

            // Load classes from jar
            final URLClassLoader loader = new URLClassLoader(new URL[]{file.toURI().toURL()});
            classLoop:
            for (final String className : classNames) {
                final Class<?> cls = loader.loadClass(className);

                if (AdvancedMessage.class.isAssignableFrom(cls)) {
                    // Class is a message class: set it as default if no
                    // other message class exists for the team
                    if (!messageClasses.containsKey(teamNumber)) {
                        messageClasses.put(teamNumber, (Class<AdvancedMessage>) cls);
                    }
                } else if (PerPlayer.class.isAssignableFrom(cls) || Static.class.isAssignableFrom(cls)) {
                    // Class is a drawing: add it to the team drawings
                    // if it does not yet exist
                    Collection<Drawing> drawingsForTeam = drawings.get(teamNumber);
                    if (drawingsForTeam == null) {
                        drawingsForTeam = new LinkedList<Drawing>();
                        drawings.put(teamNumber, drawingsForTeam);
                    }
                    for (final Drawing d : drawingsForTeam) {
                        if (cls.isInstance(d)) {
                            continue classLoop;
                        }
                    }
                    final Drawing d = (Drawing) cls.newInstance();
                    d.setTeamNumber(teamNumber);
                    drawingsForTeam.add(d);
                }
            }
        } catch (Exception ex) {
            Log.error(ex.getClass().getSimpleName() + ": Could not open plugin " + file.getPath() + ": " + ex.getMessage());
        }
    }
}
