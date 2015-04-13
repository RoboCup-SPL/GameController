package teamcomm.gui.drawings;

/**
 *
 * @author Felix Thielke
 */
public abstract class Drawing /*implements Comparable<Drawing>*/ {

    private boolean active = true;

    public void setActive(final boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    /*public abstract boolean hasAlpha();

    public abstract int getPriority();

    @Override
    public int compareTo(final Drawing o) {
        return !hasAlpha() && o.hasAlpha() ? 1 : hasAlpha() && !o.hasAlpha() ? -1 : getPriority() - o.getPriority();
    }*/

}
