package data;

import java.awt.Color;

/**
 *
 * @author Michel-Zen
 * 
 * This class sets attributes given by the humanoid-league rules.
 */
public class HL extends Rules
{
    HL()
    {
        /** The league´s name this rules are for. */
        leagueName = "HL Kid";
        /** The league´s directory name with it´s teams and icons. */
        leagueDirectory = "hl_kid";
        /** How many robots are in a team. */
        teamSize = 3;
        /** The Java Colors the left and the right team starts with. */
        teamColor = new Color[] {Color.BLUE, Color.RED};
        /** The name of the colors. */
        teamColorName = new String[] {"Blue", "Red"};
        /** If the colors change automatically. */
        colorChangeAuto = false;
        /** If the colors may be changed manually. */
        colorChangeManual = true;
        /** If the clock may stop in certain states (Ready, Set) in a play-off game. */
        playOffTimeStop = false;
        /** Time in seconds one half is long. */
        halfTime = 10*60;
        /** Time in seconds the ready state is long. */
        readyTime = 30;
        /** Time in seconds between first and second half. */
        pauseTime = 5*60;
        /** If left and right side may both have the first kickoff. */
        kickoffChoice = true;
        /** Time in seconds the ball is blocked after kickoff. */
        kickoffTime = 10;
        /** Time in seconds before a global game stuck can be called. */
        minDurationBeforeStuck = 30;
        /** If there is an overtime before penalty-shoot in a play-off game. */
        overtime = true;
        /** Time in seconds one overtime half is long. */
        overtimeTime = 5*60;
        /** If the game starts with penalty-shoots. */
        startWithPenalty = false;
        /** Time in seconds between second half and penalty shoot. */
        pausePenaltyShootOutTime = 5*60;
        /** If there can be a penalty-shoot retry. */
        penaltyShotRetries = true;
        /** Time in seconds one penalty shoot is long. */
        penaltyShotTime = 1*60;
        /** If there is a sudden-death. */
        suddenDeath = false;
        /** Time in seconds one penalty shoot is long in sudden-death. */
        penaltyShotTimeSuddenDeath = 2*60; // does not matter
        /** Number of penalty-shoots for each team when a half has 10minutes. */
        numberOfPenaltyShotsShort = 3;
        /** Number of penalty-shoots for each team after full 10minutes playing. */
        numberOfPenaltyShotsLong = 5;
        /** Time in seconds normal penalties take. */
        penaltyStandardTime = 30;
        /** Time in seconds a robot is taken out when manually penalized (ChestButton). */
        penaltyManualTime = 1;
        /** if robots should return from penalties when the game state changes. */
        returnRobotsInGameStoppages = false;
        /** Time in seconds one team has as timeOut. */
        timeOutTime = 2*60;
        /** If true, the timeOutTime will be resetted for each timeOut. */
        timeOutTimeResette = true;
        /** How many times a team may take a timeOut. */
        timeOutMaxNumber = 99; // does not matter because of timeOutMaxNumberHalf = 1
        /** How many times a team may take a timeOut within one half. */
        timeOutMaxNumberHalf = 1;
        /** On how many pushings is a robot ejected. */
        pushesToEjection = new int[] {};
    }
}