package data.teams;

import java.awt.image.BufferedImage;

/**
 * Created by rkessler on 2017-02-11.
 */
public class TeamLoadInfo {

    /** The identifier of the team. */
    public int identifier;
    /** The name of the team. */
    public String name;
    /** The icon of the team. */
    public BufferedImage icon;
    /** The first and secondary jersey colors of the team. */
    public String[] colors;

    /**
     * Create a new team information.
     * @param name The name of the team.
     * @param colors The names of the jersey colors used by the team.
     *         Can be null if no colors were specified.
     */
    public TeamLoadInfo(int identifier, String name, String[] colors)
    {
        this.identifier = identifier;
        this.name = name;
        this.colors = colors;
    }

    public TeamLoadInfo(int identifier, String name)
    {
        this.identifier = identifier;
        this.name = name;
        this.colors = new String[]{};
    }

    public TeamLoadInfo clone(){
        return new TeamLoadInfo(this.identifier, this.name, this.colors);
    }

    public String toString() {
       return String.format("%1s (%2s)", this.name, this.identifier);
    }
}
