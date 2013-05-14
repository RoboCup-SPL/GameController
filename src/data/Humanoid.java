package data;

import java.awt.Color;

/**
 *
 * @author Michel-Zen
 * 
 * This class sets attributes given by the humanoid-league rules.
 */
public class Humanoid extends Rules
{
    Humanoid()
    {
        /** The league´s name this rules are for. */
        leagueName = "Humanoid-League";
        /** The league´s directory name with it´s teams and icons. */
        leagueDirectory = "hl";
        /** How many robots are in a team. */
        teamSize = 3;
        /** The Java Colors the left and the right team starts with. */
        teamColor = new Color[2];
        teamColor[0] = Color.BLUE;
        teamColor[1] = Color.RED;
        /** The name of the colors. */
        teamColorName = new String[2];
        teamColorName[0] = "Blue";
        teamColorName[1] = "Red";
        /** If the clock may stop in certain states (Ready, Set) in a play-off game. */
        playOffTimeStop = false;
        /** Time in seconds one half is long. */
        halfTime = 10*60;
        /** Time in seconds the ready state is long. */
        readyTime = 30;
        /** Time in seconds between first and second half. */
        pauseTime = 5*60;
        /** Time in seconds the ball is blocked after kickoff. */
        kickoffTime = 10;
        /** Time in seconds before a global game stuck can be called. */
        minDurationBeforeStuck = 30;
        /** Time in seconds between second half and penalty shoot. */
        pausePenaltyShootTime = 5*60;
        /** Time in seconds one penalty shoot is long. */
        penaltyShootTime = 1*60;
        /** If there is a sudden-death. */
        suddenDeath = false;
        /** Time in seconds one penalty shoot is long in sudden-death. */
        penaltyShootTimeSuddenDeath = 2*60; // does not matter
        /** Number of penalty-shoots for each team when a half has 10minutes. */
        numberOfPenaltyShootsShort = 3;
        /** Number of penalty-shoots for each team after full 10minutes playing. */
        numberOfPenaltyShootsLong = 5;
        /** Time in seconds normal penalties take. */
        penaltyStandardTime = 30;
        /** Time in seconds a robot is taken out when manually penalized (ChestButton). */
        penaltyManualTime = 1;
        /** if all penalties should be removed by switching to the set state. */
        removePenaltiesInSet = false;
        /** Time in seconds one team has as timeOut. */
        timeOutTime = 2*60;
        /** If true, the timeOutTime will be resetted for each timeOut. */
        timeOutTimeResette = true;
        /** How many times a team may take a timeOut. */
        timeOutMaxNumber = 99; // does not matter because of timeOutMaxNumberHalf = 1
        /** How many times a team may take a timeOut within one half. */
        timeOutMaxNumberHalf = 1;
        /** On how many pushings is a robot ejected. */
        pushesToEjection = new int[3];
        pushesToEjection[0] = 99;
        pushesToEjection[1] = 99;
        pushesToEjection[2] = 99;
    }
}