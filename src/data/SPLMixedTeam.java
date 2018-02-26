package data;

/**
 * This class sets attributes given by the spl rules, adapted for the mixed team tournament.
 *
 * @author Thomas Röfer
 */
public class SPLMixedTeam extends SPL
{
    SPLMixedTeam()
    {
        /** The league´s name this rules are for. */
        leagueName = "SPL Mixed Team";
        /** The league´s directory name with it´s teams and icons. */
        leagueDirectory = "spl_mixedteam";
        /** How many robots are in a team. */
        teamSize = 6;
        /** How many robots of each team may play at one time. */
        robotsPlaying = teamSize;
        /** Number of penalty-shots for each team. */
        numberOfPenaltyShots = 4;
        /** The type of the competition (COMPETITION_TYPE_NORMAL, COMPETITION_TYPE_MIXEDTEAM, COMPETITION_TYPE_GENERAL_PENALTY_KICK) */
        competitionType = GameControlData.COMPETITION_TYPE_MIXEDTEAM;
    }
}
