package bhuman.message.messages;

import bhuman.message.data.Role;
import bhuman.message.Message;
import bhuman.message.data.ArrayReader;
import java.nio.ByteBuffer;
import java.util.List;

/**
 *
 * @author Felix Thielke
 */
public class TeammateRoles extends Message<TeammateRoles> {

    public List<Role> roles;

    @Override
    public TeammateRoles read(final ByteBuffer stream) {
        roles = new ArrayReader<Role>(Role.class).read(stream);
        return this;
    }

}
