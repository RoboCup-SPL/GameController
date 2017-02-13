package controller.ui.setup;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import data.*;
import data.spl.SPL;
import data.spl.SPLDropIn;
import data.states.GamePreparationData;
import data.states.PrepTeam;
import data.teams.TeamLoadInfo;


/**
 * @author Michel Bartsch
 *         <p>
 *         This is only to be on starting the programm to get starting input.
 */
public class StartInput extends JFrame implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Some constants defining this GUI`s appearance as their names say.
     * Feel free to change them and see what happens.
     */
    private static final String WINDOW_TITLE = "GameController";
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 480;
    private static final int STANDARD_SPACE = 10;
    private static final int TEAMS_HEIGHT = 300;
    private static final int IMAGE_SIZE = 250;
    private static final int OPTIONS_CONTAINER_HEIGHT = 80;
    private static final int OPTIONS_HEIGHT = 22;
    private static final int START_HEIGHT = 30;
    /**
     * This is not what the name says ;)
     */
    private static final int FULLSCREEN_WIDTH = 160;

    /** Dynamically settable path to the config root folder */
    private static final String CONFIG_ROOT = System.getProperty("CONFIG_ROOT", "");

    /** The path to the leagues directory directories. */
    private static final String PATH = CONFIG_ROOT + "config/";

    /** The path to the standard icons */
    private static final String ICONS_PATH = PATH + "icons/";

    private static final String[] BACKGROUND_PREFIX = {"robot_left_", "robot_right_"};
    private static final String BACKGROUND_EXT = ".png";
    private static final String FULLTIME_LABEL_NO = "Preliminaries Game";
    private static final String FULLTIME_LABEL_YES = "Play-off Game";
    private static final String FULLTIME_LABEL_HL_NO = "Normal Game";
    private static final String FULLTIME_LABEL_HL_YES = "Knock-Out Game";
    private static final String FULLSCREEN_LABEL = "Fullscreen";
    private static final String COLOR_CHANGE_LABEL = "Auto color change";
    private static final String START_LABEL = "Start";
    private static final String TEAM_COLOR_CHANGE = "Color";

    /**
     * If true, this GUI has finished and offers it`s input.
     */
    public boolean finished = false;
    private GamePreparationData gamePrepData;

    /**
     * All the components of this GUI.
     */
    private ImagePanel[] teamContainer = new ImagePanel[2];
    private JPanel[] teamChooseContainer = new JPanel[2];
    private JButton[] teamColorChangeButton = new JButton[2];
    private JLabel[] teamIconLabel = new JLabel[2];

    @SuppressWarnings("unchecked")
    private JComboBox<TeamLoadInfo>[] teamSelectionDropDown = (JComboBox<TeamLoadInfo>[]) new JComboBox[2];

    private JPanel optionsLeft;
    private JPanel optionsRight;
    private JComboBox<String> league;
    private JRadioButton nofulltime;
    private JRadioButton fulltime;
    private ButtonGroup fulltimeGroup;
    private Checkbox fullscreen;
    private Checkbox autoColorChange;
    private JButton start;
    private JLabel errorMessage;

    private HashMap<String, Image> images = new HashMap<String, Image>();


    private ActionListener chooseTeam1Listener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e != null) {
                TeamLoadInfo selected = (TeamLoadInfo) teamSelectionDropDown[0].getSelectedItem();
                if (selected != null) {
                    gamePrepData.chooseTeam(0, selected);
                    reloadState();
                }
            }
        }
    };

    private ActionListener chooseTeam2Listener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e != null) {
                TeamLoadInfo selected = (TeamLoadInfo) teamSelectionDropDown[1].getSelectedItem();
                gamePrepData.chooseTeam(1, selected);
                reloadState();

            }
        }
    };

    private ActionListener tcchange1 = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            gamePrepData.getFirstTeam().cycleColours();
            reloadState();
        }
    };

    private ActionListener tcchange2 = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            gamePrepData.getSecondTeam().cycleColours();
            reloadState();
        }
    };


    /**
     * Creates a new StartInput.
     */
    public StartInput(boolean fullscreenMode) {
        super(WINDOW_TITLE);

        // The game preparation data that is the end result of this window
        gamePrepData = new GamePreparationData();

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = gd.getDisplayMode().getWidth();
        int height = gd.getDisplayMode().getHeight();
        setLocation((width - WINDOW_WIDTH) / 2, (height - WINDOW_HEIGHT) / 2);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, STANDARD_SPACE));


        for (int i = 0; i < 2; i++) {
            teamContainer[i] = new ImagePanel();
            teamContainer[i].setPreferredSize(new Dimension(WINDOW_WIDTH / 2 - STANDARD_SPACE, TEAMS_HEIGHT));
            teamContainer[i].setOpaque(true);
            teamContainer[i].setLayout(new BorderLayout());
            add(teamContainer[i]);
            teamIconLabel[i] = new JLabel();
            teamContainer[i].add(teamIconLabel[i], BorderLayout.CENTER);

            teamSelectionDropDown[i] = new JComboBox<TeamLoadInfo>();
            teamChooseContainer[i] = new JPanel(new BorderLayout());
            teamContainer[i].add(teamChooseContainer[i], BorderLayout.SOUTH);
            teamChooseContainer[i].add(teamSelectionDropDown[i], BorderLayout.CENTER);
            //colorNames[i] = new String[]{"red", "blue"};
            teamColorChangeButton[i] = new JButton(TEAM_COLOR_CHANGE);
        }
        teamChooseContainer[0].add(teamColorChangeButton[0], BorderLayout.WEST);
        teamChooseContainer[1].add(teamColorChangeButton[1], BorderLayout.EAST);

        teamColorChangeButton[0].addActionListener(tcchange1);
        teamColorChangeButton[1].addActionListener(tcchange2);

        teamSelectionDropDown[0].addActionListener(chooseTeam1Listener);
        teamSelectionDropDown[1].addActionListener(chooseTeam2Listener);

        optionsLeft = new JPanel();
        optionsLeft.setPreferredSize(new Dimension(WINDOW_WIDTH / 2 - 2 * STANDARD_SPACE, OPTIONS_CONTAINER_HEIGHT));
        optionsLeft.setLayout(new FlowLayout(FlowLayout.CENTER));
        add(optionsLeft);

        JPanel fullscreenPanel = new JPanel();
        fullscreenPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        optionsLeft.add(fullscreenPanel);
        JPanel autoColorChangePanel = new JPanel();
        autoColorChangePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        optionsLeft.add(autoColorChangePanel);

        fullscreen = new Checkbox(FULLSCREEN_LABEL);
        fullscreen.setPreferredSize(new Dimension(FULLSCREEN_WIDTH, OPTIONS_HEIGHT));
        fullscreen.setState(fullscreenMode);
        fullscreenPanel.add(fullscreen);

        autoColorChange = new Checkbox(COLOR_CHANGE_LABEL);
        autoColorChange.setPreferredSize(new Dimension(FULLSCREEN_WIDTH, OPTIONS_HEIGHT));
        autoColorChange.setState(Rules.league.colorChangeAuto);
        autoColorChangePanel.add(autoColorChange);
        autoColorChange.setState(Rules.league.colorChangeAuto);

        optionsRight = new JPanel();
        optionsRight.setPreferredSize(new Dimension(WINDOW_WIDTH / 2 - 2 * STANDARD_SPACE, OPTIONS_CONTAINER_HEIGHT));
        add(optionsRight);
        Dimension optionsDim = new Dimension(WINDOW_WIDTH / 3 - 2 * STANDARD_SPACE, OPTIONS_HEIGHT);
        setupLeagueSelection(optionsDim);


        nofulltime = new JRadioButton();
        nofulltime.setPreferredSize(optionsDim);
        fulltime = new JRadioButton();
        fulltime.setPreferredSize(optionsDim);
        fulltimeGroup = new ButtonGroup();
        fulltimeGroup.add(nofulltime);
        fulltimeGroup.add(fulltime);
        optionsRight.add(nofulltime);
        optionsRight.add(fulltime);
        nofulltime.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e != null) {
                    reloadState();
                }
            }
        });
        fulltime.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e != null) {
                    reloadState();
                }
            }
        });

        start = new JButton(START_LABEL);
        start.setPreferredSize(new Dimension(WINDOW_WIDTH / 3 - 2 * STANDARD_SPACE, START_HEIGHT));
        start.setEnabled(false);
        add(start);
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gamePrepData.setFullTimeGame(fulltime.isSelected() && fulltime.isVisible());
                gamePrepData.setFullScreen(fullscreen.getState());
                gamePrepData.setAutoColorChange(autoColorChange.getState());
                finished = true;
            }
        });

        errorMessage = new JLabel("");
        errorMessage.setPreferredSize(new Dimension(WINDOW_WIDTH - 2 * STANDARD_SPACE, START_HEIGHT));
        errorMessage.setForeground(Color.RED);
        add(errorMessage);

        league.getActionListeners()[league.getActionListeners().length - 1].actionPerformed(null);

        getContentPane().setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        pack();
        setVisible(true);
        reloadState();
    }

    private void setupLeagueSelection(Dimension optionsDim) {
        league = new JComboBox<String>();
        optionsRight.add(league);

        // Add all the leagues one can select
        for (int i = 0; i < Rules.LEAGUES.length; i++) {
            league.addItem(Rules.LEAGUES[i].leagueName);
            if (Rules.LEAGUES[i] == Rules.league) {
                league.setSelectedIndex(i);
            }
        }

        league.setPreferredSize(optionsDim);
        league.addActionListener(new ActionListener() {
                                     @Override
                                     public void actionPerformed(ActionEvent e) {
                                         if (e != null) { // not initial setup
                                             for (int i = 0; i < Rules.LEAGUES.length; i++) {
                                                 if (Rules.LEAGUES[i].leagueName.equals((String) league.getSelectedItem())) {
                                                     Rules.league = Rules.LEAGUES[i];
                                                     gamePrepData.switchRules(Rules.league);
                                                     break;
                                                 }
                                             }
                                             reloadState();
                                         }
                                     }
                                 }
        );
    }

    /**
     * Show in the combo box which teams are available for the selected league and competition
     */
    private void showAvailableTeams() {
        ArrayList<TeamLoadInfo> preparedTeams = gamePrepData.getAvailableTeams();


        for (int i = 0; i < 2; i++) {
            teamSelectionDropDown[i].removeAllItems();

            if (Rules.league.dropInPlayerMode) {
                teamSelectionDropDown[i].addItem(preparedTeams.get(0));
                teamSelectionDropDown[i].addItem(preparedTeams.get(i == 0 ? 1 : 2));
            } else {
                for (int j = 0; j < preparedTeams.size(); j++) {
                    teamSelectionDropDown[i].addItem(preparedTeams.get(j));
                }
            }
            teamSelectionDropDown[i].setSelectedItem(gamePrepData.getPrepTeam(i).getTeamInfo());
        }
    }


    /**
     * Sets the Team-Icon on the GUI.
     */
    private void updateTeamIcons() {

        for (int side = 0; side < 2; side++) {
            BufferedImage teamImg = gamePrepData.getPrepTeam(side).getTeamInfo().icon;


            ImageIcon teamIImageIcon;
            if (teamImg == null){
                continue;
            } else {
                teamIImageIcon = new ImageIcon(teamImg);
            }


            float scaleFactor;
            if (teamIImageIcon.getImage().getWidth(null) > teamIImageIcon.getImage().getHeight(null)) {
                scaleFactor = (float) IMAGE_SIZE / teamIImageIcon.getImage().getWidth(null);
            } else {
                scaleFactor = (float) IMAGE_SIZE / teamIImageIcon.getImage().getHeight(null);
            }

            // getScaledInstance/SCALE_SMOOTH does not work with all color models, so we need to convert image
            BufferedImage image = (BufferedImage) teamIImageIcon.getImage();
            if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
                BufferedImage temp = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics g = temp.createGraphics();
                g.drawImage(image, 0, 0, null);
                g.dispose();
                image = temp;
            }
            ImageIcon i = new ImageIcon();
            i.setImage(image.getScaledInstance(
                    (int) (teamIImageIcon.getImage().getWidth(null) * scaleFactor),
                    (int) (teamIImageIcon.getImage().getHeight(null) * scaleFactor),
                    Image.SCALE_SMOOTH));

            teamIconLabel[side].setIcon(i);
            teamIconLabel[side].repaint();
        }
    }

    /**
     * Method that reloads the UI based on the data the @{@link GamePreparationData} holds
     */
    private void reloadState() {
        // We need to detach the action listeners - for some reason they interfere otherwise
        teamColorChangeButton[0].removeActionListener(tcchange1);
        teamColorChangeButton[1].removeActionListener(tcchange2);
        teamSelectionDropDown[0].removeActionListener(chooseTeam1Listener);
        teamSelectionDropDown[1].removeActionListener(chooseTeam2Listener);

        Rules active = gamePrepData.getCurrentRules();

        if (active instanceof SPLDropIn) {
            nofulltime.setVisible(false);
            fulltime.setVisible(false);
            autoColorChange.setVisible(false);
        } else {
            nofulltime.setVisible(true);
            fulltime.setVisible(true);
            if (Rules.league instanceof SPL) {
                nofulltime.setText(FULLTIME_LABEL_NO);
                fulltime.setText(FULLTIME_LABEL_YES);
                autoColorChange.setVisible(false);
            } else {
                nofulltime.setText(FULLTIME_LABEL_HL_NO);
                fulltime.setText(FULLTIME_LABEL_HL_YES);
                autoColorChange.setState(Rules.league.colorChangeAuto);
                autoColorChange.setVisible(true);
            }
        }

        showAvailableTeams(); // Reloads the available teams
        drawTeamColors(); // Draws the team color selection buttons
        updateTeamIcons(); // Draws the Team Icons if present
        updateBackgrounds(); // Draws the background robots in right colours
        updateErrorHint(); // Updates the error hint and enables/disables start button


        // Reattaching the ActionListeners so the UI works correctly again
        teamColorChangeButton[0].addActionListener(tcchange1);
        teamColorChangeButton[1].addActionListener(tcchange2);
        teamSelectionDropDown[0].addActionListener(chooseTeam1Listener);
        teamSelectionDropDown[1].addActionListener(chooseTeam2Listener);
    }

    private void updateErrorHint() {
        if (!(fulltime.isSelected() || nofulltime.isSelected() || !fulltime.isVisible())){
            errorMessage.setText("Please select a game mode!");
            start.setEnabled(false);
            return;
        }

        String problem = gamePrepData.canStart();

        if (problem == null){
            start.setEnabled(true);
            errorMessage.setText("");
        } else {
            start.setEnabled(false);
            errorMessage.setText(problem);
        }
    }

    private void updateBackgrounds(){
        for (int i = 0; i < 2; i++) {
            String current_team_color = gamePrepData.getPrepTeam(i).getTeamColor();
            current_team_color = current_team_color.toLowerCase(); // TODO Make sure to universalize the color strings (maybe cosntants or enum)
            String filename = ICONS_PATH + gamePrepData.getCurrentRules().leagueDirectory + "/" + BACKGROUND_PREFIX[i] + current_team_color + BACKGROUND_EXT;
            ImageIcon teamIImageIcon = new ImageIcon(filename);
            teamContainer[i].setImage(teamIImageIcon.getImage());
            teamContainer[i].repaint();
        }
    }

    private void drawTeamColors() {
        for (int i = 0; i < 2; i++){
            PrepTeam team = gamePrepData.getPrepTeam(i);
            Color activeColor = Helper.getColorByString(gamePrepData.getCurrentRules(), team.getTeamColor());
            assert activeColor != null : String.format("Could not determine color value for %s", team.getTeamColor());
            teamColorChangeButton[i].setBackground(activeColor);
        }
    }

    public GamePreparationData getGamePreparationData() {
        return gamePrepData;
    }
}
