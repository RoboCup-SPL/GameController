package data;

/**
 *
 * @author Michel-Zen
 * 
 * This class sets attributes given by the humanoid-league rules.
 */
public class AdultSize extends Humanoid
{
    AdultSize()
    {
        super();
        
        /** The league´s name this rules are for. */
        leagueName = "HL (AdultSize)";
        /** If the game starts with penalty-shoots. */
        startWithPenalty = true;
       /** Time in seconds one penalty shoot is long. */
        penaltyShootTime = (int)(2.5*60);
    }
}