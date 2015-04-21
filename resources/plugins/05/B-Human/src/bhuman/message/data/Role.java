package bhuman.message.data;

import java.nio.ByteBuffer;

/**
 *
 * @author Felix Thielke
 */
public class Role implements SimpleStreamReader<Role> {

    public static enum RoleType {

        undefined,
        keeper,
        attackingKeeper,
        striker,
        defender,
        supporter,
        penaltyStriker,
        penaltyKeeper,
        none
    }

    public RoleType role;

    @Override
    public int getStreamedSize() {
        return new EnumReader<RoleType>(RoleType.class).getStreamedSize();
    }

    @Override
    public Role read(ByteBuffer stream) {
        role = new EnumReader<RoleType>(RoleType.class).read(stream);
        return this;
    }

    @Override
    public String toString() {
        return role.toString();
    }

}
