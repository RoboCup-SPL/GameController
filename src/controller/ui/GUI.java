package controller.ui;

import common.TotalScaleLayout;
import controller.EventHandler;
import common.Log;
import controller.action.ActionBoard;
import controller.action.GCAction;
import controller.action.ui.penalty.Pushing;
import controller.net.RobotOnlineStatus;
import controller.net.RobotWatcher;
import data.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;


/**
 * @author: Michel Bartsch
 * 
 * This is the main GUI.
 * In this class you will find the whole graphical output and the bindings
 * of buttons to their actions, nothing less and nothing more.
 */
public class GUI extends JFrame implements GCGUI
{
    private static final long serialVersionUID = 1L;
    
    /**
     * Some constants defining this GUI`s appearance as their names say.
     * Feel free to change them and see what happens.
     */
    private static final boolean IS_OSX = System.getProperty("os.name").contains("OS X");
    private static final int WINDOW_WIDTH = 1024;
    private static final int WINDOW_HEIGHT = 768;
    private static final int STANDARD_FONT_SIZE = 18;
    private static final int TITLE_FONT_SIZE = 24;
    private static final String STANDARD_FONT = "Helvetica";
    private static final int GOALS_FONT_SIZE = 60;
    private static final int TIME_FONT_SIZE = 50;
    private static final int TIME_SUB_FONT_SIZE = 40;
    private static final int TIMEOUT_FONT_SIZE = 14;
    private static final int STATE_FONT_SIZE = 12;
    private static final String WINDOW_TITLE = "GameController2";
    private static final String[][] BACKGROUND_SIDE = {{"config/icons/robot_left_blue.png",
                                                        "config/icons/robot_left_red.png"},
                                                       {"config/icons/robot_right_blue.png",
                                                        "config/icons/robot_right_red.png"}};
    private static final String BACKGROUND_MID = "config/icons/field.png";
    private static final String BACKGROUND_CLOCK = "config/icons/time_ground.png";
    private static final String KICKOFF = "Kickoff";
    private static final String KICKOFF_PENALTY_SHOOTOUT = "P.-taker";
    private static final String PUSHES = "Pushes";
    private static final String SHOOTS = "Shoot";
    private static final String EJECTED = "Ejected";
    private static final String ONLINE = "config/icons/wlan_status_green.png";
    private static final String OFFLINE = "config/icons/wlan_status_red.png";
    private static final String HIGH_LATENCY = "config/icons/wlan_status_yellow.png";
    private static final String UNKNOWN_ONLINE_STATUS = "config/icons/wlan_status_grey.png";
    private static final String TIMEOUT = "Timeout";
    private static final String STUCK = "Global<br/>Game<br/>Stuck";
    private static final String KICKOFF_GOAL = "Kickoff Goal";
    private static final String OUT = "Out";
    private static final String STATE_INITIAL = "Initial";
    private static final String STATE_READY = "Ready";
    private static final String STATE_SET = "Set";
    private static final String STATE_PLAY = "Play";
    private static final String STATE_FINISH = "Finish";
    private static final String CLOCK_RESET = "config/icons/reset.png";
    private static final String CLOCK_PAUSE = "config/icons/pause.png";
    private static final String CLOCK_PLAY = "config/icons/play.png";
    private static final String FIRST_HALF = "First-Half";
    private static final String SECOND_HALF = "Second-Half";
    private static final String PENALTY_SHOOT = "Penalty-Shoot";
    private static final String PEN_PUSHING = "Pushing";
    private static final String PEN_LEAVING = "Leaving the Field";
    private static final String PEN_FALLEN = "Fallen Robot";
    private static final String PEN_INACTIVE = "Inactive /<br>Local Game Stuck";
    private static final String PEN_DEFENDER = "Illegal Defender";
    private static final String PEN_HOLDING = "Ball Holding";
    private static final String PEN_HANDS = "Hands";
    private static final String PEN_PICKUP = "Pick-Up";
    private static final String PEN_DEFENSE = "Illegal Defense";
    private static final String PEN_ATTACK = "Illegal Attack";
    private static final String CANCEL = "Cancel";
    private static final String BACKGROUND_BOTTOM = "config/icons/timeline_ground.png";
    private static final Color COLOR_HIGHLIGHT = Color.YELLOW;
    private static final Color COLOR_STANDARD = (new JButton()).getBackground();
    private static final int UNPEN_HIGHLIGHT_SECONDS = 7;
    private static final int TIMEOUT_HIGHLIGHT_SECONDS = 10;
    private static final int FINISH_HIGHLIGHT_SECONDS = 10;
    private static final int KICKOFF_BLOCKED_HIGHLIGHT_SECONDS = 3;
  
    /** Some attributes used in the GUI components. */
    private Font standardFont;
    private Font titleFont;
    private Font goalsFont;
    private Font timeFont;
    private Font timeSubFont;
    private Font timeoutFont;
    private Font stateFont;
    private SimpleDateFormat clockFormat = new SimpleDateFormat("mm:ss");
    private ImageIcon clockImgReset;
    private ImageIcon clockImgPlay;
    private ImageIcon clockImgPause;
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
    private JLabel[] pushes;
    private JPanel[] robots;
    private JButton[][] robot;
    private JLabel[][] robotLabel;
    private ImageIcon[][] lanIcon;
    private JProgressBar[][] robotTime;
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
    private ImageButton clockPause;
    private JToggleButton firstHalf;
    private JToggleButton secondHalf;
    private JToggleButton penaltyShoot;
    private ButtonGroup halfGroup;
    private JToggleButton[] pen;
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
     * @param data      The startig data.
     */
    public GUI(boolean fullscreen, AdvancedData data)
    {
        super(WINDOW_TITLE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setResizable(true);
        Dimension desktop = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((desktop.width-WINDOW_WIDTH)/2, (desktop.height-WINDOW_HEIGHT)/2);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        clockImgReset = new ImageIcon(CLOCK_RESET);
        clockImgPlay = new ImageIcon(CLOCK_PLAY);
        clockImgPause = new ImageIcon(CLOCK_PAUSE);
        lanOnline = new ImageIcon(ONLINE);
        lanHighLatency = new ImageIcon(HIGH_LATENCY);
        lanOffline = new ImageIcon(OFFLINE);
        lanUnknown = new ImageIcon(UNKNOWN_ONLINE_STATUS);
        
        backgroundSide = new ImageIcon[2][2];
        for(int i=0; i<BACKGROUND_SIDE.length; i++) {
            for(int j=0; j<BACKGROUND_SIDE[i].length; j++) {
                backgroundSide[i][j] = new ImageIcon(BACKGROUND_SIDE[i][j]);
            }
        }
        
        
        //Components
        side = new ImagePanel[2];
        for(int i=0; i<2; i++) {
            side[i] = new ImagePanel(backgroundSide[i][i].getImage());
            side[i].setOpaque(true);
        }
        mid = new ImagePanel(new ImageIcon(BACKGROUND_MID).getImage());
        bottom = new ImagePanel(new ImageIcon(BACKGROUND_BOTTOM).getImage());
        
        //--side--
        //  score
        name = new JLabel[2];
        goalDec = new JButton[2];
        goalInc = new JButton[2];
        goals = new JLabel[2];
        kickOff = new JRadioButton[2];
        kickOffGroup = new ButtonGroup();
        pushes = new JLabel[2];
        for(int i=0; i<2; i++) {
            name[i] = new JLabel(Teams.getNames(false)[data.team[i].teamNumber]);
            name[i].setHorizontalAlignment(JLabel.CENTER);
            name[i].setForeground(Rules.league.teamColor[data.team[i].teamColor]);
            goalInc[i] = new JButton("+");
            goalDec[i] = new JButton("-");
            kickOff[i] = new JRadioButton(KICKOFF);
            kickOff[i].setOpaque(false);
            kickOff[i].setHorizontalAlignment(JLabel.CENTER);
            kickOffGroup.add(kickOff[i]);
            goals[i] = new JLabel("0");
            goals[i].setHorizontalAlignment(JLabel.CENTER);
            pushes[i] = new JLabel("0");
            pushes[i].setHorizontalAlignment(JLabel.CENTER);
        }
        //  robots
        robots = new JPanel[2];
        robot = new JButton[2][Rules.league.teamSize];
        robotLabel = new JLabel[2][Rules.league.teamSize];
        lanIcon = new ImageIcon[2][Rules.league.teamSize];
        robotTime = new JProgressBar[2][Rules.league.teamSize];
        for(int i=0; i<2; i++) {
            robots[i] = new JPanel();
            robots[i].setLayout(new GridLayout(robot[i].length, 1, 0, 10));
            robots[i].setOpaque(false);
            for(int j=0; j<robot[i].length; j++) {
                robot[i][j] = new JButton();
                robotLabel[i][j] = new JLabel();
                robotLabel[i][j].setHorizontalAlignment(JLabel.CENTER);
                lanIcon[i][j] = lanUnknown;
                robotLabel[i][j].setIcon(lanIcon[i][j]);
                robotTime[i][j] = new JProgressBar();
                robotTime[i][j].setVisible(false);
                TotalScaleLayout robotLayout = new TotalScaleLayout(robot[i][j]);
                robot[i][j].setLayout(robotLayout);
                robotLayout.add(.1, .1, .8, .5, robotLabel[i][j]);
                robotLayout.add(.1, .7, .8, .2, robotTime[i][j]);
                robots[i].add(robot[i][j]);
            }
        }
        //  team
        timeOut = new JToggleButton[2];
        stuck = new JButton[2];
        out = new JButton[2];
        for(int i=0; i<2; i++) {
            timeOut[i] = new JToggleButton();
            stuck[i] = new JButton();
            out[i] = new JButton(OUT);
        }
        
        //--mid--
        //  time
        clockReset = new ImageButton(clockImgReset.getImage());
        clockReset.setOpaque(false);
        clockReset.setBorder(null);
        clockContainer = new ImagePanel(new ImageIcon(BACKGROUND_CLOCK).getImage());
        clockContainer.setOpaque(false);
        clock = new JLabel("10:00");
        clock.setForeground(Color.WHITE);
        clock.setHorizontalAlignment(JLabel.CENTER);
        clockPause = new ImageButton(clockImgReset.getImage());
        clockPause.setOpaque(false);
        clockPause.setBorder(null);
        clockSub = new JLabel("0:00");
        clockSub.setHorizontalAlignment(JLabel.CENTER);
        firstHalf = new JToggleButton(FIRST_HALF);
        firstHalf.setSelected(true);
        secondHalf = new JToggleButton(SECOND_HALF);
        penaltyShoot = new JToggleButton(PENALTY_SHOOT);
        halfGroup = new ButtonGroup();
        halfGroup.add(firstHalf);
        halfGroup.add(secondHalf);
        halfGroup.add(penaltyShoot);
        //  state
        initial = new JToggleButton(STATE_INITIAL);
        initial.setSelected(true);
        ready = new JToggleButton(STATE_READY);
        set = new JToggleButton(STATE_SET);
        play = new JToggleButton(STATE_PLAY);
        finish = new JToggleButton(STATE_FINISH);
        stateGroup = new ButtonGroup();
        stateGroup.add(initial);
        stateGroup.add(ready);
        stateGroup.add(set);
        stateGroup.add(play);
        stateGroup.add(finish);
        //  penalties
        if(Rules.league instanceof SPL) {
            pen = new JToggleButton[8];
            pen[0] = new JToggleButton(PEN_PUSHING);
            pen[1] = new JToggleButton(PEN_LEAVING);
            pen[2] = new JToggleButton(PEN_FALLEN);
            pen[3] = new JToggleButton(PEN_INACTIVE);
            pen[4] = new JToggleButton(PEN_DEFENDER);
            pen[5] = new JToggleButton(PEN_HOLDING);
            pen[6] = new JToggleButton(PEN_HANDS);
            pen[7] = new JToggleButton(PEN_PICKUP);
        } else if(Rules.league instanceof Humanoid) {
            pen = new JToggleButton[5];
            pen[0] = new JToggleButton(PEN_HOLDING);
            pen[1] = new JToggleButton(PEN_PUSHING);
            pen[2] = new JToggleButton(PEN_ATTACK);
            pen[3] = new JToggleButton(PEN_DEFENSE);
            pen[4] = new JToggleButton(PEN_PICKUP);
        }
        
        //--bottom--
        //  log
        log = new JPanel();
        log.setOpaque(false);
        log.setLayout(new GridLayout(1, ActionBoard.MAX_NUM_UNDOS_AT_ONCE-1, 10, 0));
        undo = new JToggleButton[ActionBoard.MAX_NUM_UNDOS_AT_ONCE-1];
        for(int i=undo.length-1; i>=0; i--) {
            undo[i] = new JToggleButton();
            undo[i].setVisible(false);
            log.add(undo[i]);
        }
        cancelUndo = new JButton(CANCEL);
        cancelUndo.setVisible(false);
      
        //--layout--
        TotalScaleLayout layout = new TotalScaleLayout(this);
        setLayout(layout);
        
        layout.add(0, 0, .3, .04, name[0]);
        layout.add(.7, 0, .3, .04, name[1]);
        layout.add(.01, .05, .08, .07, goalInc[0]);
        layout.add(.91, .05, .08, .07, goalInc[1]);
        layout.add(.01, .13, .08, .06, goalDec[0]);
        layout.add(.91, .13, .08, .06, goalDec[1]);
        layout.add(.17, .05, .12, .04, kickOff[0]);
        layout.add(.71, .05, .12, .04, kickOff[1]);
        layout.add(.21, .09, .08, .07, goals[0]);
        layout.add(.71, .09, .08, .07, goals[1]);
        layout.add(.21, .16, .08, .04, pushes[0]);
        layout.add(.71, .16, .08, .04, pushes[1]);
        layout.add(.01, .21, .28, .55, robots[0]);
        layout.add(.71, .21, .28, .55, robots[1]);
        layout.add(.01, .77, .09, .09, timeOut[0]);
        layout.add(.9, .77, .09, .09, timeOut[1]);
        layout.add(.11, .77, .08, .09, stuck[0]);
        layout.add(.81, .77, .08, .09, stuck[1]);
        layout.add(.20, .77, .09, .09, out[0]);
        layout.add(.71, .77, .09, .09, out[1]);
        layout.add(.31, .0, .08, .11, clockReset);
        layout.add(.4, .012, .2, .10, clock);
        layout.add(.4, .0, .2, .11, clockContainer);
        layout.add(.61, .0, .08, .11, clockPause);
        layout.add(.4, .11, .2, .07, clockSub);
        layout.add(.31, .19, .12, .06, firstHalf);
        layout.add(.44, .19, .12, .06, secondHalf);
        layout.add(.57, .19, .12, .06, penaltyShoot);
        layout.add(.31, .26, .07, .08, initial);
        layout.add(.3875, .26, .07, .08, ready);
        layout.add(.465, .26, .07, .08, set);
        layout.add(.5425, .26, .07, .08, play);
        layout.add(.62, .26, .07, .08, finish);
        if(pen.length > 0) {
            layout.add(.31, .37, .185, .11, pen[0]);
        }
        if(pen.length > 1) {
            layout.add(.505, .37, .185, .11, pen[1]);
        }
        if(pen.length > 2) {
            layout.add(.31, .49, .185, .11, pen[2]);
        }
        if(pen.length > 3) {
            layout.add(.505, .49, .185, .11, pen[3]);
        }
        if(pen.length > 4) {
            layout.add(.31, .61, .185, .11, pen[4]);
        }
        if(pen.length > 5) {
            layout.add(.505, .61, .185, .11, pen[5]);
        }
        if(pen.length > 6) {
            layout.add(.31, .73, .185, .11, pen[6]);
        }
        if(pen.length > 7) {
            layout.add(.505, .73, .185, .11, pen[7]);
        }
        layout.add(.08, .88, .84, .11, log);
        layout.add(.925, .88, .07, .11, cancelUndo);
        layout.add(0, 0, .3, .87, side[0]);
        layout.add(.3, 0, .4, .87, mid);
        layout.add(.7, 0, .3, .87, side[1]);
        layout.add(0, .87, 1, .132, bottom);
        
        //--listener--
        for(int i=0; i<2; i++) {
            goalDec[i].addActionListener(ActionBoard.goalDec[i]);
            goalInc[i].addActionListener(ActionBoard.goalInc[i]);
            kickOff[i].addActionListener(ActionBoard.kickOff[i]);
            for(int j=0; j<robot[i].length; j++) {
                robot[i][j].addActionListener(ActionBoard.robot[i][j]);
            }
            timeOut[i].addActionListener(ActionBoard.timeOut[i]);
            stuck[i].addActionListener(ActionBoard.stuck[i]);
            out[i].addActionListener(ActionBoard.out[i]);
        }
        initial.addActionListener(ActionBoard.initial);
        ready.addActionListener(ActionBoard.ready);
        set.addActionListener(ActionBoard.set);
        play.addActionListener(ActionBoard.play);
        finish.addActionListener(ActionBoard.finish);
        clockReset.addActionListener(ActionBoard.clockReset);
        clockPause.addActionListener(ActionBoard.clockPause);
        firstHalf.addActionListener(ActionBoard.firstHalf);
        secondHalf.addActionListener(ActionBoard.secondHalf);
        penaltyShoot.addActionListener(ActionBoard.penaltyShoot);
        if(Rules.league instanceof SPL) {
            pen[0].addActionListener(ActionBoard.pushing);
            pen[1].addActionListener(ActionBoard.leaving);
            pen[2].addActionListener(ActionBoard.fallen);
            pen[3].addActionListener(ActionBoard.inactive);
            pen[4].addActionListener(ActionBoard.defender);
            pen[5].addActionListener(ActionBoard.holding);
            pen[6].addActionListener(ActionBoard.hands);
            pen[7].addActionListener(ActionBoard.pickUp);
        } else if(Rules.league instanceof Humanoid) {
            pen[0].addActionListener(ActionBoard.hands);
            pen[1].addActionListener(ActionBoard.pushing);
            pen[2].addActionListener(ActionBoard.attack);
            pen[3].addActionListener(ActionBoard.defense);
            pen[4].addActionListener(ActionBoard.pickUp);
        }
        for(int i=0; i<undo.length; i++) {
            undo[i].addActionListener(ActionBoard.undo[i+1]);
        }
        cancelUndo.addActionListener(ActionBoard.cancelUndo);
      
        //fullscreen
        if(fullscreen) {
            setUndecorated(true);
            GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            devices[0].setFullScreenWindow(this);
        }
        
        updateFonts();
        
        setVisible(true);
    }
    
    /**
     * @author: Michel Bartsch
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
            if(super.isOpaque()) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        }
    }
    
    /**
     * @author: Michel Bartsch
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
            if(super.isOpaque()) {
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
        updateRobots(data);
        updatePushes(data);
        updateTimeOut(data);
        updateGlobalStuck(data);
        updateOut(data);
        if(Rules.league instanceof SPL) {
            updatePenaltiesSPL(data);
        } else if(Rules.league instanceof Humanoid) {
            updatePenaltiesHL(data);
        }
        updateUndo(data);
        updateFonts();
        repaint();
    }
    
    /**
     * Updates the clock.
     * 
     * @param data     The current data (model) the GUI should view.
     */
    private void updateClock(AdvancedData data)
    {
        if(data.extraTime == 0) {
            clock.setText(clockFormat.format(new Date(data.secsRemaining*1000+999)));
        } else {
            clock.setText("-" + clockFormat.format(new Date(data.extraTime*1000)));
        }

        if(data.gameState == GameControlData.STATE_READY) {
            clockSub.setText(clockFormat.format(new Date(data.remainingReady+999)));
            clockSub.setForeground(Color.BLACK);
        } else if( ( (data.gameState == GameControlData.STATE_FINISHED)
                  || (data.gameState == GameControlData.STATE_INITIAL) )
                && (data.remainingPaused > 0) ) {
            clockSub.setText(clockFormat.format(new Date(data.remainingPaused+999)));
            clockSub.setForeground(Color.BLACK);
        } else if( (data.gameState == GameControlData.STATE_PLAYING)
                && (data.remainingKickoffBlocked >= -KICKOFF_BLOCKED_HIGHLIGHT_SECONDS*1000) ) {
            clockSub.setText(clockFormat.format(new Date(Math.max(0, data.remainingKickoffBlocked+999))));
            clockSub.setForeground(data.remainingKickoffBlocked < 0
                    && clockSub.getForeground() != COLOR_HIGHLIGHT ? COLOR_HIGHLIGHT : Color.BLACK);
        } else {
            clockSub.setText("");
            clockSub.setForeground(Color.BLACK);
        }
        ImageIcon tmp;
        if(ActionBoard.clock.isClockRunning(data)) {
            tmp = clockImgPause;
        } else {
            tmp = clockImgPlay;
        }
        clockPause.setImage(tmp.getImage());
        clockReset.setVisible(ActionBoard.clockReset.isLegal(data));
        clockPause.setVisible(ActionBoard.clockPause.isLegal(data));
    }
    
    /**
     * Updates the half.
     * 
     * @param data     The current data (model) the GUI should view.
     */
    private void updateHalf(AdvancedData data)
    {
        for(int i=0; i<2; i++) {
            name[i].setText(Teams.getNames(false)[data.team[i].teamNumber]);
        }
        firstHalf.setEnabled(ActionBoard.firstHalf.isLegal(data));
        secondHalf.setEnabled(ActionBoard.secondHalf.isLegal(data));
        penaltyShoot.setEnabled(ActionBoard.penaltyShoot.isLegal(data));
        firstHalf.setSelected(data.firstHalf == GameControlData.C_TRUE);
        secondHalf.setSelected( (data.firstHalf != GameControlData.C_TRUE)
                             && (data.secGameState != GameControlData.STATE2_PENALTYSHOOT) );
        penaltyShoot.setSelected(data.secGameState == GameControlData.STATE2_PENALTYSHOOT);
    }
    
    /**
     * Updates left and right background picture.
     * 
     * @param data     The current data (model) the GUI should view.
     */
    private void updateColor(AdvancedData data)
    {
        for(int i=0; i<2; i++) {
            name[i].setForeground(Rules.league.teamColor[data.team[i].teamColor]);
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
        switch(data.gameState) {
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
                play.setSelected(true);
                break;
            case GameControlData.STATE_FINISHED:
                finish.setSelected(true);
                break;
        }
        highlight(finish, (data.secsRemaining <= FINISH_HIGHLIGHT_SECONDS)
                && (finish.getBackground() != COLOR_HIGHLIGHT) );
    }
    
    /**
     * Updates the goal.
     * 
     * @param data     The current data (model) the GUI should view.
     */
    private void updateGoal(AdvancedData data)
    {
        for(int i=0; i<2; i++) {
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
        kickOff[data.team[0].teamColor == data.kickOffTeam ? 0 : 1].setSelected(true);
        for(int i=0; i<2; i++) {
            kickOff[i].setEnabled(ActionBoard.kickOff[i].isLegal(data));
            if(data.secGameState != GameControlData.STATE2_PENALTYSHOOT) {
                kickOff[i].setText(KICKOFF);
            } else {
                kickOff[i].setText(KICKOFF_PENALTY_SHOOTOUT);
            }
        }
    }
    
    /**
     * Updates the pushes.
     * 
     * @param data     The current data (model) the GUI should view.
     */
    private void updatePushes(AdvancedData data)
    {
        for(int i=0; i<2; i++) {
            if(data.secGameState != GameControlData.STATE2_PENALTYSHOOT)
            {
                pushes[i].setText(PUSHES+": "+data.pushes[i]);
            } else {
                pushes[i].setText(SHOOTS+": "+data.penaltyShoot[i]);
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
        for(int i=0; i<robot.length; i++) {
            for(int j=0; j<robot[i].length; j++) {            
                if(data.team[i].player[j].penalty != PlayerInfo.PENALTY_NONE) {
                    if(data.team[i].player[j].secsTillUnpenalised != Pushing.BANN_TIME) {
                        if(data.team[i].player[j].secsTillUnpenalised == 0 
                                && data.team[i].player[j].penalty == PlayerInfo.PENALTY_SPL_REQUEST_FOR_PICKUP) {
                            robotLabel[i][j].setText(Rules.league.teamColorName[i]+" "+(j+1)+" ("+PEN_PICKUP+")");
                        } else {
                            robotLabel[i][j].setText(Rules.league.teamColorName[i]+" "+(j+1)+": "+clockFormat.format(new Date(data.team[i].player[j].secsTillUnpenalised*1000)));
                        }
                        robotTime[i][j].setValue(100*data.team[i].player[j].secsTillUnpenalised/Rules.league.penaltyStandardTime);
                        robotTime[i][j].setVisible(true);
                    } else {
                        robotLabel[i][j].setText(EJECTED);
                        robotTime[i][j].setVisible(false);
                    }
                } else {
                    robotLabel[i][j].setText(Rules.league.teamColorName[i]+" "+(j+1));
                    robotTime[i][j].setVisible(false);
                }
                robot[i][j].setEnabled(ActionBoard.robot[i][j].isLegal(data));
                highlight(robot[i][j],
                        (data.team[i].player[j].penalty != PlayerInfo.PENALTY_NONE)
                        && (data.team[i].player[j].secsTillUnpenalised <= UNPEN_HIGHLIGHT_SECONDS)
                        && (robot[i][j].getBackground() != COLOR_HIGHLIGHT
                         || data.team[i].player[j].penalty == PlayerInfo.PENALTY_SPL_REQUEST_FOR_PICKUP
                          && data.team[i].player[j].secsTillUnpenalised == 0) );
                ImageIcon currentLanIcon;
                if(onlineStatus[i][j] == RobotOnlineStatus.ONLINE) {
                    currentLanIcon = lanOnline;
                } else if(onlineStatus[i][j] == RobotOnlineStatus.HIGH_LATENCY) {
                    currentLanIcon = lanHighLatency;
                } else if(onlineStatus[i][j] == RobotOnlineStatus.OFFLINE) {
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
        for(int i=0; i<2; i++) {
            if(!data.timeOutActive[i]) {
                timeOut[i].setText(TIMEOUT);
                timeOut[i].setSelected(false);
                highlight(timeOut[i], false);
            } else {
                timeOut[i].setText(clockFormat.format(new Date(data.timeOut[i])));
                boolean shouldHighlight = (data.timeOut[i]/1000 < TIMEOUT_HIGHLIGHT_SECONDS)
                        && (timeOut[i].getBackground() != COLOR_HIGHLIGHT);
                timeOut[i].setSelected(!IS_OSX || !shouldHighlight);
                highlight(timeOut[i], shouldHighlight);
            }
            timeOut[i].setEnabled(ActionBoard.timeOut[i].isLegal(data));
        }
    }
    
    /**
     * Updates the global game stuck.
     * 
     * @param data     The current data (model) the GUI should view.
     */
    private void updateGlobalStuck(AdvancedData data)
    {
        for(int i=0; i<2; i++) {
            if(data.gameState == GameControlData.STATE_PLAYING
                    && -1*(data.remainingKickoffBlocked - Rules.league.kickoffTime*1000) < Rules.league.minDurationBeforeStuck*1000)
            {
                if(data.kickOffTeam == data.team[i].teamColor)
                {
                    stuck[i].setEnabled(true);
                    stuck[i].setText("<html><center>"
                        +"<font color=#000000>"
                        +KICKOFF_GOAL);
                } else {
                    stuck[i].setEnabled(false);
                    stuck[i].setText("<html><center>"
                        +"<font color=#808080>"
                        +STUCK);
                }
            } else {
                stuck[i].setEnabled(ActionBoard.stuck[i].isLegal(data));
                stuck[i].setText("<html><center>"
                        +(ActionBoard.stuck[i].isLegal(data) ? "<font color=#000000>" : "<font color=#808080>")
                        +STUCK);
            }
        }
    }
    
    /**
     * Updates the out.
     * 
     * @param data     The current data (model) the GUI should view.
     */
    private void updateOut(AdvancedData data)
    {
        for(int i=0; i<2; i++) {
            out[i].setEnabled(ActionBoard.out[i].isLegal(data));
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
        pen[1].setEnabled(ActionBoard.leaving.isLegal(data));
        pen[2].setEnabled(ActionBoard.fallen.isLegal(data));
        pen[3].setEnabled(ActionBoard.inactive.isLegal(data));
        pen[3].setText("<html><center>"
                +(ActionBoard.inactive.isLegal(data) ? "<font color=#000000>" : "<font color=#808080>")
                +PEN_INACTIVE);
        pen[4].setEnabled(ActionBoard.defender.isLegal(data));
        pen[5].setEnabled(ActionBoard.holding.isLegal(data));
        pen[6].setEnabled(ActionBoard.hands.isLegal(data));
        pen[7].setEnabled(ActionBoard.pickUp.isLegal(data));
        
        GCAction hightlightEvent = EventHandler.getInstance().lastUIEvent;
        pen[0].setSelected(hightlightEvent == ActionBoard.pushing);
        pen[1].setSelected(hightlightEvent == ActionBoard.leaving);
        pen[2].setSelected(hightlightEvent == ActionBoard.fallen);
        pen[3].setSelected(hightlightEvent == ActionBoard.inactive);
        pen[4].setSelected(hightlightEvent == ActionBoard.defender);
        pen[5].setSelected(hightlightEvent == ActionBoard.holding);
        pen[6].setSelected(hightlightEvent == ActionBoard.hands);
        pen[7].setSelected(hightlightEvent == ActionBoard.pickUp);
    }
    
        /**
     * Updates the HL penalties.
     * 
     * @param data     The current data (model) the GUI should view.
     */
    private void updatePenaltiesHL(AdvancedData data)
    {
        pen[0].setEnabled(ActionBoard.hands.isLegal(data));
        pen[1].setEnabled(ActionBoard.pushing.isLegal(data));
        pen[2].setEnabled(ActionBoard.attack.isLegal(data));
        pen[3].setEnabled(ActionBoard.defense.isLegal(data));
        pen[4].setEnabled(ActionBoard.pickUp.isLegal(data));
        
        GCAction hightlightEvent = EventHandler.getInstance().lastUIEvent;
        pen[0].setSelected(hightlightEvent == ActionBoard.hands);
        pen[1].setSelected(hightlightEvent == ActionBoard.pushing);
        pen[2].setSelected(hightlightEvent == ActionBoard.attack);
        pen[3].setSelected(hightlightEvent == ActionBoard.defense);
        pen[4].setSelected(hightlightEvent == ActionBoard.pickUp);
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
        for(int i=undo.length - 1; i >= 0; i--) {
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
        
        titleFont = new Font(STANDARD_FONT, Font.PLAIN, (int)(TITLE_FONT_SIZE*(size)));
        standardFont = new Font(STANDARD_FONT, Font.PLAIN, (int)(STANDARD_FONT_SIZE*(size)));
        goalsFont = new Font(STANDARD_FONT, Font.PLAIN, (int)(GOALS_FONT_SIZE*(size)));
        timeFont = new Font(STANDARD_FONT, Font.PLAIN, (int)(TIME_FONT_SIZE*(size)));
        timeSubFont = new Font(STANDARD_FONT, Font.PLAIN, (int)(TIME_SUB_FONT_SIZE*(size)));
        timeoutFont = new Font(STANDARD_FONT, Font.PLAIN, (int)(TIMEOUT_FONT_SIZE*(size)));
        stateFont = new Font(STANDARD_FONT, Font.PLAIN, (int)(STATE_FONT_SIZE*(size)));
        
        for(int i=0; i<=1; i++) {
            name[i].setFont(titleFont);
            goalInc[i].setFont(standardFont);
            goalDec[i].setFont(standardFont);
            kickOff[i].setFont(standardFont);
            goals[i].setFont(goalsFont);
            pushes[i].setFont(standardFont);
            for(int j=0; j<robot[i].length; j++) {
                robotLabel[i][j].setFont(titleFont);
            }
            timeOut[i].setFont(timeoutFont);
            stuck[i].setFont(timeoutFont);
            out[i].setFont(standardFont);
        }
        clock.setFont(timeFont);
        clockSub.setFont(timeSubFont);
        firstHalf.setFont(timeoutFont);
        secondHalf.setFont(timeoutFont);
        penaltyShoot.setFont(timeoutFont);
        initial.setFont(stateFont);
        ready.setFont(stateFont);
        set.setFont(stateFont);
        play.setFont(stateFont);
        finish.setFont(stateFont);
        for(int i=0; i<pen.length; i++) {
            pen[i].setFont(standardFont);
        }
        for(int i=0; i<undo.length; i++) {
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
        if(IS_OSX) {
            button.setOpaque(highlight);
            button.setBorderPainted(!highlight);
        }
    }
}