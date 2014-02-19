package controller.ui;

import data.Rules;
import data.SPL;
import data.Teams;
import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import javax.swing.*;


/**
 * @author Michel Bartsch
 * 
 * This is only to be on starting the programm to get starting input.
 */
public class StartInput extends JFrame implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * Some constants defining this GUI`s appearance as their names say.
     * Feel free to change them and see what happens.
     */
    private static final String WINDOW_TITLE = "GameController";
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 510;
    private static final int STANDARD_SPACE = 10;
    private static final int TEAMS_HEIGHT = 300;
    private static final int IMAGE_SIZE = 250;
    private static final int OPTIONS_CONTAINER_HEIGHT = 80;
    private static final int OPTIONS_HEIGHT = 22;
    private static final int START_HEIGHT = 30;
    /** This is not what the name says ;) */
    private static final int FULLSCREEN_WIDTH = 160;
    private static final String ICONS_PATH = "config/icons/";
    private static final String[] BACKGROUND_SIDE = {"robot_left_red.png",
                                                        "robot_right_blue.png"};
    private static final String FULLTIME_LABEL_NO = "Preliminaries Game";
    private static final String FULLTIME_LABEL_YES = "Play-off Game";
    private static final String DROP_IN_PLAYER_COMPETITION = "Drop-In Player Modus";
    private static final String FULLTIME_LABEL_HL_NO = "Normal Game";
    private static final String FULLTIME_LABEL_HL_YES = "Knock-Out Game";
    private static final String FULLSCREEN_LABEL = "Fullscreen";
    private static final String COLOR_CHANGE_LABEL = "Auto color change";
    private static final String START_LABEL = "Start";
    
    /** If true, this GUI has finished and offers it`s input. */
    public boolean finished = false;
    
    /**
     * This is true, if the teams chosen are legal. They are not legal, if
     * they are the same.
     */
    private boolean teamsOK = false;
    /**
     * This is true, if a time-mode has manually be set. Manually setting this
     * is recommended.
     */
    private boolean fulltimeOK = false;
    
    /** The inputs that can be read from this GUI when it has finished. */
    public int[] outTeam = {0, 0};
    public boolean outFulltime;
    public boolean outFullscreen;
    public boolean outAutoColorChange;
    public boolean dropInPlayerMode = false;
    
    /** All the components of this GUI. */
    private ImagePanel[] teamContainer = new ImagePanel[2];
    private ImageIcon[] teamIcon = new ImageIcon[2];
    private JLabel[] teamIconLabel = new JLabel[2];
    private JComboBox[] team = new JComboBox[2];
    private JPanel optionsLeft;
    private JPanel optionsRight;
    private JComboBox league;
    private JRadioButton nofulltime;
    private JRadioButton fulltime;
    private JRadioButton dropInPlayerCompetition;
    private ButtonGroup fulltimeGroup;
    private Checkbox fullscreen;
    private Checkbox autoColorChange;
    private JButton start;
    
    
    /**
     * Creates a new StartInput.
     * @param args The parameters that the jar file was started with.
     */
    @SuppressWarnings("unchecked")
    public StartInput(boolean fullscreenMode)
    {
        super(WINDOW_TITLE);

        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        Dimension desktop = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((desktop.width-WINDOW_WIDTH)/2, (desktop.height-WINDOW_HEIGHT)/2);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, STANDARD_SPACE));
        
        String[] teams = getShortTeams();
        for (int i=0; i<2; i++) {
            teamContainer[i] = new ImagePanel((
                    new ImageIcon(ICONS_PATH+Rules.league.leagueDirectory+"/"+BACKGROUND_SIDE[i])).getImage());
            teamContainer[i].setPreferredSize(new Dimension(WINDOW_WIDTH/2-STANDARD_SPACE, TEAMS_HEIGHT));
            teamContainer[i].setOpaque(true);
            teamContainer[i].setLayout(new BorderLayout());
            add(teamContainer[i]);
            setTeamIcon(i, 0);
            teamIconLabel[i] = new JLabel(teamIcon[i]);
            teamContainer[i].add(teamIconLabel[i], BorderLayout.CENTER);
            team[i] = new JComboBox(teams);
            teamContainer[i].add(team[i], BorderLayout.SOUTH);
        }
        team[0].addActionListener(new ActionListener()
            {
            @Override
                public void actionPerformed(ActionEvent e)
                {
                    Object selected = team[0].getSelectedItem();
                    if (selected == null)
                    {
                        return;
                    }
                    outTeam[0] = Integer.valueOf(((String)selected).split(": ")[0]);
                    setTeamIcon(0, outTeam[0]);
                    teamIconLabel[0].setIcon(teamIcon[0]);
                    teamIconLabel[0].repaint();
                    teamsOK = outTeam[0] != outTeam[1];
                    startEnableing();
                }
            }
        );
        team[1].addActionListener(new ActionListener()
            {
            @Override
                public void actionPerformed(ActionEvent e)
                {
                    Object selected = team[1].getSelectedItem();
                    if (selected == null)
                    {
                        return;
                    }
                    outTeam[1] = Integer.valueOf(((String)selected).split(": ")[0]);
                    setTeamIcon(1, outTeam[1]);
                    teamIconLabel[1].setIcon(teamIcon[1]);
                    teamIconLabel[1].repaint();
                    teamsOK = outTeam[1] != outTeam[0];
                    startEnableing();
                }
            }
        );

        optionsLeft = new JPanel();
        optionsLeft.setPreferredSize(new Dimension(WINDOW_WIDTH/2-2*STANDARD_SPACE, OPTIONS_CONTAINER_HEIGHT));
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
        autoColorChange.setVisible(false);
        autoColorChange.setState(Rules.league.colorChangeAuto);
        
        optionsRight = new JPanel();
        optionsRight.setPreferredSize(new Dimension(WINDOW_WIDTH/2-2*STANDARD_SPACE, OPTIONS_CONTAINER_HEIGHT + 30));
        add(optionsRight);
        Dimension optionsDim = new Dimension(WINDOW_WIDTH/3-2*STANDARD_SPACE, OPTIONS_HEIGHT);
        league = new JComboBox();
        for (int i=0; i < Rules.LEAGUES.length; i++) {
            league.addItem(Rules.LEAGUES[i].leagueName);
            if (Rules.LEAGUES[i] == Rules.league) {
                league.setSelectedIndex(i);
            }
        }
        league.setPreferredSize(optionsDim);
        league.addActionListener(new ActionListener()
            {
            @Override
                public void actionPerformed(ActionEvent e)
                {
                    for (int i=0; i < Rules.LEAGUES.length; i++) {
                        if (Rules.LEAGUES[i].leagueName.equals((String)league.getSelectedItem())) {
                            Rules.league = Rules.LEAGUES[i];
                            break;
                        }
                    }
                    for (int i=0; i < 2; i++) {
                        teamContainer[i].setImage((
                                new ImageIcon(ICONS_PATH+Rules.league.leagueDirectory+"/"+BACKGROUND_SIDE[i])).getImage());
                        team[i].removeAllItems();
                        String[] names = getShortTeams();
                        for (int j=0; j < names.length; j++) {
                            team[i].addItem(names[j]);
                        }
                        outTeam[i] = 0;
                        setTeamIcon(i, outTeam[i]);
                        teamIconLabel[i].setIcon(teamIcon[i]);
                        teamIconLabel[i].repaint();
                    }
                    teamsOK = false;
                    
                    if (Rules.league instanceof SPL) {
                        nofulltime.setText(FULLTIME_LABEL_NO);
                        fulltime.setText(FULLTIME_LABEL_YES);
                        autoColorChange.setVisible(false);
                        dropInPlayerCompetition.setVisible(true);
                    } else {
                        nofulltime.setText(FULLTIME_LABEL_HL_NO);
                        fulltime.setText(FULLTIME_LABEL_HL_YES);
                        dropInPlayerCompetition.setVisible(false);
                        autoColorChange.setState(Rules.league.colorChangeAuto);
                        autoColorChange.setVisible(true);
                    }
                    startEnableing();
                }
            }
        );
        optionsRight.add(league);
        nofulltime = new JRadioButton();
        nofulltime.setPreferredSize(optionsDim);
        dropInPlayerCompetition = new JRadioButton();
        dropInPlayerCompetition.setPreferredSize(optionsDim);
        fulltime = new JRadioButton();
        fulltime.setPreferredSize(optionsDim);
        if (Rules.league instanceof SPL) {
            nofulltime.setText(FULLTIME_LABEL_NO);
            fulltime.setText(FULLTIME_LABEL_YES);
            dropInPlayerCompetition.setText(DROP_IN_PLAYER_COMPETITION);
        } else {
            nofulltime.setText(FULLTIME_LABEL_HL_NO);
            fulltime.setText(FULLTIME_LABEL_HL_YES);
        }
        fulltimeGroup = new ButtonGroup();
        fulltimeGroup.add(nofulltime);
        fulltimeGroup.add(fulltime);
        fulltimeGroup.add(dropInPlayerCompetition);
        
        optionsRight.add(nofulltime);
        optionsRight.add(fulltime);
        optionsRight.add(dropInPlayerCompetition);
        dropInPlayerCompetition.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				outFulltime = true;
				dropInPlayerMode = true;
                fulltimeOK = true;
                startEnableing();
			}
		});
        
        nofulltime.addActionListener(new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e) {
                    outFulltime = false;
                    fulltimeOK = true;
                    dropInPlayerMode = false;
                    startEnableing();
                }});
        fulltime.addActionListener(new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e) {
                    outFulltime = true;
                    fulltimeOK = true;
                    dropInPlayerMode = false;
                    startEnableing();
                }});
        
        start = new JButton(START_LABEL);
        start.setPreferredSize(new Dimension(WINDOW_WIDTH/3-2*STANDARD_SPACE, START_HEIGHT));
        start.setEnabled(false);
        add(start);
        start.addActionListener(new ActionListener() {
            @Override
                public void actionPerformed(ActionEvent e) {
                    outFullscreen = fullscreen.getState();
                    outAutoColorChange = autoColorChange.getState();
                    finished = true;
                }});
                
        setVisible(true);
    }
    
    /**
     * Calculates an array that contains only the existing Teams of the
     * current league.
     * 
     * @return  Short teams array with numbers
     */ 
    private String[] getShortTeams()
    {
        String[] fullTeams = Teams.getNames(true);
        int k = 0;
        for (int j=0; j<fullTeams.length; j++) {
            if (fullTeams[j] != null) {
                k++;
            }
        }
        String[] out = new String[k];
        k = 0;
        for (int j=0; j<fullTeams.length; j++) {
            if (fullTeams[j] != null) {
                out[k++] = fullTeams[j];
            }
        }
        return out;
    }
    
    /**
     * Sets the Team-Icon on the GUI.
     * 
     * @param side      The side (0=left, 1=right)
     * @param team      The number of the Team
     */ 
    private void setTeamIcon(int side, int team)
    {
        teamIcon[side] = new ImageIcon(Teams.getIcon(team));
        float scaleFactor;
        if (teamIcon[side].getImage().getWidth(null) > teamIcon[side].getImage().getHeight(null)) {
            scaleFactor = (float)IMAGE_SIZE/teamIcon[side].getImage().getWidth(null);
        } else {
            scaleFactor = (float)IMAGE_SIZE/teamIcon[side].getImage().getHeight(null);
        }
        teamIcon[side].setImage(teamIcon[side].getImage().getScaledInstance(
                (int)(teamIcon[side].getImage().getWidth(null)*scaleFactor),
                (int)(teamIcon[side].getImage().getHeight(null)*scaleFactor),
                Image.SCALE_DEFAULT));
    }
    
    /**
     * Enables the start button, if the conditions are ok.
     */
    private void startEnableing()
    {
        start.setEnabled(teamsOK && fulltimeOK);
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
            g.drawImage(image, (getWidth()-image.getWidth(null))/2, 0, image.getWidth(null), image.getHeight(null), null);
        }
    }
}