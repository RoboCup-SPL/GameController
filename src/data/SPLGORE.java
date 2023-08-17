package data;

/**
 * This class sets attributes given by the spl rules, adapted for the German Open Replacement Event 2021.
 *
 * @author Arne Hasselbring
 */
public class SPLGORE extends SPL
{
    SPLGORE()
    {
        /* The league's name these rules are for. */
        leagueName = "SPL GORE";
        /* The league's directory name with its teams and icons. */
        leagueDirectory = "spl";
        /* How many robots are in a team. */
        teamSize = 5; // no substitute
        /* Number of hardware penalties per half before the robot is ejected. */
        allowedHardwarePenaltiesPerHalf = 2;
        /* Number of hardware penalties per game before the robot is ejected. */
        allowedHardwarePenaltiesPerGame = 3;
    }
}
