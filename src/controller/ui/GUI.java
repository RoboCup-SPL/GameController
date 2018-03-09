package controller.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import common.Log;
import common.TotalScaleLayout;
import controller.Clock;
import controller.EventHandler;
import controller.action.ActionBoard;
import controller.action.GCAction;
import controller.net.RobotOnlineStatus;
import controller.net.RobotWatcher;
import data.AdvancedData;
import data.GameControlData;
import data.HL;
import data.PlayerInfo;
import data.Rules;
import data.SPL;
import data.Teams;


/**
 * @author Michel Bartsch
 * 
 * This is the main GUI.
 * In this class you will find the whole graphical output and the bindings
 * of buttons to their actions, nothing less and nothing more.
 */
public class GUI extends JFrame implements GCGUI
{
    private static final boolean IS_OSX = System.getProperty("os.name").contains("OS X");
    private static final Insets insets = IS_OSX ? new Insets (2, -30, 2, -30) : null;
    private static final String BUTTON_MASK = IS_OSX
            ? "<html><div style=\"padding: 0px 12px\"><center>%s</center></div></html>"
            : "<html><center>%s</center></html>";
    
    /** Fix button centering for Apple Java. */
    private class Button extends JButton
    {
        private static final long serialVersionUID = -1533689100759569853L;

        public Button(String text)
        {
            setMargin(insets);
            setText(text);
        }
        
        public void setText(String text)
        {
            super.setText(String.format(BUTTON_MASK, text));
        }
    }
    
    /** Fix button centering for Apple Java. */
    private class ToggleButton extends JToggleButton
    {
        private static final long serialVersionUID = -7733709666734108610L;

        public ToggleButton()
        {
            setMargin(insets);
        }
        
        public ToggleButton(String text)
        {
            setMargin(insets);
            setText(text);
        }
        
        public void setText(String text)
        {
            super.setText(String.format(BUTTON_MASK, text));
        }
    }
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Some constants defining this GUI`s appearance as their names say.
     * Feel free to change them and see what happens.
     */
    private static final int WINDOW_WIDTH = 1024;
    private static final int WINDOW_HEIGHT = 768;
    private static final int STANDARD_FONT_SIZE = 17;
    private static final int TITLE_FONT_SIZE = 24;
    private static final String STANDARD_FONT = "Helvetica";
    private static final int GOALS_FONT_SIZE = 60;
    private static final int TIME_FONT_SIZE = 50;
    private static final int TIME_SUB_FONT_SIZE = 40;
    private static final int TIMEOUT_FONT_SIZE = 14;
    private static final int STATE_FONT_SIZE = 12;
    private static final String WINDOW_TITLE = "GameController";
    private static final String ICONS_PATH = "config/icons/";
    private static final String BACKGROUND_EXT = ".png";
    private static final String BACKGROUND_MID = "field.png";
    private static final String BACKGROUND_CLOCK_SMALL = "time_ground_small.png";
    private static final String BACKGROUND_CLOCK = "time_ground.png";
    private static final String KICKING = "Kicking";
    private static final String KICKOFF_PENALTY_SHOOTOUT = "P.-taker";
    private static final String PENALTIES = "Penalties";
    private static final String SHOT = "Shot";
    private static final String SHOTS = "Shots";
    private static final String ONLINE = "wlan_status_green.png";
    private static final String OFFLINE = "wlan_status_red.png";
    private static final String HIGH_LATENCY = "wlan_status_yellow.png";
    private static final String UNKNOWN_ONLINE_STATUS = "wlan_status_grey.png";
    private static final String TIMEOUT = "Timeout";
    private static final String STUCK = "Global Game Stuck";
    private static final String REFEREE_TIMEOUT = "Referee<br/>Timeout";
    private static final String GOAL_FREE_KICK = "Goal Free Kick";
    private static final String PUSHING_FREE_KICK = "Pushing Free Kick";
    private static final String OUT = "Out";
    private static final String STATE_INITIAL = "Initial";
    private static final String STATE_READY = "Ready";
    private static final String STATE_SET = "Set";
    private static final String STATE_PLAY = "Play";
    private static final String STATE_FINISH = "Finish";
    private static final String CLOCK_RESET = "reset.png";
    private static final String CLOCK_PAUSE = "pause.png";
    private static final String CLOCK_PLAY = "play.png";
    private static final String CLOCK_PLUS = "plus.png";
    private static final String FIRST_HALF = "First Half";
    private static final String SECOND_HALF = "Second Half";
    private static final String FIRST_HALF_SHORT = "1st Half";
    private static final String SECOND_HALF_SHORT = "2nd Half";
    private static final String FIRST_HALF_OVERTIME = "1st Extra";
    private static final String SECOND_HALF_OVERTIME = "2nd Extra";
    private static final String PENALTY_SHOOT = "Penalty Shots";
    private static final String PENALTY_SHOOT_SHORT = "Penalty";
    private static final String PEN_PUSHING = "Pushing";
    private static final String PEN_LEAVING = "Leaving the Field";
    private static final String PEN_MOTION_IN_SET = "Motion in Set";
    private static final String PEN_MOTION_IN_SET_SHORT = "Motion";
    private static final String PEN_INACTIVE = "Fallen / Inactive";
    private static final String PEN_DEFENDER = "Illegal Defender";
    private static final String PEN_BALL_CONTACT = "Ball Holding / Hands";
    private static final String PEN_KICK_OFF_GOAL = "Kickoff Goal";
    private static final String PEN_PICKUP = "Pick-Up";
    private static final String PEN_MANIPULATION = "Ball Manipulation";
    private static final String PEN_PHYSICAL = "Physical Contact";
    private static final String PEN_DEFENSE = "Illegal Defense";
    private static final String PEN_ATTACK = "Illegal Attack";
    private static final String PEN_PICKUP_INCAPABLE = "Pickup/Incapable";
    private static final String PEN_SERVICE = "Service";
    private static final String PEN_FOUL = "Foul";
    private static final String PEN_SUBSTITUTE_SHORT = "Sub";
    private static final String PEN_LOCAL_GAME_STUCK = "Local Game Stuck";
    private static final String DROP_BALL = "Dropped Ball";
    private static final String CANCEL = "Cancel";
    private static final String BACKGROUND_BOTTOM = "timeline_ground.png";
    private static final Color COLOR_HIGHLIGHT = Color.YELLOW;
    private static final Color COLOR_STANDARD = (new JButton()).getBackground();
    private static final int UNPEN_HIGHLIGHT_SECONDS = 10;
    private static final int TIMEOUT_HIGHLIGHT_SECONDS = 10;
    private static final int FINISH_HIGHLIGHT_SECONDS = 10;

    private static final Color COLOR_PENALTY_SHOOTOUT_TAKER = new Color(0xBBFFBB);
    private static final Color COLOR_PENALTY_SHOOTOUT_KEEPER = new Color(0xFF5555);

    /** Some attributes used in the GUI components. */
    private double lastSize = 0;
    private Font standardFont;
    private Font titleFont;
    private Font goalsFont;
    private Font timeFont;
    private Font timeSubFont;
    private Font timeoutFont;
    private Font stateFont;
    private ImageIcon clockImgReset;
    private ImageIcon clockImgPlay;
    private ImageIcon clockImgPause;
    private ImageIcon clockImgPlus;
    private ImageIcon lanOnline;
    private ImageIcon lanHighLatency;
    private ImageIcon lanOffline;
    private ImageIcon lanUnknown;
    private ImageIcon[][] backgroundSide;
    
    /** All the components of this GUI. */
    private ImagePanel[] side;
    private JLabel[] name;
    private JButton[] goalDec;
    private JButton[] goalInc;
    private JLabel[] goals;
    private JRadioButton[] kickOff;
    private ButtonGroup kickOffGroup;
    private JLabel[] penalties;
    private JPanel[] robots;
    private JToggleButton[][] robot;
    private JLabel[][] robotLabel;
    private ImageIcon[][] lanIcon;
    private JProgressBar[][] robotTime;
    private JToggleButton refereeTimeout;
    private JToggleButton[] goalFreeKick;
    private JToggleButton[] pushingFreeKick;
    private JToggleButton[] timeOut;
    private JButton[] stuck;
    private JButton[] out;
    private JPanel mid;
    private JToggleButton initial;
    private JToggleButton ready;
    private JToggleButton set;
    private JToggleButton play;
    private JToggleButton finish;
    private ButtonGroup stateGroup;
    private ImageButton clockReset;
    private ImagePanel clockContainer;
    private JLabel clock;
    private JLabel clockSub;
    private JLabel freeKick;
    private ImageButton incGameClock;
    private ImageButton clockPause;
    private JToggleButton firstHalf;
    private JToggleButton secondHalf;
    private JToggleButton firstHalfOvertime;
    private JToggleButton secondHalfOvertime;
    private JToggleButton penaltyShoot;
    private ButtonGroup halfGroup;
    private JToggleButton[] pen;
    private JButton dropBall;
    private ImagePanel bottom;
    private JPanel log;
    private JToggleButton[] undo;
    private JButton cancelUndo;
  
    
    /**
     * Creates a new GUI.
     * 
     * @param fullscreen    If true, the GUI tries to start using the full
     *                      size of the screen. Actually this means changing
     *                      the display`s resolution to the GUI`s size.
     * @param data      The starting data.
     */
    public GUI(boolean fullscreen, String additionalInfo, GameControlData data)
    {
        super(WINDOW_TITLE + " (" + additionalInfo + ")");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setResizable(true);

        final Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        setLocation((int) center.getX() - WINDOW_WIDTH / 2, (int) center.getY() - WINDOW_HEIGHT / 2);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Clock.getInstance().interrupt();
                dispose();
            }
        });
        
        clockImgReset = new ImageIcon(ICONS_PATH+CLOCK_RESET);
        clockImgPlay = new ImageIcon(ICONS_PATH+CLOCK_PLAY);
        clockImgPause = new ImageIcon(ICONS_PATH+CLOCK_PAUSE);
        clockImgPlus = new ImageIcon(ICONS_PATH+CLOCK_PLUS);
        lanOnline = new ImageIcon(ICONS_PATH+ONLINE);
        lanHighLatency = new ImageIcon(ICONS_PATH+HIGH_LATENCY);
        lanOffline = new ImageIcon(ICONS_PATH+OFFLINE);
        lanUnknown = new ImageIcon(ICONS_PATH+UNKNOWN_ONLINE_STATUS);
        
        backgroundSide = new ImageIcon[2][Rules.league.teamColor.length];
        for (int i=0; i<backgroundSide.length; i++) {
            for (int j=0; j<backgroundSide[i].length; j++) {
                backgroundSide[i][j] = new BackgroundImage(ICONS_PATH+Rules.league.leagueDirectory+BACKGROUND_EXT, i==1, Rules.league.teamColor[j]);
            }
        }
        
        
        //Components
        stateGroup = new ButtonGroup();
        side = new ImagePanel[2];
        for (int i=0; i<2; i++) {
            side[i] = new ImagePanel(backgroundSide[i][i].getImage());
            side[i].setOpaque(true);
        }
        mid = new ImagePanel(new ImageIcon(ICONS_PATH+BACKGROUND_MID).getImage());
        bottom = new ImagePanel(new ImageIcon(ICONS_PATH+BACKGROUND_BOTTOM).getImage());
        
        //--side--
        //  score
        name = new JLabel[2];
        goalDec = new JButton[2];
        goalInc = new JButton[2];
        goals = new JLabel[2];
        kickOff = new JRadioButton[3];
        kickOffGroup = new ButtonGroup();
        penalties = new JLabel[2];
        timeOut = new JToggleButton[2];
        if(Rules.league instanceof SPL) {
            stuck = new JButton[2];
        }
        for (int i=0; i<2; i++) {
            name[i] = new JLabel(Teams.getNames(false)[data.team[i].teamNumber]);
            name[i].setHorizontalAlignment(JLabel.CENTER);
            if(data.team[i].teamColor == GameControlData.TEAM_WHITE) {
                name[i].setForeground(Color.BLACK);
            } else {
                name[i].setForeground(Rules.league.teamColor[data.team[i].teamColor]);
            }
            goalInc[i] = new Button("+");
            goalDec[i] = new Button("-");
            goalDec[i].setVerticalAlignment(SwingConstants.BOTTOM);
            kickOff[i] = new JRadioButton(KICKING);
            kickOff[i].setOpaque(false);
            kickOff[i].setHorizontalAlignment(JLabel.CENTER);
            kickOffGroup.add(kickOff[i]);
            goals[i] = new JLabel("0");
            goals[i].setHorizontalAlignment(JLabel.CENTER);
            penalties[i] = new JLabel(PENALTIES+": 0");
            penalties[i].setHorizontalAlignment(JLabel.CENTER);
            timeOut[i] = new ToggleButton(TIMEOUT);
            if(Rules.league instanceof SPL) {
                stuck[i] = new Button(STUCK);
            }
        }
        kickOff[2] = new JRadioButton();
        kickOffGroup.add(kickOff[2]);
        
        //  robots
        robots = new JPanel[2];
        robot = new JToggleButton[2][Rules.league.teamSize];
        robotLabel = new JLabel[2][Rules.league.teamSize];
        lanIcon = new ImageIcon[2][Rules.league.teamSize];
        robotTime = new JProgressBar[2][Rules.league.teamSize];
        
        for (int i=0; i<2; i++) {
            robots[i] = new JPanel();
            robots[i].setLayout(new GridLayout(robot[i].length, 1, 0, Math.max(0, 11 - robot[i].length) * 2));
            robots[i].setOpaque(false);
            
            for (int j=0; j<robot[i].length; j++) {
                robot[i][j] = new ToggleButton();
                robotLabel[i][j] = new JLabel();
                robotLabel[i][j].setHorizontalAlignment(JLabel.CENTER);
                lanIcon[i][j] = lanUnknown;
                robotLabel[i][j].setIcon(lanIcon[i][j]);
                robotTime[i][j] = new JProgressBar();
                robotTime[i][j].setMaximum(1000);
                robotTime[i][j].setVisible(false);
                TotalScaleLayout robotLayout = new TotalScaleLayout(robot[i][j]);
                robot[i][j].setLayout(robotLayout);
                robotLayout.add(.1, .1, .8, .8, robotLabel[i][j]);
                robotLayout.add(.1, .7, .8, .2, robotTime[i][j]);
                robots[i].add(robot[i][j]);
            }
        }
        //  team
        out = new JButton[2];        
        if (Rules.league instanceof SPL) {
            goalFreeKick = new JToggleButton[2];
            pushingFreeKick = new JToggleButton[2];
            for (int i=0; i<2; i++) {                
                goalFreeKick[i] = new ToggleButton(GOAL_FREE_KICK);
                pushingFreeKick[i] = new ToggleButton(PUSHING_FREE_KICK);
                pushingFreeKick[i].setVisible(false);
                out[i] = new JButton(OUT);
                stateGroup.add(goalFreeKick[i]);
                stateGroup.add(pushingFreeKick[i]);
            }
        } else {
            timeOut = new JToggleButton[2];
            for (int i=0; i<2; i++) {
                timeOut[i] = new ToggleButton(TIMEOUT);
                out[i] = new JButton(OUT);
            }
        }
        
        //--mid--
        //  time
        clockReset = new ImageButton(clockImgReset.getImage());
        clockReset.setOpaque(false);
        clockReset.setBorder(null);
        if (Rules.league.lostTime) {
            clockContainer = new ImagePanel(new ImageIcon(ICONS_PATH+BACKGROUND_CLOCK_SMALL).getImage());
        } else {
            clockContainer = new ImagePanel(new ImageIcon(ICONS_PATH+BACKGROUND_CLOCK).getImage());
        }
        clockContainer.setOpaque(false);
        clock = new JLabel("10:00");
        clock.setForeground(Color.WHITE);
        clock.setHorizontalAlignment(JLabel.CENTER);
        clockPause = new ImageButton(clockImgReset.getImage());
        clockPause.setOpaque(false);
        clockPause.setBorder(null);
        clockSub = new JLabel("0:00");
        clockSub.setForeground(Color.BLACK);
        clockSub.setHorizontalAlignment(JLabel.CENTER);
        incGameClock = new ImageButton(clockImgPlus.getImage());
        incGameClock.setOpaque(false);
        incGameClock.setBorder(null);
        freeKick = new JLabel("");
        freeKick.setHorizontalAlignment(JLabel.CENTER);
        if (!Rules.league.overtime) {
            firstHalf = new ToggleButton(FIRST_HALF);
            firstHalf.setSelected(true);
            secondHalf = new ToggleButton(SECOND_HALF);
            penaltyShoot = new ToggleButton(PENALTY_SHOOT);
            refereeTimeout = new ToggleButton(REFEREE_TIMEOUT);
            halfGroup = new ButtonGroup();
            halfGroup.add(firstHalf);
            halfGroup.add(secondHalf);
            halfGroup.add(penaltyShoot);
            
            if (Rules.league.isRefereeTimeoutAvailable) {
                halfGroup.add(refereeTimeout);
            }
        } else {
            firstHalf = new ToggleButton(FIRST_HALF_SHORT);
            firstHalf.setSelected(true);
            secondHalf = new ToggleButton(SECOND_HALF_SHORT);
            firstHalfOvertime = new ToggleButton(FIRST_HALF_OVERTIME);
            secondHalfOvertime = new ToggleButton(SECOND_HALF_OVERTIME);
            penaltyShoot = new ToggleButton(PENALTY_SHOOT_SHORT);
            refereeTimeout = new ToggleButton(REFEREE_TIMEOUT);
            halfGroup = new ButtonGroup();
            halfGroup.add(firstHalf);
            halfGroup.add(secondHalf);
            halfGroup.add(firstHalfOvertime);
            halfGroup.add(secondHalfOvertime);
            halfGroup.add(penaltyShoot);
            
            if (Rules.league.isRefereeTimeoutAvailable) {
                halfGroup.add(refereeTimeout);
            }
        }
        //  state
        initial = new ToggleButton(STATE_INITIAL);
        initial.setSelected(true);
        ready = new ToggleButton(STATE_READY);
        set = new ToggleButton(STATE_SET);
        play = new ToggleButton(STATE_PLAY);
        finish = new ToggleButton(STATE_FINISH);
        
        stateGroup.add(initial);
        stateGroup.add(ready);
        stateGroup.add(set);
        stateGroup.add(play);
        stateGroup.add(finish);
        //  penalties
        if (Rules.league instanceof SPL) {
            pen = new JToggleButton[10];
            
            pen[0] = new ToggleButton(PEN_PUSHING);
            pen[1] = new ToggleButton(PEN_FOUL);
            pen[2] = new ToggleButton(PEN_INACTIVE);
            pen[3] = new ToggleButton(PEN_LEAVING);
            pen[4] = new ToggleButton(PEN_MOTION_IN_SET);
            pen[5] = new ToggleButton(PEN_DEFENDER);
            pen[6] = new ToggleButton(PEN_BALL_CONTACT);
            pen[7] = new ToggleButton(PEN_KICK_OFF_GOAL);
            pen[8] = new ToggleButton(PEN_LOCAL_GAME_STUCK);
            pen[9] = new ToggleButton(PEN_PICKUP);
        } else if (Rules.league instanceof HL) {
            pen = new JToggleButton[6];
            pen[0] = new ToggleButton(PEN_MANIPULATION);
            pen[1] = new ToggleButton(PEN_PHYSICAL);
            pen[2] = new ToggleButton(PEN_ATTACK);
            pen[3] = new ToggleButton(PEN_DEFENSE);
            pen[4] = new ToggleButton(PEN_PICKUP_INCAPABLE);
            pen[5] = new ToggleButton(PEN_SERVICE);
            dropBall = new Button(DROP_BALL);
        }
        //--bottom--
        //  log
        log = new JPanel();
        log.setOpaque(false);
        log.setLayout(new GridLayout(1, ActionBoard.MAX_NUM_UNDOS_AT_ONCE-1, 10, 0));
        undo = new JToggleButton[ActionBoard.MAX_NUM_UNDOS_AT_ONCE-1];
        for (int i=undo.length-1; i>=0; i--) {
            undo[i] = new ToggleButton();
            undo[i].setVisible(false);
            log.add(undo[i]);
        }
        cancelUndo = new Button(CANCEL);
        cancelUndo.setVisible(false);
      
        //--layout--
        TotalScaleLayout layout = new TotalScaleLayout(this);
        setLayout(layout);
        
        layout.add(0, 0, .3, .04, name[0]);
        layout.add(.7, 0, .3, .04, name[1]);
        layout.add(.17, .05, .12, .04, kickOff[0]);
        layout.add(.71, .05, .12, .04, kickOff[1]);
        layout.add(.21, .09, .08, .07, goals[0]);
        layout.add(.71, .09, .08, .07, goals[1]);
        layout.add(.17, .16, .12, .04, penalties[0]);
        layout.add(.71, .16, .12, .04, penalties[1]);
        layout.add(.01, .21, .28, .55, robots[0]);
        layout.add(.71, .21, .28, .55, robots[1]);
        if (Rules.league instanceof SPL) {
            layout.add(.01, .05, .08, .065, timeOut[0]);
            layout.add(.91, .05, .08, .065, timeOut[1]);
            layout.add(.01, .13, .08, .065, stuck[0]);
            layout.add(.91, .13, .08, .065, stuck[1]);
            layout.add(.01, .77, .1375, .09, goalFreeKick[0]);
            layout.add(.8525, .77, .1375, .09, goalFreeKick[1]);
            layout.add(.105, .77, .09, .09, pushingFreeKick[0]);
            layout.add(.805, .77, .091, .09, pushingFreeKick[1]);
            layout.add(.1525, .77, .1375, .09, out[0]);
            layout.add(.71, .77, .1375, .09, out[1]);
            layout.add(.1, .05, .08, .065, goalInc[0]);
            layout.add(.82, .05, .08, .065, goalInc[1]);
            layout.add(.1, .13, .08, .065, goalDec[0]);
            layout.add(.82, .13, .08, .065, goalDec[1]);
        } else {
            layout.add(.01, .77, .135, .09, timeOut[0]);
            layout.add(.855, .77, .135, .09, timeOut[1]);
            layout.add(.155, .77, .135, .09, out[0]);
            layout.add(.71, .77, .135, .09, out[1]);
            layout.add(.01, .05, .08, .07, goalInc[0]);
            layout.add(.91, .05, .08, .07, goalInc[1]);
            layout.add(.01, .13, .08, .06, goalDec[0]);
            layout.add(.91, .13, .08, .06, goalDec[1]);
        }
        layout.add(.31, .0, .08, .11, clockReset);
        layout.add(.4, .012, .195, .10, clock);
        layout.add(.61, .0, .08, .11, clockPause);
        layout.add(.4, .11, .2, .07, clockSub);
        layout.add(.4, .15, .2, .07, freeKick);
        if (Rules.league.lostTime) {
            layout.add(.590, .0, .03, .11, incGameClock);
            layout.add(.4, .0, .195, .11, clockContainer);
        }
        else{
            layout.add(.4, .0, .2, .11, clockContainer);
        }
        if (!Rules.league.overtime) {
            if (Rules.league.isRefereeTimeoutAvailable) {
                layout.add(.31, .2, .09, .06, firstHalf);
                layout.add(.407, .2, .09, .06, secondHalf);
                layout.add(.503, .2, .09, .06, penaltyShoot);
                layout.add(.60, .2, .09, .06, refereeTimeout);
            } else {
                layout.add(.31, .2, .12, .06, firstHalf);
                layout.add(.44, .2, .12, .06, secondHalf);
                layout.add(.57, .2, .12, .06, penaltyShoot);
            }
        } else {
            if (Rules.league.isRefereeTimeoutAvailable) {
                layout.add(.31, .2, .06, .06, firstHalf);
                layout.add(.375, .2, .06, .06, secondHalf);
                layout.add(.439, .2, .06, .06, firstHalfOvertime);
                layout.add(.501, .2, .06, .06, secondHalfOvertime);
                layout.add(.565, .2, .06, .06, penaltyShoot);
                layout.add(.63, .2, .06, .06, refereeTimeout);
            } else {
                layout.add(.31, .2, .07, .06, firstHalf);
                layout.add(.3875, .2, .07, .06, secondHalf);
                layout.add(.465, .2, .07, .06, firstHalfOvertime);
                layout.add(.5425, .2, .07, .06, secondHalfOvertime);
                layout.add(.62, .2, .07, .06, penaltyShoot);
            }
        }
        layout.add(.31, .27, .07, .08, initial);
        layout.add(.3875, .27, .07, .08, ready);
        layout.add(.465, .27, .07, .08, set);
        layout.add(.5425, .27, .07, .08, play);
        layout.add(.62, .27, .07, .08, finish);
        if (Rules.league instanceof SPL) {
            layout.add(.31, .38, .185, .08, pen[0]);
            layout.add(.505, .38, .185, .08, pen[1]);
            layout.add(.31, .48, .185, .08, pen[2]);
            layout.add(.505, .48, .185, .08, pen[3]);
            layout.add(.31, .58, .185, .08, pen[4]);
            layout.add(.505, .58, .185, .08, pen[5]);
            layout.add(.31, .68, .185, .08, pen[6]);
            layout.add(.505, .68, .185, .08, pen[7]);
            layout.add(.31, .78, .185, .08, pen[8]);
            layout.add(.505, .78, .185, .08, pen[9]);
        } else if (Rules.league instanceof HL) {
            layout.add(.31,  .38, .185, .08, pen[0]);
            layout.add(.505, .38, .185, .08, pen[1]);
            layout.add(.31,  .48, .185, .08, pen[2]);
            layout.add(.505, .48, .185, .08, pen[3]);
            layout.add(.31,  .58, .185, .08, pen[4]);
            layout.add(.505, .58, .185, .08, pen[5]);
            layout.add(.31,  .68, .185, .08, pen[6]);
            layout.add(.31,  .78, .38,  .08, dropBall);
        }
        layout.add(.08, .88, .84, .11, log);
        layout.add(.925, .88, .07, .11, cancelUndo);
        layout.add(0, 0, .3, .87, side[0]);
        layout.add(.3, 0, .4, .87, mid);
        layout.add(.7, 0, .3, .87, side[1]);
        layout.add(0, .87, 1, .132, bottom);
        
        //--listener--
        for (int i=0; i<2; i++) {
            goalDec[i].addActionListener(ActionBoard.goalDec[i]);
            goalInc[i].addActionListener(ActionBoard.goalInc[i]);
            kickOff[i].addActionListener(ActionBoard.kickOff[i]);
            for (int j=0; j<robot[i].length; j++) {
                robot[i][j].addActionListener(ActionBoard.robot[i][j]);
            }
            out[i].addActionListener(ActionBoard.out[i]);
            timeOut[i].addActionListener(ActionBoard.timeOut[i]);
            if (Rules.league instanceof SPL) {
                stuck[i].addActionListener(ActionBoard.stuck[i]);
                goalFreeKick[i].addActionListener(ActionBoard.goalFreeKick[i]);
            }
        }
        refereeTimeout.addActionListener(ActionBoard.refereeTimeout);
        initial.addActionListener(ActionBoard.initial);
        ready.addActionListener(ActionBoard.ready);
        set.addActionListener(ActionBoard.set);
        play.addActionListener(ActionBoard.play);
        finish.addActionListener(ActionBoard.finish);
        clockReset.addActionListener(ActionBoard.clockReset);
        clockPause.addActionListener(ActionBoard.clockPause);
        if (Rules.league.lostTime) {
            incGameClock.addActionListener(ActionBoard.incGameClock);
        }
        firstHalf.addActionListener(ActionBoard.firstHalf);
        secondHalf.addActionListener(ActionBoard.secondHalf);
        if (Rules.league.overtime) {
            firstHalfOvertime.addActionListener(ActionBoard.firstHalfOvertime);
            secondHalfOvertime.addActionListener(ActionBoard.secondHalfOvertime);
        }
        penaltyShoot.addActionListener(ActionBoard.penaltyShoot);
        if (Rules.league instanceof SPL) {
            pen[0].addActionListener(ActionBoard.pushing);
            pen[1].addActionListener(ActionBoard.foul);
            pen[2].addActionListener(ActionBoard.inactive);
            pen[3].addActionListener(ActionBoard.leaving);
            pen[4].addActionListener(ActionBoard.motionInSet);
            pen[5].addActionListener(ActionBoard.defender);
            pen[6].addActionListener(ActionBoard.ballContact);
            pen[7].addActionListener(ActionBoard.kickOffGoal);
            pen[8].addActionListener(ActionBoard.localGameStuck);
            pen[9].addActionListener(ActionBoard.pickUp);
        } else if (Rules.league instanceof HL) {
            pen[0].addActionListener(ActionBoard.ballManipulation);
            pen[1].addActionListener(ActionBoard.pushing);
            pen[2].addActionListener(ActionBoard.attack);
            pen[3].addActionListener(ActionBoard.defense);
            pen[4].addActionListener(ActionBoard.pickUpHL);
            pen[5].addActionListener(ActionBoard.serviceHL);
            dropBall.addActionListener(ActionBoard.dropBall);
        }
        for (int i=0; i<undo.length; i++) {
            undo[i].addActionListener(ActionBoard.undo[i+1]);
        }
        cancelUndo.addActionListener(ActionBoard.cancelUndo);
      
        //fullscreen
        if (fullscreen) {
            setUndecorated(true);
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(this);
        }
        
        setVisible(true);
    }
    
    /**
     * @author Michel Bartsch
     * 
     * This is a normal JPanel, but it has a background image.
     */
    class ImagePanel extends JPanel
    {
        private static final long serialVersionUID = 1L;
        
        
        /** The image that is shown in the background. */
        private Image image;

        /**
         * Creates a new ImagePanel.
         * 
         * @param image     The Image to be shown in the background.
         */
        public ImagePanel(Image image)
        {
            this.image = image;
        }
        
        /**
         * Changes the background image.
         * 
         * @param image     Changes the image to this one.
         */
        public void setImage(Image image)
        {
            this.image = image;
        }
        
        /**
         * Paints this Component, should be called automatically.
         * 
         * @param g     This components graphical content.
         */
        @Override
        public void paintComponent(Graphics g)
        {
            if (super.isOpaque()) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        }
    }
    
    /**
     * @author Michel Bartsch
     * 
     * This is a normal JButton, but it has a background image.
     */
    class ImageButton extends JButton
    {
        private static final long serialVersionUID = 1L;
        
        
        /** The image that is shown in the background. */
        private Image image;

        /**
         * Creates a new ImageButton.
         * 
         * @param image     The Image to be shown in the background.
         */
        public ImageButton(Image image)
        {
            this.image = image;
        }
        
        /**
         * Changes the background image.
         * 
         * @param image     Changes the image to this one.
         */
        public void setImage(Image image)
        {
            this.image = image;
        }
        
        /**
         * Paints this Component, should be called automatically.
         * 
         * @param g     This components graphical content.
         */
        @Override
        public void paintComponent(Graphics g)
        {
            if (super.isOpaque()) {
                g.clearRect(0, 0, getWidth(), getHeight());
            }
            g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        }
    }

    /**
     * This is called by the EventHandler after an action has been performed.
     * Here the GUI should update it`s view based on the data parameter.
     * There are three additional sources of information that can be used here:
     *  1. The RobotWatcher, you can ask him for the robots online-status.
     *  2. The last events from the EventHandler, but you should try to avoid
     *     this for less dependencies between actions and GUI (view and control).
     *  3. The actions isLegal method to enable or disable buttons.
     * This method should never have other effects than updating the view!
     * 
     * @param data     The current data (model) the GUI should view.
     */
    @Override
    public void update(AdvancedData data)
    {
        updateClock(data);
        updateHalf(data);
        updateColor(data);
        updateState(data);
        updateGoal(data);
        updateKickoff(data);
        updateNumOfPenalties(data);
        updateRefereeTimeout(data);
        updateOut(data); 
        updateTimeOut(data);
        /*
         * This needs to happen before updatePenaltiesSPL because otherwise
         * the Motion in Set autoselection will not work. On the other hand,
         * the rest updateRobots (at least) must be called after the autoselection
         * because otherwise it is not legal to press a robot.
         */
        for (int i=0; i<robot.length; i++) {
            for (int j=0; j<robot[i].length; j++) {
                // This shows the robot selected for substitution.
                robot[i][j].setSelected(ActionBoard.robot[i][j] == EventHandler.getInstance().lastUIEvent);
            }
        }
        if (Rules.league instanceof SPL) {
            updateGlobalStuck(data);
            updateFreeKick(data);
            updatePenaltiesSPL(data);
        } else if (Rules.league instanceof HL) {
            updatePenaltiesHL(data);
            updateDropBall(data);

        }
        updateRobots(data);
        updateUndo(data);
        repaint();
    }

    /**
     * Always update fonts before drawing.
     *
     * @param g     The graphics context to draw to.
     */
    @Override
    public void paint(Graphics g)
    {
        updateFonts();
        super.paint(g);
    }

    /**
     * Updates the clock.
     * 
     * @param data     The current data (model) the GUI should view.
     */
    private void updateClock(AdvancedData data)
    {
        clock.setText(formatTime(data.getRemainingGameTime(true)));
        Integer secondaryTime = data.getSecondaryTime(true);
        if (secondaryTime != null) {
        	clockSub.setText(formatTime(secondaryTime));
        } else {
            clockSub.setText("");
        }

    	if (data.setPlay == GameControlData.SET_PLAY_PUSHING_FREE_KICK) {
    		freeKick.setText("Pushing Free Kick");
    	} else {
    		freeKick.setText("");
    	}
        
        ImageIcon tmp;
        if (ActionBoard.clock.isClockRunning(data)) {
            tmp = clockImgPause;
        } else {
            tmp = clockImgPlay;
        }
        clockPause.setImage(tmp.getImage());
        clockReset.setVisible(ActionBoard.clockReset.isLegal(data));
        clockPause.setVisible(ActionBoard.clockPause.isLegal(data));
        if (Rules.league.lostTime) {
            incGameClock.setVisible(ActionBoard.incGameClock.isLegal(data));
        }
    }
    
    /**
     * Updates the half.
     * 
     * @param data     The current data (model) the GUI should view.
     */
    private void updateHalf(AdvancedData data)
    {
        for (int i=0; i<2; i++) {
            name[i].setText(Teams.getNames(false)[data.team[i].teamNumber]);
        }
        firstHalf.setEnabled(ActionBoard.firstHalf.isLegal(data));
        secondHalf.setEnabled(ActionBoard.secondHalf.isLegal(data));
        if (Rules.league.overtime) {
            firstHalfOvertime.setEnabled(ActionBoard.firstHalfOvertime.isLegal(data));
            secondHalfOvertime.setEnabled(ActionBoard.secondHalfOvertime.isLegal(data));
        }
        penaltyShoot.setEnabled(ActionBoard.penaltyShoot.isLegal(data));
        firstHalf.setSelected((data.gamePhase == GameControlData.GAME_PHASE_NORMAL)
                            && (data.firstHalf == GameControlData.C_TRUE));
        secondHalf.setSelected((data.gamePhase == GameControlData.GAME_PHASE_NORMAL)
                            && (data.firstHalf != GameControlData.C_TRUE));
        if (Rules.league.overtime) {
           firstHalfOvertime.setSelected((data.gamePhase == GameControlData.GAME_PHASE_OVERTIME)
                            && (data.firstHalf == GameControlData.C_TRUE));
           secondHalfOvertime.setSelected((data.gamePhase == GameControlData.GAME_PHASE_OVERTIME)
                            && (data.firstHalf != GameControlData.C_TRUE)); 
        }
        penaltyShoot.setSelected(data.gamePhase == GameControlData.GAME_PHASE_PENALTYSHOOT || data.previousGamePhase == GameControlData.GAME_PHASE_PENALTYSHOOT);
    }
    
    /**
     * Updates left and right background picture.
     * 
     * @param data     The current data (model) the GUI should view.
     */
    private void updateColor(AdvancedData data)
    {
        for (int i=0; i<2; i++) {
            if(data.team[i].teamColor == GameControlData.TEAM_WHITE) {
                name[i].setForeground(Color.BLACK);
            } else {
                name[i].setForeground(Rules.league.teamColor[data.team[i].teamColor]);
            }
            side[i].setImage(backgroundSide[i][data.team[i].teamColor].getImage());
        }
    }
    
    /**
     * Updates the state.
     * 
     * @param data     The current data (model) the GUI should view.
     */
    private void updateState(AdvancedData data)
    {
        initial.setEnabled(ActionBoard.initial.isLegal(data));
        ready.setEnabled(ActionBoard.ready.isLegal(data));
        set.setEnabled(ActionBoard.set.isLegal(data));
        play.setEnabled(ActionBoard.play.isLegal(data));
        finish.setEnabled(ActionBoard.finish.isLegal(data));
        switch (data.gameState) {
            case GameControlData.STATE_INITIAL:
                initial.setSelected(true);
                break;
            case GameControlData.STATE_READY:
                ready.setSelected(true);
                break;
            case GameControlData.STATE_SET:
                set.setSelected(true);
                break;
            case GameControlData.STATE_PLAYING:
                if (data.setPlay == GameControlData.SET_PLAY_NONE) {
                    play.setSelected(true);
                }
                break;
            case GameControlData.STATE_FINISHED:
                finish.setSelected(true);
                break;
        }
        highlight(finish, (data.gameState != GameControlData.STATE_FINISHED)
                && (data.getRemainingGameTime(true) <= FINISH_HIGHLIGHT_SECONDS)
                && (finish.getBackground() != COLOR_HIGHLIGHT));
    }
    
    /**
     * Updates the goal.
     * 
     * @param data     The current data (model) the GUI should view.
     */
    private void updateGoal(AdvancedData data)
    {
        for (int i=0; i<2; i++) {
            goals[i].setText(""+data.team[i].score);
            goalInc[i].setEnabled(ActionBoard.goalInc[i].isLegal(data));
            goalDec[i].setVisible(ActionBoard.goalDec[i].isLegal(data));
        }
    }
    
    /**
     * Updates the kickoff.
     * 
     * @param data     The current data (model) the GUI should view.
     */
    private void updateKickoff(AdvancedData data)
    {
        if (data.kickingTeam == GameControlData.DROPBALL) {
            kickOff[2].setSelected(true);
        } else {
            kickOff[data.team[0].teamNumber == data.kickingTeam ? 0 : 1].setSelected(true);
        }
        for (int i=0; i<2; i++) {
            kickOff[i].setEnabled(ActionBoard.kickOff[i].isLegal(data));
            if (data.gamePhase != GameControlData.GAME_PHASE_PENALTYSHOOT
                && data.previousGamePhase != GameControlData.GAME_PHASE_PENALTYSHOOT) {
                kickOff[i].setText(KICKING);
            } else {
                kickOff[i].setText(KICKOFF_PENALTY_SHOOTOUT);
            }
        }
    }

    /**
     * Updates the number of penalties / penalty shots.
     *
     * @param data     The current data (model) the GUI should view.
     */
    private void updateNumOfPenalties(AdvancedData data)
    {
        for (int i=0; i<2; i++) {
            if (data.gamePhase != GameControlData.GAME_PHASE_PENALTYSHOOT && data.previousGamePhase != GameControlData.GAME_PHASE_PENALTYSHOOT) {
                penalties[i].setText(PENALTIES+": "+data.penaltyCount[i]);
            } else {
                penalties[i].setText((i == 0 && (data.gameState == GameControlData.STATE_SET
                        || data.gameState == GameControlData.STATE_PLAYING) ? SHOT : SHOTS)+": "+data.team[i].penaltyShot);
            }
        }
    }
    
    /**
     * Updates the robots.
     * 
     * @param data     The current data (model) the GUI should view.
     */
    private void updateRobots(AdvancedData data)
    {
        RobotOnlineStatus[][] onlineStatus = RobotWatcher.updateRobotOnlineStatus();
        for (int i=0; i<robot.length; i++) {
            for (int j=0; j<robot[i].length; j++) {
                if (data.team[i].player[j].penalty != PlayerInfo.PENALTY_NONE) {
                    int seconds = data.getRemainingPenaltyTime(i, j, true);
                    boolean pickup = ((Rules.league instanceof SPL &&
                                data.team[i].player[j].penalty == PlayerInfo.PENALTY_SPL_REQUEST_FOR_PICKUP)
                           || (Rules.league instanceof HL &&
                               ( data.team[i].player[j].penalty == PlayerInfo.PENALTY_HL_PICKUP_OR_INCAPABLE
                              || data.team[i].player[j].penalty == PlayerInfo.PENALTY_HL_SERVICE ))
                            );
                    boolean illegalMotion = Rules.league instanceof SPL
                            && data.team[i].player[j].penalty == PlayerInfo.PENALTY_SPL_ILLEGAL_MOTION_IN_SET;
                    if (seconds == 0) {
                        if (pickup) {
                            robotLabel[i][j].setText(Rules.league.teamColorName[data.team[i].teamColor]+" "+(j+1)+" ("+PEN_PICKUP+")");
                            highlight(robot[i][j], true);
                        } else if (illegalMotion) {
                            robotLabel[i][j].setText(Rules.league.teamColorName[data.team[i].teamColor]+" "+(j+1)+" ("+PEN_MOTION_IN_SET_SHORT+")");
                            highlight(robot[i][j], true);
                        } else if (data.team[i].player[j].penalty == PlayerInfo.PENALTY_SUBSTITUTE) {
                            robotLabel[i][j].setText(Rules.league.teamColorName[data.team[i].teamColor]+" "+(j+1)+" ("+PEN_SUBSTITUTE_SHORT+")");
                            highlight(robot[i][j], false);
                        } else {
                            robotLabel[i][j].setText(Rules.league.teamColorName[data.team[i].teamColor]+" "+(j+1)+": "+formatTime(seconds));
                            highlight(robot[i][j], seconds <= UNPEN_HIGHLIGHT_SECONDS && robot[i][j].getBackground() != COLOR_HIGHLIGHT);
                        }
                    }  else {
                        robotLabel[i][j].setText(Rules.league.teamColorName[data.team[i].teamColor]+" "+(j+1)+": "+formatTime(seconds)+(pickup ? " (P)" : ""));
                        robotTime[i][j].setValue(1000 * seconds / data.getPenaltyDuration(i, j));
                        highlight(robot[i][j], seconds <= UNPEN_HIGHLIGHT_SECONDS && robot[i][j].getBackground() != COLOR_HIGHLIGHT);
                    }
                    robotTime[i][j].setVisible(seconds != 0);
                } else {
                    robotLabel[i][j].setText(Rules.league.teamColorName[data.team[i].teamColor]+" "+(j+1));
                    robotTime[i][j].setVisible(false);
                    highlight(robot[i][j], false);
                }
                
                // Adds an information if player is selected for penalty
                // shootout:
                if (data.gamePhase == AdvancedData.GAME_PHASE_PENALTYSHOOT) {
                    // if the same robot is taker and keeper in a team, show
                    // just the current info:
                    if (data.penaltyShootOutPlayers[i][0] == j
                            && data.penaltyShootOutPlayers[i][0] == data.penaltyShootOutPlayers[i][1]) {
                        boolean isTaker = data.team[i].teamNumber == data.kickingTeam;
                        robotLabel[i][j].setText(robotLabel[i][j].getText() + (isTaker ? " Taker" : " Keeper"));
                        robot[i][j].setBackground(
                                isTaker ? COLOR_PENALTY_SHOOTOUT_TAKER : COLOR_PENALTY_SHOOTOUT_KEEPER);
                        if (IS_OSX) {
                            robot[i][j].setOpaque(true);
                            robot[i][j].setBorderPainted(false);
                        }

                        // if keeper and taker are different, mark both but
                        // set only the background for the relevant player:
                    } else if (data.penaltyShootOutPlayers[i][0] == j || data.penaltyShootOutPlayers[i][1] == j) {
                        boolean isTaker = data.penaltyShootOutPlayers[i][0] == j;
                        robotLabel[i][j].setText(robotLabel[i][j].getText() + (isTaker ? " Taker" : " Keeper"));

                        if (data.team[i].teamNumber == data.kickingTeam == isTaker) {
                            robot[i][j].setBackground(
                                    isTaker ? COLOR_PENALTY_SHOOTOUT_TAKER : COLOR_PENALTY_SHOOTOUT_KEEPER);
                            if (IS_OSX) {
                                robot[i][j].setOpaque(true);
                                robot[i][j].setBorderPainted(false);
                            }
                        }
                    }
                }
                
                robot[i][j].setEnabled(ActionBoard.robot[i][j].isLegal(data));
                
                ImageIcon currentLanIcon;
                if (onlineStatus[i][j] == RobotOnlineStatus.ONLINE) {
                    currentLanIcon = lanOnline;
                } else if (onlineStatus[i][j] == RobotOnlineStatus.HIGH_LATENCY) {
                    currentLanIcon = lanHighLatency;
                } else if (onlineStatus[i][j] == RobotOnlineStatus.OFFLINE) {
                    currentLanIcon = lanOffline;
                } else {
                    currentLanIcon = lanUnknown;
                }
                robotLabel[i][j].setIcon(currentLanIcon);
            }
        }
    }
    
    /**
     * Updates the time-out.
     * 
     * @param data     The current data (model) the GUI should view.
     */
    private void updateTimeOut(AdvancedData data)
    {
        for (int i=0; i<2; i++) {
            if (!data.timeOutActive[i]) {
                timeOut[i].setSelected(false);
                highlight(timeOut[i], false);
            } else {
                boolean shouldHighlight = (data.getRemainingSeconds(data.whenCurrentGameStateBegan, Rules.league.timeOutTime) < TIMEOUT_HIGHLIGHT_SECONDS)
                        && (timeOut[i].getBackground() != COLOR_HIGHLIGHT);
                timeOut[i].setSelected(!IS_OSX || !shouldHighlight);
                highlight(timeOut[i], shouldHighlight);
            }
            timeOut[i].setEnabled(ActionBoard.timeOut[i].isLegal(data));
        }
}
    
    private void updateRefereeTimeout(AdvancedData data) {
        refereeTimeout.setSelected(data.refereeTimeout);
        refereeTimeout.setEnabled(ActionBoard.refereeTimeout.isLegal(data));
    }
    
    /**
     * Updates the global game stuck.
     * 
     * @param data     The current data (model) the GUI should view.
     */
    private void updateGlobalStuck(AdvancedData data)
    {
        for (int i=0; i<2; i++) {
            stuck[i].setEnabled(ActionBoard.stuck[i].isLegal(data));
        }
    }
    
    /**
     * Updates the dropped-ball button.
     * 
     * @param data     The current data (model) the GUI should view.
     */
    private void updateDropBall(AdvancedData data)
    {
        dropBall.setEnabled(ActionBoard.dropBall.isLegal(data));
    }
    
    /**
     * Updates the out.
     * 
     * @param data     The current data (model) the GUI should view.
     */
    private void updateOut(AdvancedData data)
    {
        for (int i=0; i<2; i++) {
            out[i].setEnabled(ActionBoard.out[i].isLegal(data));
        }
    }
    
    /**
     * Updates the free kick.
     * 
     * @param data     The current data (model) the GUI should view.
     */
    private void updateFreeKick(AdvancedData data)
    {
        for (int i=0; i<2; i++) {
            goalFreeKick[i].setSelected(data.setPlay == GameControlData.SET_PLAY_GOAL_FREE_KICK && data.kickingTeam == data.team[i].teamNumber);
            goalFreeKick[i].setEnabled(ActionBoard.goalFreeKick[i].isLegal(data));

            pushingFreeKick[i].setSelected(data.setPlay == GameControlData.SET_PLAY_PUSHING_FREE_KICK && data.kickingTeam == data.team[i].teamNumber);
            pushingFreeKick[i].setEnabled(false);
        }
    }

    /**
     * Updates the SPL penalties.
     * 
     * @param data     The current data (model) the GUI should view.
     */
    private void updatePenaltiesSPL(AdvancedData data)
    {
        pen[0].setEnabled(ActionBoard.pushing.isLegal(data));
        pen[1].setEnabled(ActionBoard.foul.isLegal(data));
        pen[2].setEnabled(ActionBoard.inactive.isLegal(data));
        pen[3].setEnabled(ActionBoard.leaving.isLegal(data));
        pen[4].setEnabled(ActionBoard.motionInSet.isLegal(data));
        pen[5].setEnabled(ActionBoard.defender.isLegal(data));
        pen[6].setEnabled(ActionBoard.ballContact.isLegal(data));
        pen[7].setEnabled(ActionBoard.kickOffGoal.isLegal(data));
        pen[8].setEnabled(ActionBoard.localGameStuck.isLegal(data));
        pen[9].setEnabled(ActionBoard.pickUp.isLegal(data));
        
        GCAction highlightEvent = EventHandler.getInstance().lastUIEvent;
        if (highlightEvent != null && !highlightEvent.isLegal(data)) {
          EventHandler.getInstance().lastUIEvent = null;
        }
        pen[0].setSelected(highlightEvent == ActionBoard.pushing);
        pen[1].setSelected(highlightEvent == ActionBoard.foul);
        pen[2].setSelected(highlightEvent == ActionBoard.inactive);
        pen[3].setSelected(highlightEvent == ActionBoard.leaving);
        pen[5].setSelected(highlightEvent == ActionBoard.defender);
        pen[6].setSelected(highlightEvent == ActionBoard.ballContact);
        pen[7].setSelected(highlightEvent == ActionBoard.kickOffGoal);
        pen[8].setSelected(highlightEvent == ActionBoard.localGameStuck);
        pen[9].setSelected(highlightEvent == ActionBoard.pickUp);

        // Handle quick select for ILLEGAL_MOTION_IN_SET
        if (pen[4].isEnabled()) {
            boolean otherButtonSelected = false;
            for (JToggleButton button : pen) {
                otherButtonSelected |= button != pen[4] && button.isSelected();
            }
            for (JToggleButton[] buttonList : robot) {
                for (JToggleButton button : buttonList) {
                    otherButtonSelected |= button.isSelected();
                }
            }
            for (JToggleButton button : undo) {
                otherButtonSelected |= button.isSelected();
            }
            pen[4].setSelected(!otherButtonSelected);
            if (!otherButtonSelected) {
                EventHandler.getInstance().lastUIEvent = ActionBoard.motionInSet;
            }
        } else {
            pen[4].setSelected(EventHandler.getInstance().lastUIEvent == ActionBoard.motionInSet);
        }
    }
    
    /**
     * Updates the HL penalties.
     * 
     * @param data     The current data (model) the GUI should view.
     */
    private void updatePenaltiesHL(AdvancedData data)
    {
        pen[0].setEnabled(ActionBoard.ballManipulation.isLegal(data));
        pen[1].setEnabled(ActionBoard.pushing.isLegal(data));
        pen[2].setEnabled(ActionBoard.attack.isLegal(data));
        pen[3].setEnabled(ActionBoard.defense.isLegal(data));
        pen[4].setEnabled(ActionBoard.pickUpHL.isLegal(data));
        pen[5].setEnabled(ActionBoard.serviceHL.isLegal(data));

        GCAction hightlightEvent = EventHandler.getInstance().lastUIEvent;
        pen[0].setSelected(hightlightEvent == ActionBoard.ballManipulation);
        pen[1].setSelected(hightlightEvent == ActionBoard.pushing);
        pen[2].setSelected(hightlightEvent == ActionBoard.attack);
        pen[3].setSelected(hightlightEvent == ActionBoard.defense);
        pen[4].setSelected(hightlightEvent == ActionBoard.pickUpHL);
        pen[5].setSelected(hightlightEvent == ActionBoard.serviceHL);
    }
    
    /**
     * Updates the timeline/undo.
     * 
     * @param data     The current data (model) the GUI should view.
     */
    private void updateUndo(AdvancedData data)
    {
        GCAction highlightEvent = EventHandler.getInstance().lastUIEvent;
        String[] undos = Log.getLast(ActionBoard.MAX_NUM_UNDOS_AT_ONCE);
        boolean undoFromHere = false;
        for (int i=undo.length - 1; i >= 0; i--) {
            undo[i].setVisible(!undos[i].equals(""));
            undo[i].setEnabled(!undos[i].contains(" vs "));
            if ((highlightEvent == ActionBoard.undo[i+1]) && (!ActionBoard.undo[i+1].executed)) {
                undoFromHere = true;
            }
            if (undoFromHere) {
                undo[i].setText("<html><center>Undo '"+undos[i] + "\'?");
                undo[i].setSelected(true);
            } else {
                undo[i].setText("<html><center>"+undos[i]);
                undo[i].setSelected(false);
            }
        }
        cancelUndo.setVisible(undoFromHere);
    }
    
    private void updateFonts()
    {
        double size = Math.min((getWidth()/(double)WINDOW_WIDTH), (getHeight()/(double)WINDOW_HEIGHT));
        
        if (size == lastSize) {
            return;
        }
        lastSize = size;
        
        titleFont = new Font(STANDARD_FONT, Font.PLAIN, (int)(TITLE_FONT_SIZE*(size)));
        standardFont = new Font(STANDARD_FONT, Font.PLAIN, (int)(STANDARD_FONT_SIZE*(size)));
        goalsFont = new Font(STANDARD_FONT, Font.PLAIN, (int)(GOALS_FONT_SIZE*(size)));
        timeFont = new Font(STANDARD_FONT, Font.PLAIN, (int)(TIME_FONT_SIZE*(size)));
        timeSubFont = new Font(STANDARD_FONT, Font.PLAIN, (int)(TIME_SUB_FONT_SIZE*(size)));
        timeoutFont = new Font(STANDARD_FONT, Font.PLAIN, (int)(TIMEOUT_FONT_SIZE*(size)));
        stateFont = new Font(STANDARD_FONT, Font.PLAIN, (int)(STATE_FONT_SIZE*(size)));

        for (int i=0; i<2; i++) {
            name[i].setFont(titleFont);
            goalInc[i].setFont(timeSubFont);
            goalDec[i].setFont(timeSubFont);
            kickOff[i].setFont(standardFont);
            goals[i].setFont(goalsFont);
            penalties[i].setFont(standardFont);
            for (int j=0; j<robot[i].length; j++) {
                robotLabel[i][j].setFont(titleFont);
            }
            timeOut[i].setFont(standardFont);
            out[i].setFont(standardFont);
            if (Rules.league instanceof SPL) {
                stuck[i].setFont(timeoutFont);
                goalFreeKick[i].setFont(standardFont);
                pushingFreeKick[i].setFont(standardFont);
            }
        }
        clock.setFont(timeFont);
        clockSub.setFont(timeSubFont);
        freeKick.setFont(standardFont);
        
        firstHalf.setFont(timeoutFont);
        secondHalf.setFont(timeoutFont);
        if (Rules.league.overtime) {
            firstHalfOvertime.setFont(timeoutFont);
            secondHalfOvertime.setFont(timeoutFont);
        }
        penaltyShoot.setFont(timeoutFont);
        if (Rules.league.isRefereeTimeoutAvailable) {
            refereeTimeout.setFont(timeoutFont);
        }

        initial.setFont(stateFont);
        ready.setFont(stateFont);
        set.setFont(stateFont);
        play.setFont(stateFont);
        finish.setFont(stateFont);
        for (int i=0; i<pen.length; i++) {
            pen[i].setFont(standardFont);
        }
        if (dropBall != null) {
            dropBall.setFont(standardFont);
        }
        for (int i=0; i<undo.length; i++) {
            undo[i].setFont(timeoutFont);
        }
        cancelUndo.setFont(standardFont);
    }
    
    /**
     * Set the given button highlighted or normal.
     * 
     * @param button        The button to highlight.
     * @param highlight     If the button should be highlighted.
     */
    private void highlight(AbstractButton button, boolean highlight)
    {
        button.setBackground(highlight ? COLOR_HIGHLIGHT : COLOR_STANDARD);
        if (IS_OSX) {
            button.setOpaque(highlight);
            button.setBorderPainted(!highlight);
        }
    }

    private String formatTime(int seconds) {
        int displaySeconds = Math.abs(seconds) % 60;
        int displayMinutes = Math.abs(seconds) / 60;
        return (seconds < 0 ? "-" : "") + String.format("%02d:%02d", displayMinutes, displaySeconds);
    }
}
