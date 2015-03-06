package teamcomm.gui.drawings;

/**
 *
 * @author Felix Thielke
 */
public abstract class Drawing {

    private boolean active = true;

    public void setActive(final boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
