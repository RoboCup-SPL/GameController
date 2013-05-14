package data;

import java.awt.Color;


/**
 * @author: Michel Bartsch
 * 
 * This class holds attributes defining rules.
 */
public abstract class Rules
{   
    /** Note all league´s rules here to have them available. */
    public static final Rules[] LEAGUES = {
        new SPL(),
        new Humanoid()
    };
    
    /** The rules of the league playing. */
    public static Rules league = LEAGUES[0];
    
    /** The league´s name this rules are for. */
    public String leagueName;
    /** The league´s directory name with it´s teams and icons. */
    public String leagueDirectory;
    /** How many robots are in a team. */
    public int teamSize;
    /** The Java Colors the left and the right team starts with. */
    public Color[] teamColor;
    /** The name of the colors. */
    public String[] teamColorName;
    /** Time in seconds one half is long. */
    public int halfTime;
    /** Time in seconds the ready state is long. */
    public int readyTime;
    /** Time in seconds between first and second half. */
    public int pauseTime;
    /** Time in seconds the ball is blocked after kickoff. */
    public int kickoffTime;
    /** Time in seconds before a global game stuck can be called. */
    public int minDurationBeforeStuck;
    /** Time in seconds between second half and penalty shoot. */
    public int pausePenaltyShootTime;
    /** Time in seconds one penalty shoot is long. */
    public int penaltyShootTime;
    /** If there is a sudden-death. */
    public boolean suddenDeath;
    /** Time in seconds one penalty shoot is long in sudden-death. */
    public int penaltyShootTimeSuddenDeath;
    /** Number of penalty-shoots for each team when a half has 10minutes. */
    public int numberOfPenaltyShootsShort;
    /** Number of penalty-shoots for each team after full 10minutes playing. */
    public int numberOfPenaltyShootsLong;
    /** Time in seconds normal penalties take. */
    public int penaltyStandardTime;
    /** Time in seconds a robot is taken out when manually penalized (ChestButton). */
    public int penaltyManualTime;
    /** Time in seconds one team has as timeOut. */
    public int timeOutTime;
    /** If true, the timeOutTime will be resetted for each timeOut. */
    public boolean timeOutTimeResette;
    /** How many times a team may take a timeOut. */
    public int timeOutMaxNumber;
    /** How many times a team may take a timeOut within one half. */
    public int timeOutMaxNumberHalf;
    /** On how many pushings is a robot ejected. */
    public int[] pushesToEjection;
}