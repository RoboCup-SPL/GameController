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
        teamColor = new Color[] {Color.BLUE, Color.RED};
        /** The name of the colors. */
        teamColorName = new String[] {"Blue", "Red"};
        /** If the colors change automatically. */
        colorChangeAuto = true;
        /** If the colors may be changed manually. */
        colorChangeManual = false;
        /** If the clock may stop in certain states (Ready, Set) in a play-off game. */
        playOffTimeStop = true;
        /** Time in seconds one half is long. */
        halfTime = 10*60;
        /** Time in seconds the ready state is long. */
        readyTime = 45;
        /** Time in seconds between first and second half. */
        pauseTime = 10*60;
        /** If left and right side may both have the first kickoff. */
        kickoffChoice = false;
        /** Time in seconds the ball is blocked after kickoff. */
        kickoffTime = 10;
        /** Time in seconds before a global game stuck can be called. */
        minDurationBeforeStuck = 15;
        /** If there is an overtime before penalty-shoot in a play-off game. */
        overtime = false;
        /** Time in seconds one overtime half is long. */
        overtimeTime = 0;
        /** If the game starts with penalty-shoots. */
        startWithPenalty = false;
       /** Time in seconds between second half and penalty shoot. */
        pausePenaltyShootOutTime = 5*60;
        /** Time in seconds one penalty shoot is long. */
        penaltyShotTime = 1*60;
        /** If there can be a penalty-shoot retry. */
        penaltyShotRetries = false;
        /** If there is a sudden-death. */
        suddenDeath = true;
        /** Time in seconds one penalty shoot is long in sudden-death. */
        penaltyShotTimeSuddenDeath = 2*60;
        /** Number of penalty-shoots for each team when a half has 10minutes. */
        numberOfPenaltyShotsShort = 3;
        /** Number of penalty-shoots for each team after full 10minutes playing. */
        numberOfPenaltyShotsLong = 5;
        /** Time in seconds normal penalties take. */
        penaltyStandardTime = 30;
        /** Time in seconds a robot is taken out when manually penalized (ChestButton). */
        penaltyManualTime = 1;
        /** if robots should return from penalties when the game state changes. */
        returnRobotsInGameStoppages = true;
        /** Time in seconds one team has as timeOut. */
        timeOutTime = 5*60;
        /** One time-out per half? */
        timeOutPerHalf = false;
        /** On how many pushings is a robot ejected. */
        pushesToEjection = new int[] {4, 6, 8, 10, 12};
    }
}