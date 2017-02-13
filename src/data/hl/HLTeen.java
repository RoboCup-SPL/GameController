package data.hl;

/**
 * This class sets attributes given by the humanoid-league rules.
 *
 * @author Michel-Zen
 */
public class HLTeen extends HL
{
    public HLTeen()
    {
        /** The league´s name this rules are for. */
        leagueName = "HL Teen";
        /** The league´s directory name with it´s teams and icons. */
        leagueDirectory = "hl_teen";
        /** How many robots are in a team. */
        teamSize = 4;
        /** How many robots of each team may play at one time. */
        robotsPlaying = 2;
    }
}