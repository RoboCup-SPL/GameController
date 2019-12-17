package data;

/**
 * This class sets attributes given by the spl rules, but starting with a penalty shoot-out.
 *
 * @author Thomas Röfer
 */
public class SPLPenaltyShootout extends SPL
{
    SPLPenaltyShootout()
    {
        /** The league´s name this rules are for. */
        leagueName = "SPL Penalty Shootout";
        /** If the game starts with penalty-shots. */
        startWithPenalty = true;
    }
}
