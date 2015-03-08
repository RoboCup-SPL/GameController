package teamcomm.data.messages;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
/**
 *
 * @author Felix Thielke
 */
public @interface Team {
    public int[] value();
}
