package data;

/**
 * This class sets attributes given by the spl rules, adapted for the drop-in competition.
 *
 * @author Michel-Zen
 */
public class SPLDropIn extends SPL
{
    SPLDropIn()
    {
        /** The league´s name this rules are for. */
        leagueName = "SPL Drop-in";
        /** The league´s directory name with it´s teams and icons. */
        leagueDirectory = "spl_dropin";
        /** How many robots are in a team. */
        teamSize = robotsPlaying;
        /** Time in seconds to increment penalties. */
        penaltyIncreaseTime = 0;
        /** The number of seconds switching to Playing is delayed. */
        delayedSwitchToPlaying = 0;
        /** Defines if coach is available */
        isCoachAvailable = false;
        /** If true, the drop-in player competition is active */
        dropInPlayerMode = true;
        /** On how many pushings is a robot ejected. */
        pushesToEjection = new int[] {};
    }
}