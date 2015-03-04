package teamcomm.gui.drawings;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
/**
 *
 * @author Felix Thielke
 */
public @interface Models {

    String[] value();
}
