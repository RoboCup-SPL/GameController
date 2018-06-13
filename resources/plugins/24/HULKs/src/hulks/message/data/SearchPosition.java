package hulks.message.data;

/**
 * @author Georg Felbinger
 */
public class SearchPosition {
    private int player;
    private Eigen.Vector2f position;

    public SearchPosition(int player, Eigen.Vector2f position) {
        this.player = player;
        this.position = position;
    }

    public int getPlayer() {
        return player;
    }

    public Eigen.Vector2f getPosition() {
        return position;
    }
}
