package data;

/**
 * This class sets attributes given by the spl rules for 7 players.
 *
 * @author Arne Hasselbring
 */
public class SPL7v7 extends SPL
{
    SPL7v7()
    {
        /* The league's name these rules are for. */
        leagueName = "SPL 7v7";
        /* The league's directory name with its teams and icons. */
        leagueDirectory = "spl";
        /* How many robots are in a team. */
        teamSize = 7;
        /* How many robots of each team may play at one time. */
        robotsPlaying = 7;
    }
}
