package data;

/**
 * This class sets attributes given by the spl rules, adapted for the passing challenge.
 *
 * @author Arne Hasselbring
 */
public class SPLPassingChallenge extends SPL
{
    SPLPassingChallenge()
    {
        /** The league´s name this rules are for. */
        leagueName = "SPL Passing Challenge";
        /** The league´s directory name with it´s teams and icons. */
        leagueDirectory = "spl_passing_challenge";
        /** How many robots are in a team. */
        teamSize = 5; // 5 players
        /** Time in seconds one half is long. */
        halfTime = 5*60;
        /** Time in seconds the ball is blocked after kickoff. */
        kickoffTime = -1;
        /** The type of the competition (COMPETITION_TYPE_NORMAL, COMPETITION_TYPE_1VS1, COMPETITION_TYPE_PASSING_CHALLENGE) */
        competitionType = GameControlData.COMPETITION_TYPE_PASSING_CHALLENGE;
    }
}

