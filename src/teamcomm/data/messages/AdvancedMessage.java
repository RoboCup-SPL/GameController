package teamcomm.data.messages;

import data.SPLStandardMessage;

/**
 *
 * @author Felix Thielke
 */
public abstract class AdvancedMessage extends SPLStandardMessage {
    private static final long serialVersionUID = 5893551586737053344L;

    public abstract String[] display();
    
    public abstract void init();
}
