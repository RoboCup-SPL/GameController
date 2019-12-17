package data;

/**
 * This class sets attributes given by the spl rules, adapted for the general penalty kick challenge.
 *
 * @author Arne Hasselbring
 */
public class SPLGeneralPenaltyKick extends SPLPenaltyShootout
{
    SPLGeneralPenaltyKick()
    {
        /** The league´s name this rules are for. */
        leagueName = "SPL General Penalty Kick Challenge";
        /** The type of the competition (COMPETITION_TYPE_NORMAL, COMPETITION_TYPE_GENERAL_PENALTY_KICK) */
        competitionType = GameControlData.COMPETITION_TYPE_GENERAL_PENALTY_KICK;
    }
}
