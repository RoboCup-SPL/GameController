package data;

/**
 * This class sets attributes given by the spl rules, adapted for the 1vs1 competition.
 *
 * @author Arne Hasselbring
 */
public class SPL1vs1 extends SPL
{
    SPL1vs1()
    {
        /** The league´s name this rules are for. */
        leagueName = "SPL 1vs1";
        /** How many robots are in a team. */
        teamSize = 5; // 5 players
        /** Time in seconds one half is long. */
        halfTime = 5*60;
        /** Time in seconds the ball is blocked after kickoff. */
        kickoffTime = -1;
        /** The type of the competition (COMPETITION_TYPE_NORMAL, COMPETITION_TYPE_1VS1, COMPETITION_TYPE_PASSING_CHALLENGE) */
        competitionType = GameControlData.COMPETITION_TYPE_1VS1;
        /** Number of hardware penalties per half before the robot is ejected. */
        allowedHardwarePenaltiesPerHalf = 3;
        /** Number of hardware penalties per game before the robot is ejected. */
        allowedHardwarePenaltiesPerGame = 3;
        /** The score factor that a team gets for using the fully autonomous calibration procedure. */
        autonomousCalibrationScoreFactor = 1.5f;
    }
}
