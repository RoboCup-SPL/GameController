package controller.action;

import controller.action.clock.ClockTick;
import controller.action.ui.CancelUndo;
import controller.action.ui.ClockPause;
import controller.action.ui.ClockReset;
import controller.action.ui.CornerKick;
import controller.action.ui.GlobalStuck;
import controller.action.ui.Goal;
import controller.action.ui.GoalKick;
import controller.action.ui.IncGameClock;
import controller.action.ui.KickIn;
import controller.action.ui.KickOff;
import controller.action.ui.Quit;
import controller.action.ui.RefereeTimeout;
import controller.action.ui.Robot;
import controller.action.ui.Testmode;
import controller.action.ui.TimeOut;
import controller.action.ui.Undo;
import controller.action.ui.half.FirstHalf;
import controller.action.ui.half.FirstHalfOvertime;
import controller.action.ui.half.PenaltyShoot;
import controller.action.ui.half.SecondHalf;
import controller.action.ui.half.SecondHalfOvertime;
import controller.action.ui.penalty.Foul;
import controller.action.ui.penalty.FoulPenaltyArea;
import controller.action.ui.penalty.MotionInSet;
import controller.action.ui.penalty.BallContact;
import controller.action.ui.penalty.Inactive;
import controller.action.ui.penalty.Leaving;
import controller.action.ui.penalty.LocalGameStuck;
import controller.action.ui.penalty.PickUp;
import controller.action.ui.penalty.Position;
import controller.action.ui.penalty.Pushing;
import controller.action.ui.state.Finish;
import controller.action.ui.state.Initial;
import controller.action.ui.state.Play;
import controller.action.ui.state.Ready;
import controller.action.ui.state.Set;
import data.Rules;


/**
 * @author Michel Bartsch
 *
 * This class actually holds static every instance of an action to get these
 * actions where ever you want to execute or identify them.
 * It may be usefull to have instances of actions that are not listed here,
 * that would be ok but for basic features it should not be needed.
 * Because of multi-thredding you should not take actions from here to write
 * into their attributes. However, you should allways avoid writing in
 * action`s attributes except in their constructor.
 *
 * You can read a detailed description of each action in it`s class.
 */
public class ActionBoard
{
    public static ClockTick clock;

    public static Quit quit;
    public static Testmode testmode;
    public static Undo[] undo;
    public static CancelUndo cancelUndo;
    public static final int MAX_NUM_UNDOS_AT_ONCE = 8;

    public static Goal[] goalDec = new Goal[2];
    public static Goal[] goalInc = new Goal[2];
    public static KickOff[] kickOff = new KickOff[2];
    public static Robot[][] robot;
    public static GoalKick[] goalKick = new GoalKick[2];
    public static TimeOut[] timeOut = new TimeOut[2];
    public static GlobalStuck[] stuck = new GlobalStuck[2];
    public static KickIn[] kickIn = new KickIn[2];
    public static CornerKick[] cornerKick = new CornerKick[2];
    public static ClockReset clockReset;
    public static ClockPause clockPause;
    public static IncGameClock incGameClock;
    public static FirstHalf firstHalf;
    public static SecondHalf secondHalf;
    public static FirstHalfOvertime firstHalfOvertime;
    public static SecondHalfOvertime secondHalfOvertime;
    public static PenaltyShoot penaltyShoot;
    public static RefereeTimeout refereeTimeout;
    public static Initial initial;
    public static Ready ready;
    public static Set set;
    public static Play play;
    public static Finish finish;
    public static Pushing pushing;
    public static Leaving leaving;
    public static MotionInSet motionInSet;
    public static Inactive inactive;
    public static Position position;
    public static BallContact ballContact;
    public static PickUp pickUp;
    public static LocalGameStuck localGameStuck;
    public static Foul foul;
    public static FoulPenaltyArea foulPenaltyArea;



    /**
     * This must be called before using actions from this class. It creates
     * all the actions instances.
     */
    public static void init()
    {
        clock = new ClockTick();

        quit = new Quit();
        testmode = new Testmode();
        undo = new Undo[MAX_NUM_UNDOS_AT_ONCE];
        for (int i=0; i<undo.length; i++) {
            undo[i] = new Undo(i);
        }
        cancelUndo = new CancelUndo();

        robot = new Robot[2][Rules.league.teamSize];

        for (int i=0; i<2; i++) {
            goalDec[i] = new Goal(i, -1);
            goalInc[i] = new Goal(i, 1);
            kickOff[i] = new KickOff(i);
            for (int j=0; j<robot[i].length; j++) {
                robot[i][j] = new Robot(i, j);
            }
            timeOut[i] = new TimeOut(i);
            stuck[i] = new GlobalStuck(i);
            kickIn[i] = new KickIn(i);
            goalKick[i] = new GoalKick(i);
            cornerKick[i] = new CornerKick(i);
        }

        clockReset = new ClockReset();
        clockPause = new ClockPause();
        incGameClock = new IncGameClock();
        firstHalf = new FirstHalf();
        secondHalf = new SecondHalf();
        firstHalfOvertime = new FirstHalfOvertime();
        secondHalfOvertime = new SecondHalfOvertime();
        penaltyShoot = new PenaltyShoot();
        refereeTimeout = new RefereeTimeout();

        initial = new Initial();
        ready = new Ready();
        set = new Set();
        play = new Play();
        finish = new Finish();

        pushing = new Pushing();
        leaving = new Leaving();
        motionInSet = new MotionInSet();
        inactive = new Inactive();
        position = new Position();
        ballContact = new BallContact();
        pickUp = new PickUp();
        localGameStuck = new LocalGameStuck();
        foul = new Foul();
        foulPenaltyArea = new FoulPenaltyArea();
    }
}
