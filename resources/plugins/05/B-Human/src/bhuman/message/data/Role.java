package bhuman.message.data;

import java.nio.ByteBuffer;

/**
 * Class for player roles.
 *
 * @author Felix Thielke
 */
public class Role implements SimpleStreamReader<Role> {

    /**
     * Enum of all roles.
     */
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

    /**
     * Role.
     */
    public RoleType role;

    @Override
    public int getStreamedSize() {
        return new EnumReader<>(RoleType.class).getStreamedSize();
    }

    @Override
    public Role read(ByteBuffer stream) {
        role = new EnumReader<>(RoleType.class).read(stream);
        return this;
    }

    @Override
    public String toString() {
        return role.toString();
    }

}
