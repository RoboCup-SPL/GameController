package data;

import java.awt.Color;

/**
 *
 * @author Michel-Zen
 * 
 * This class sets attributes given by the spl rules.
 */
public class SPL extends Rules
{
    SPL()
    {
        /** The league´s name this rules are for. */
        leagueName = "SPL";
        /** The league´s directory name with it´s teams and icons. */
        leagueDirectory = "spl";
        /** How many robots are in a team. */
        teamSize = 5;
        /** The Java Colors the left and the right team starts with. */
        teamColor = new Color[2];
        teamColor[0] = Color.BLUE;
        teamColor[1] = Color.RED;
        /** The name of the colors. */
        teamColorName = new String[2];
        teamColorName[0] = "Blue";
        teamColorName[1] = "Red";
        /** If the clock may stop in certain states (Ready, Set) in a play-off game. */
        playOffTimeStop = true;
        /** Time in seconds one half is long. */
        halfTime = 10*60;
        /** Time in seconds the ready state is long. */
        readyTime = 45;
        /** Time in seconds between first and second half. */
        pauseTime = 10*60;
        /** Time in seconds the ball is blocked after kickoff. */
        kickoffTime = 10;
        /** Time in seconds before a global game stuck can be called. */
        minDurationBeforeStuck = 30;
        /** Time in seconds between second half and penalty shoot. */
        pausePenaltyShootTime = 5*60;
        /** Time in seconds one penalty shoot is long. */
        penaltyShootTime = 1*60;
        /** If there is a sudden-death. */
        suddenDeath = true;
        /** Time in seconds one penalty shoot is long in sudden-death. */
        penaltyShootTimeSuddenDeath = 2*60;
        /** Number of penalty-shoots for each team when a half has 10minutes. */
        numberOfPenaltyShootsShort = 3;
        /** Number of penalty-shoots for each team after full 10minutes playing. */
        numberOfPenaltyShootsLong = 5;
        /** Time in seconds normal penalties take. */
        penaltyStandardTime = 30;
        /** Time in seconds a robot is taken out when manually penalized (ChestButton). */
        penaltyManualTime = 1;
        /** Time in seconds one team has as timeOut. */
        timeOutTime = 5*60;
        /** If true, the timeOutTime will be resetted for each timeOut. */
        timeOutTimeResette = false;
        /** How many times a team may take a timeOut. */
        timeOutMaxNumber = 1;
        /** How many times a team may take a timeOut within one half. */
        timeOutMaxNumberHalf = 99; //does not matter because of timeOutMaxNumber = 1
        /** On how many pushings is a robot ejected. */
        pushesToEjection = new int[5];
        pushesToEjection[0] = 4;
        pushesToEjection[1] = 6;
        pushesToEjection[2] = 8;
        pushesToEjection[3] = 10;
        pushesToEjection[4] = 12;
    }
}