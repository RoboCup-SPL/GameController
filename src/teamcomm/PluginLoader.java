package teamcomm;

import data.SPLStandardMessage;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import teamcomm.data.messages.AdvancedMessage;

/**
 *
 * @author Felix Thielke
 */
public class PluginLoader {

    private static final String PLUGIN_PATH = "plugins/";

    private static final Map<Integer, Class<? extends AdvancedMessage>> activeMessageClasses = new HashMap<Integer, Class<? extends AdvancedMessage>>();
    private static final Map<Integer, Collection<Class<? extends AdvancedMessage>>> messageClasses = new HashMap<Integer, Collection<Class<? extends AdvancedMessage>>>();

    public static Class<? extends SPLStandardMessage> getMessageClass(final int teamNumber) {
        Class<? extends AdvancedMessage> c = activeMessageClasses.get(teamNumber);

        return c != null ? c : SPLStandardMessage.class;
    }

}
