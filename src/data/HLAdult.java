package data;

/**
 *
 * @author Michel-Zen
 * 
 * This class sets attributes given by the humanoid-league rules.
 */
public class HLAdult extends HL
{
    HLAdult()
    {
        /** The league´s name this rules are for. */
        leagueName = "HL Adult";
        /** If the game starts with penalty-shoots. */
        startWithPenalty = true;
       /** Time in seconds one penalty shoot is long. */
        penaltyShootTime = (int)(2.5*60);
    }
}