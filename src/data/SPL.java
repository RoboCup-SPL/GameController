package data;

import java.awt.Color;

/**
 * This class sets attributes given by the spl rules.
 *
 * @author Michel-Zen
 */
public class SPL extends Rules
{
    SPL()
    {
        /* The league's name this rules are for. */
        leagueName = "SPL";
        /* The league's directory name with its teams and icons. */
        leagueDirectory = "spl";
        /* How many robots are in a team. */
        teamSize = 6; // 5 players + 1 sub
        /* How many robots of each team may play at one time. */
        robotsPlaying = 5;
        /* The Java Colors the left and the right team starts with. */
        teamColor = new Color[] {Color.BLUE, Color.RED, new Color(224, 200, 0), Color.BLACK, Color.WHITE, new Color(0, 192, 0), new Color(255, 165, 0), new Color(128, 0, 128), new Color(165, 42, 42), new Color(128, 128, 128)};
        /* The name of the colors. */
        teamColorName = new String[] {"Blue", "Red", "Yellow", "Black", "White", "Green", "Orange", "Purple", "Brown", "Gray"};
        /* If the clock may stop in certain states (Ready, Set) in a play-off game. */
        playOffTimeStop = true;
        /* Time in seconds one half is long. */
        halfTime = 10*60;
        /* Time in seconds the ready state is long. */
        readyTime = 45;
        /* Time in seconds between first and second half. */
        pauseTime = 10*60;
        /* If left and right side may both have the first kickoff. */
        kickoffChoice = false;
        /* Time in seconds the ball is blocked after kickoff. */
        kickoffTime = 10;
        /* Time in seconds the ball is blocked after a free kick. */
        freeKickTime = 30;
        /* Time in seconds the ready state during a penalty kick is long. */
        penaltyKickReadyTime = 30;
        /* Time in seconds before a global game stuck can be called. */
        minDurationBeforeStuck = 30;
        /* The number of seconds switching to Playing is delayed. */
        delayedSwitchToPlaying = 15;
        /* The number of seconds switching to Ready after a goal is delayed. */
        delayedSwitchAfterGoal = 15;
        /* If there is an overtime before penalty-shoot in a play-off game. */
        overtime = false;
        /* Time in seconds one overtime half is long. */
        overtimeTime = 0;
        /* If the game starts with penalty-shoots. */
        startWithPenalty = false;
        /* Time in seconds between second half and penalty shoot. */
        pausePenaltyShootOutTime = 0;
        /* Time in seconds one penalty shoot is long. */
        penaltyShotTime = 30;
        /* If there can be a penalty-shoot retry. */
        penaltyShotRetries = false;
        /* If there is a sudden-death. */
        suddenDeath = true;
        /* Time in seconds one penalty shoot is long in sudden-death. */
        penaltyShotTimeSuddenDeath = 30;
        /* Number of penalty-shots for each team. */
        numberOfPenaltyShots = 3;
        /* Time in seconds for each kind of penalty (-1 = should not be used). */
        penaltyTime = new int[] {-1, 45, 45, 15, 45, 45, 45, 45, 45, 15};
        /* Time in seconds to increment penalties. */
        penaltyIncreaseTime = 10;
        /* Whether the penalty count is reset on halftime */
        resetPenaltyCountOnHalftime = false;
        /* Whether penalties can be removed before the penalty time has passed. */
        allowEarlyPenaltyRemoval = false;
        /* Penalty that players get when they substitute another player. */
        substitutePenalty = PlayerInfo.PENALTY_SPL_REQUEST_FOR_PICKUP;
        /* if robots should return from penalties when the game state changes. */
        returnRobotsInGameStoppages = true;
        /* Time in seconds one team has as timeOut. */
        timeOutTime = 5*60;
        /* Time in seconds of a referee timeout */
        refereeTimeout = 10*60;
        /* Defines if the option for a referee timeout is available */
        isRefereeTimeoutAvailable = true;
        /* One time-out per half? */
        timeOutPerHalf = false;
        /* Allowed to compensate the lost time? */
        lostTime = true;
        /* If true, the game controller should drop broadcast-messages */
        dropBroadcastMessages = true;
        /* The type of the competition (COMPETITION_TYPE_NORMAL, COMPETITION_TYPE_CHALLENGE_SHIELD, etc) */
        competitionType = GameControlData.COMPETITION_TYPE_NORMAL;
        /* Number of hardware penalties per half before the robot is ejected. */
        allowedHardwarePenaltiesPerHalf = Integer.MAX_VALUE;
        /* Number of hardware penalties per game before the robot is ejected. */
        allowedHardwarePenaltiesPerGame = Integer.MAX_VALUE;
        /* Number of team messages a team is allowed to send per game. */
        overallMessageBudget = 1200;
        /* Number of team messages that are added to the budget per minute of extra time.  */
        additionalMessageBudgetPerMinute = 60;
    }
}
