package data;

import java.awt.Color;

/**
 * This class sets attributes given by the spl rules.
 *
 * @author Michel-Zen
 */
public class SPL extends Rules
{
    SPL()
    {
        /* The league's name this rules are for. */
        leagueName = "SPL";
        /* The league's directory name with its teams and icons. */
        leagueDirectory = "spl";
        /* How many robots are in a team. */
        teamSize = 6; // 5 players + 1 sub
        /* The Java Colors the left and the right team starts with. */
        teamColor = new Color[] {Color.BLUE, Color.RED, new Color(224, 200, 0), Color.BLACK, Color.WHITE, new Color(0, 192, 0), new Color(255, 165, 0), new Color(128, 0, 128), new Color(165, 42, 42), new Color(128, 128, 128)};
        /* Time in seconds one half is long. */
        halfTime = 10*60;
    }
}
