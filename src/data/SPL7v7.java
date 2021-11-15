package data;

/**
 * This class sets attributes given by the spl rules, adapted for the 7v7 competition.
 *
 * @author Arne Hasselbring
 */
public class SPL7v7 extends SPL
{
    SPL7v7()
    {
        /** The league's name this rules are for. */
        leagueName = "SPL 7v7";
        /** The league's directory name with its teams and icons. */
        leagueDirectory = "spl";
        /** How many robots are in a team. */
        teamSize = 7;
        /** How many robots of each team may play at one time. */
        robotsPlaying = 7;
        /** The type of the competition (COMPETITION_TYPE_NORMAL, COMPETITION_TYPE_7V7, COMPETITION_TYPE_DYNAMIC_BALL_HANDLING) */
        competitionType = GameControlData.COMPETITION_TYPE_7V7;
        /** Number of hardware penalties per half before the robot is ejected. */
        allowedHardwarePenaltiesPerHalf = 2;
        /** Number of hardware penalties per game before the robot is ejected. */
        allowedHardwarePenaltiesPerGame = 3;
    }
}
