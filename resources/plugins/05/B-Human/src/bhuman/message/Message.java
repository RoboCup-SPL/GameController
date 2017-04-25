package bhuman.message;

import bhuman.message.data.StreamReader;

/**
 * Abstract base class for messages read from the message queue.
 *
 * @author Felix Thielke
 * @param <T> type of the implementing subclass
 */
public interface Message<T extends Message> extends StreamReader<T> {
}
