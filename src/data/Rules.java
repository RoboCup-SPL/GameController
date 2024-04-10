package data;

import java.awt.Color;


/**
 * This class holds attributes defining rules.
 *
 * @author Michel Bartsch
 */
public abstract class Rules
{
    /** Note all league's rules here to have them available. */
    public static final Rules[] LEAGUES = {
        new SPL()
    };

    /**
     * Returns the Rules object for the given class.
     */
    public static Rules getLeagueRules(final Class<? extends Rules> c) {
        for(final Rules r : LEAGUES) {
            if(c.isInstance(r) && r.getClass().isAssignableFrom(c)) {
                return r;
            }
        }
        return null;
    }

    /** The rules of the league playing. */
    public static Rules league = LEAGUES[0];
    /** The league's name this rules are for. */
    public String leagueName;
    /** The league's directory name with its teams and icons. */
    public String leagueDirectory;
    /** How many robots are in a team. */
    public int teamSize;
    /** The Java Colors the left and the right team starts with. */
    public Color[] teamColor;
    /** Time in seconds one half is long. */
    public int halfTime;
}
