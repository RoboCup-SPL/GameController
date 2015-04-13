package teamcomm;

import data.SPLStandardMessage;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import teamcomm.data.messages.AdvancedMessage;
import teamcomm.gui.drawings.Drawing;

/**
 *
 * @author Felix Thielke
 */
public class PluginLoader {

    private static final String PLUGIN_PATH = "plugins/";

    private static final PluginLoader instance = new PluginLoader();

    private final File pluginDir = new File(PLUGIN_PATH);
    private final Map<Integer, Class<? extends AdvancedMessage>> messageClasses = new HashMap<Integer, Class<? extends AdvancedMessage>>();
    private final Map<Integer, Collection<Drawing>> drawings = new HashMap<Integer, Collection<Drawing>>();

    private PluginLoader() {
    }

    public static PluginLoader getInstance() {
        return instance;
    }

    public Class<? extends SPLStandardMessage> getMessageClass(final int teamNumber) {
        final Class<? extends AdvancedMessage> c = messageClasses.get(teamNumber);

        return c != null ? c : SPLStandardMessage.class;
    }

    public Collection<Drawing> getDrawings(final int teamNumber) {
        final Collection<Drawing> ds = drawings.get(teamNumber);

        return ds != null ? ds : new ArrayList<Drawing>(0);
    }

    public void update(final Integer... teamNumbers) {
        update(new HashSet<Integer>(Arrays.asList(teamNumbers)));
    }

    public void update(final Set<Integer> teamNumbers) {
        
        pluginDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                try {
                    return teamNumbers.contains(Integer.parseInt(name));
                } catch(NumberFormatException e) {
                    return false;
                }
            }
        });
    }

}
