package teamcomm.gui.drawings;

/**
 * Abstract base class for drawings of the 3D field view.
 *
 * @author Felix Thielke
 */
public abstract class Drawing {

    private boolean active = true;

    private int teamNumber = -1;

    /**
     * Sets whether this drawing is drawn.
     *
     * @param active boolean
     */
    public void setActive(final boolean active) {
        this.active = active;
    }

    /**
     * Returns whether this drawing is drawn.
     *
     * @return boolean
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Returns the number of the team for which thhis drawing is drawn.
     *
     * @return team number
     */
    public int getTeamNumber() {
        return teamNumber;
    }

    /**
     * Sets the number of the team for which thhis drawing is drawn.
     *
     * @param teamNumber team number
     */
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
