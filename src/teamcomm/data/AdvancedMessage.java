package teamcomm.data;

import data.SPLTeamMessage;

/**
 * Abstract base class for plugin-based extensions to the SPLTeamMessage.
 *
 * @author Felix Thielke
 */
public abstract class AdvancedMessage extends SPLTeamMessage {

    private static final long serialVersionUID = 5893551586737053344L;

    public static final String DISPLAY_NEXT_COLUMN = "<NEXT_COLUMN>";

    /**
     * Returns an array of strings containing info from the message which should
     * be displayed in the detail window of the sending robots. Each string is
     * displayed in a single line.
     *
     * @return array
     */
    public abstract String[] display();

    /**
     * Initializes the message. This method is called once after receiving the
     * message and can be used to parse the contents of the custom data array
     * into fields.
     */
    public abstract void init();
}
