package data;

/**
 * This class sets attributes given by the spl rules, adapted for the dynamic ball handling challenge.
 *
 * @author Arne Hasselbring
 */
public class SPLDynamicBallHandling extends SPL
{
    SPLDynamicBallHandling()
    {
        /* The league's name these rules are for. */
        leagueName = "SPL Dynamic Ball Handling";
        /* The league's directory name with its teams and icons. */
        leagueDirectory = "spl";
        /* How many robots are in a team. */
        teamSize = 3;
        /* How many robots of each team may play at one time. */
        robotsPlaying = 3;
        /* Time in seconds one half is long. */
        halfTime = 4*60;
        /* Time in seconds the ball is blocked after kickoff. */
        kickoffTime = -1;
        /* The type of the competition (COMPETITION_TYPE_NORMAL, COMPETITION_TYPE_CHALLENGE_SHIELD, etc) */
        competitionType = GameControlData.COMPETITION_TYPE_DYNAMIC_BALL_HANDLING;
    }
}
