package data;

/**
 * This class sets attributes given by the spl rules, adapted for the penalty shootout challenge.
 *
 * @author Thomas Röfer
 */
public class SPLPenaltyShootout extends SPL
{
    SPLPenaltyShootout()
    {
        /** The league´s name this rules are for. */
        leagueName = "SPL Penalty Shootout";
        /** The league´s directory name with it´s teams and icons. */
        leagueDirectory = "spl_penaltyshootout";
        /** If the game starts with penalty-shots. */
        startWithPenalty = true;
        /** The type of the competition (GAMETYPE_NORMAL, GAMETYPE_MIXEDTEAM, GAMETYPE_GENERAL_PENALTY_KICK) */
        competitionType = GameControlData.GAMETYPE_GENERAL_PENALTY_KICK;
    }
}