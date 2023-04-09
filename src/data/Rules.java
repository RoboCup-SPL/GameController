package data;

import java.awt.Color;


/**
 * This class holds attributes defining rules.
 *
 * @author Michel Bartsch
 */
public abstract class Rules
{
    /** Note all league's rules here to have them available. */
    public static final Rules[] LEAGUES = {
        new SPL(),
        new SPLPenaltyShootout(),
        new SPL7v7(),
        new SPLDynamicBallHandling()
    };

    /**
     * Returns the Rules object for the given class.
     */
    public static Rules getLeagueRules(final Class<? extends Rules> c) {
        for(final Rules r : LEAGUES) {
            if(c.isInstance(r) && r.getClass().isAssignableFrom(c)) {
                return r;
            }
        }
        return null;
    }

    /** The rules of the league playing. */
    public static Rules league = LEAGUES[0];
    /** The league's name this rules are for. */
    public String leagueName;
    /** The league's directory name with its teams and icons. */
    public String leagueDirectory;
    /** How many robots are in a team. */
    public int teamSize;
    /** How many robots of each team may play at one time. */
    public int robotsPlaying;
    /** The Java Colors the left and the right team starts with. */
    public Color[] teamColor;
    /** The name of the colors. */
    public String[] teamColorName;
    /** If the clock may stop in certain states (Ready, Set) in a play-off game. */
    public boolean playOffTimeStop;
    /** Time in seconds one half is long. */
    public int halfTime;
    /** Time in seconds the ready state is long. */
    public int readyTime;
    /** Time in seconds between first and second half. */
    public int pauseTime;
    /** If left and right side may both have the first kickoff. */
    public boolean kickoffChoice;
    /** Time in seconds the ball is blocked after kickoff. */
    public int kickoffTime;
    /** Time in seconds the ball is blocked after a free kick. */
    public int freeKickTime;
    /** Time in seconds the the ready state during a penalty kick is long. */
    public int penaltyKickReadyTime;
    /** Time in seconds before a global game stuck can be called. */
    public int minDurationBeforeStuck;
    /** The number of seconds switching to Playing is delayed. */
    public int delayedSwitchToPlaying;
    /** The number of seconds switching to Ready after a goal is delayed. */
    public int delayedSwitchAfterGoal;
    /** If there is an overtime before the penalty shoot-out in a play-off game. */
    public boolean overtime;
    /** Time in seconds one overtime half is long. */
    public int overtimeTime;
    /** If the game starts with penalty-shots. */
    public boolean startWithPenalty;
    /** Time in seconds between second half and penalty shoot-out. */
    public int pausePenaltyShootOutTime;
    /** Time in seconds one penalty shoot is long. */
    public int penaltyShotTime;
    /** If there can be a penalty-shot retry. */
    public boolean penaltyShotRetries;
    /** If there is a sudden-death. */
    public boolean suddenDeath;
    /** Time in seconds one penalty shoot is long in sudden-death. */
    public int penaltyShotTimeSuddenDeath;
    /** Number of penalty-shots for each team. */
    public int numberOfPenaltyShots;
    /** Time in seconds for each kind of penalty. */
    public int[] penaltyTime;
    /** Time in seconds to increment penalties. */
    public int penaltyIncreaseTime;
    /** Whether the penalty count is reset on halftime */
    public boolean resetPenaltyCountOnHalftime;
    /** Whether penalties can be removed before the penalty time has passed. */
    public boolean allowEarlyPenaltyRemoval;
    /** Penalty that players get when they substitute another player. */
    public byte substitutePenalty;
    /** if robots should return from penalties when the game state changes. */
    public boolean returnRobotsInGameStoppages;
    /** Time in seconds one team has as timeOut. */
    public int timeOutTime;
    /** Time in seconds of a referee timeout. */
    public int refereeTimeout;
    /** Defines if the option for a referee timeout is available. */
    public boolean isRefereeTimeoutAvailable;
    /** One time-out per half? */
    public boolean timeOutPerHalf;
    /** Allowed to compensate for lost time? */
    public boolean lostTime;
    /** If true, the game controller should drop broadcast-messages */
    public boolean dropBroadcastMessages;
    /** The type of the competition (COMPETITION_TYPE_NORMAL, COMPETITION_TYPE_CHALLENGE_SHIELD, etc) */
    public byte competitionType;
    /** Number of hardware penalties per half before the robot is ejected. */
    public int allowedHardwarePenaltiesPerHalf;
    /** Number of hardware penalties per game before the robot is ejected. */
    public int allowedHardwarePenaltiesPerGame;
    /** Number of team messages a team is allowed to send per game. */
    public short overallMessageBudget;
    /** Number of team messages that are added to the budget per minute of extra time.  */
    public short additionalMessageBudgetPerMinute;
}
