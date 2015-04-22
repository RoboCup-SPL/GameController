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
        final ArrayReader<Role> reader = new ArrayReader<Role>(Role.class);
        if(stream.remaining() != reader.getStreamedSize(stream)) {
            return null;
        }
        roles = reader.read(stream);
        return this;
    }

}
