package teamcomm.gui.drawings;

/**
 *
 * @author Felix Thielke
 */
public abstract class Drawing {

    private boolean active = true;

    private int teamNumber = -1;

    public void setActive(final boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    public void setTeamNumber(final int teamNumber) {
        this.teamNumber = teamNumber;
    }

    /**
     * Indicates whether this drawing contains transparent objects.
     *
     * @return boolean
     */
    public abstract boolean hasAlpha();

    /**
     * The higher the priority, the earlier the drawing is drawn. This only
     * matters for transparent drawings so they overlap correctly. Drawings in
     * the foreground should have lower priority than drawings in the
     * background.
     *
     * @return priority
     */
    public abstract int getPriority();

}
