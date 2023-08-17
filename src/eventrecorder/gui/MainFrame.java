package eventrecorder.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import common.Log;
import eventrecorder.EventRecorder;
import eventrecorder.action.Action;
import eventrecorder.action.EntryCreateAction;
import eventrecorder.action.TitleChangeAction;
import eventrecorder.export.MarkDownExporter;
import eventrecorder.data.LogEntry;
import eventrecorder.data.LogType;

/**
 * Main window of the event recorder tool.
 *
 * @author Andre Muehlenbrock
 *
 */

public class MainFrame extends JFrame {
    private static final long serialVersionUID = -6572494292734211470L;

    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("mm:ss");
    public static final String ICONS_PATH = "config/icons/";

    public static final Color MANUALLY_LOG_ENTRY_COLOR = new Color(0xFFFFFF);
    public static final Color GAMESTATE_LOG_ENTRY_COLOR = new Color(0xFFCACA);
    public static final Color PLAYERSTATE_LOG_ENTRY_COLOR = new Color(0xCACAFF);

    public static final Color DISCONNECTED_BACKGROUND_COLOR = new Color(0xF0F0F0);
    public static final Color CONNECTED_BACKGROUND_COLOR = new Color(0xD0FFD0);

    public static final JLabel DISCONNECTED_LABEL = new JLabel("No active GameController in network.");
    public static final JLabel CONNECTED_LABEL = new JLabel("Connected to GameController!");

    public static final int SCROLL_SPEED = 16;
    public static final int START_WINDOW_WIDTH = 800;
    public static final int START_WINDOW_HEIGHT = 600;

    public static final int CURRENT_TIME_SMALL_STEP = 5;
    public static final int CURRENT_TIME_BIG_STEP = 60;

    private final Preferences prefs;

    private final LogEntryTable entryTable;

    private final JPanel statusPanel = new JPanel();

    private final ImageButton undoButton;
    private final ImageButton redoButton;
    private final JLabel currentTimeLabel;

    private final ImageToggleButton startButton;
    private final ImageButton resetButton;

    private final JTextArea additionalField;

    private final JTextField titleField;

    private boolean activeGameController;

    private final JButton timePlusPlusButton;
    private final JButton timePlusButton;

    private final JButton timeMinusMinusButton;
    private final JButton timeMinusButton;

    /**
     * Creates the Main Window.
     */

    public MainFrame(){
        JComponent.setDefaultLocale(Locale.ENGLISH);

        // Enable fullscreen mode on macOS
        getRootPane().putClientProperty("apple.awt.fullscreenable", true);

        // Load preferences:
        prefs = Preferences.userRoot().node(this.getClass().getName());
        EventRecorder.model.title = prefs.get("DEFAULT_TITLE", "1. Testgame");
        EventRecorder.model.additionalInfo = prefs.get("DEFAULT_ADDITIONAL", "- Start: "+EventRecorder.DATE_TIME_FORMAT.format(new Date())+" \n\n- Black Team: \n- Red Team: ");

        // Set System Look and Feel:
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e){
            System.out.println("Warning: System Look and Feel could not be set!");
        }

        // Increase default font size up to 14:
        setUIFont(new javax.swing.plaf.FontUIResource("Sans",Font.PLAIN,14));

        // Set Menubar:
        setJMenuBar(new MenuBar());

        // Calculate fitting window size and position:
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min((int)screenSize.getWidth(),START_WINDOW_WIDTH);
        int height = Math.min((int)screenSize.getHeight(),START_WINDOW_HEIGHT);
        int x = ((int)screenSize.getWidth() - width) / 2;
        int y = ((int)screenSize.getHeight() - height) / 2;

        // Set some window settings:
        setTitle("Event Recorder");
        setBounds(x, y, width, height);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Defines a method if the users wants to close the window:
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we)
            {
                if(saveBeforeExit()){
                    EventRecorder.cleanExit();
                }
            }

        });

        // JPanel which contains all:
        JPanel main = new JPanel();
        main.setLayout(new BorderLayout());

        // Contains title, additional and other information:
        JPanel head = new JPanel();
        head.setLayout(new BoxLayout(head,BoxLayout.Y_AXIS));
        try {
            String current = new java.io.File( "." ).getCanonicalPath();
            System.out.println(current);
        } catch (IOException e) {
            Log.error(e.getMessage());
            System.exit(1);
        }
        // Undo and Redo Line:
        JPanel topLine = new JPanel();
        topLine.setLayout(new BorderLayout());
        undoButton = new ImageButton("Make Undo ( Strg+Z )",ICONS_PATH+"undo.png",ICONS_PATH+"undo_disabled.png", EventRecorder.history.undoPossible(),24,24);
        undoButton.addActionListener(e -> EventRecorder.history.undo());

        redoButton = new ImageButton("Make Redo ( Strg+Y )",ICONS_PATH+"redo.png",ICONS_PATH+"redo_disabled.png", EventRecorder.history.redoPossible(),24,24);
        redoButton.addActionListener(e -> EventRecorder.history.redo());

        // Current Time Label:
        currentTimeLabel = new JLabel(TIME_FORMAT.format(EventRecorder.model.currentTime*1000));
        currentTimeLabel.setFont(new Font("Sans", Font.BOLD, 16));
        currentTimeLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));


        ImageButton createNewButton = new ImageButton("New ( Strg+New )", ICONS_PATH+"plus_icon.png", 24,24);
        createNewButton.addActionListener(e -> EventRecorder.history.execute(new EntryCreateAction(new LogEntry("","",LogType.Manually))));

        // Start/Stop and Reset-Button:
        startButton = new ImageToggleButton("Play and Pause ( Strg+Space )", ICONS_PATH+"pause_icon.png", ICONS_PATH+"play_icon.png", false,24,24);
        startButton.addActionListener(e -> EventRecorder.model.isManuallyRunning = !EventRecorder.model.isManuallyRunning);

        resetButton = new ImageButton("Reset", ICONS_PATH+"reset_icon.png", 24,24);
        resetButton.addActionListener(e -> {
            EventRecorder.model.currentTime = 600;
            currentTimeLabel.setText((EventRecorder.model.currentTime < 0? "-":"")+TIME_FORMAT.format(Math.abs(EventRecorder.model.currentTime*1000)));
            currentTimeLabel.revalidate();
            currentTimeLabel.repaint();
        });

        // Add Buttons to the Top Panel and Top Panel to the Main Panel:
        JPanel redoUndoPanel = new JPanel();
        redoUndoPanel.add(undoButton);
        redoUndoPanel.add(redoButton);
        redoUndoPanel.add(Box.createRigidArea(new Dimension(54,1)));

        JPanel currentTimeWrapper = new JPanel();
        currentTimeWrapper.setLayout(new BoxLayout(currentTimeWrapper,BoxLayout.Y_AXIS));
        currentTimeLabel.setAlignmentX(0.5f);
        currentTimeWrapper.add(currentTimeLabel);
        currentTimeWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        timeMinusMinusButton = new JButton("--");
        timeMinusMinusButton.setToolTipText("Decrease by 60 seconds ( Strg+Shift+Minus )");
        timeMinusMinusButton.addActionListener(e -> {
            EventRecorder.model.currentTime -= CURRENT_TIME_BIG_STEP;
            updateTimeAndButtons();
        });
        timeMinusButton = new JButton("-");
        timeMinusButton.setToolTipText("Decrease by 5 seconds ( Strg+Minus )");
        timeMinusButton.addActionListener(e -> {
            EventRecorder.model.currentTime -= CURRENT_TIME_SMALL_STEP;
            updateTimeAndButtons();
        });

        timePlusButton = new JButton("+");
        timePlusButton.setToolTipText("Increase by 5 seconds ( Strg+Plus )");
        timePlusButton.addActionListener(e -> {
            EventRecorder.model.currentTime += CURRENT_TIME_SMALL_STEP;
            updateTimeAndButtons();
        });

        timePlusPlusButton = new JButton("++");
        timePlusPlusButton.setToolTipText("Increase by 60 seconds ( Strg+Shift+Plus )");
        timePlusPlusButton.addActionListener(e -> {
            EventRecorder.model.currentTime += CURRENT_TIME_BIG_STEP;
            updateTimeAndButtons();
        });

        JPanel currentTimePanel = new JPanel();
        currentTimePanel.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
        currentTimePanel.add(timeMinusMinusButton);
        currentTimePanel.add(timeMinusButton);
        currentTimePanel.add(currentTimeWrapper);
        currentTimePanel.add(timePlusButton);
        currentTimePanel.add(timePlusPlusButton);



        JPanel timeControlPanel = new JPanel();
        timeControlPanel.add(createNewButton);
        timeControlPanel.add(startButton);
        timeControlPanel.add(resetButton);

        topLine.add(redoUndoPanel, BorderLayout.WEST);
        topLine.add(currentTimePanel, BorderLayout.CENTER);
        topLine.add(timeControlPanel, BorderLayout.EAST);

        head.add(topLine);
        head.add(new JSeparator(JSeparator.HORIZONTAL));



        FocusListener titleChangeExecutionListener = new FocusListener(){
            String savedAdditional,savedTitle;

            @Override
            public void focusGained(FocusEvent e) {
                savedAdditional = additionalField.getText();
                savedTitle = titleField.getText();
            }

            @Override
            public void focusLost(FocusEvent e) {
                if(savedAdditional != null && savedTitle != null){
                    String newAdditional = additionalField.getText();
                    String newTitle  = titleField.getText();

                    if(!savedAdditional.equals(newAdditional) || !savedTitle.equals(newTitle)){
                        EventRecorder.history.execute(new TitleChangeAction(null, newTitle, newAdditional, savedTitle, savedAdditional));
                        prefs.put("DEFAULT_TITLE", newTitle);
                        prefs.put("DEFAULT_ADDITIONAL", newAdditional);
                    }
                }


                savedAdditional = null;
                savedTitle = null;
            }
        };


        // Titel Panel
        JPanel titleLine = new JPanel();
        titleLine.setLayout(new BorderLayout());
        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setPreferredSize(new Dimension(100, titleLabel.getPreferredSize().height));
        titleLine.add(titleLabel, BorderLayout.WEST);
        titleLine.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        titleField = new JTextField(EventRecorder.model.title);
        titleField.addFocusListener(titleChangeExecutionListener);
        titleField.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
        titleLine.add(titleField, BorderLayout.CENTER);
        head.add(titleLine);

        // Additional Panel
        JPanel additionalLine = new JPanel();
        additionalLine.setLayout(new BorderLayout());
        JLabel additionalLabel = new JLabel("Additional:");
        additionalLabel.setPreferredSize(new Dimension(100, titleLabel.getPreferredSize().height));
        additionalLine.add(additionalLabel, BorderLayout.WEST);
        additionalLine.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));

        additionalField = new JTextArea(EventRecorder.model.additionalInfo);
        additionalField.addFocusListener(titleChangeExecutionListener);
        additionalField.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
        additionalLine.add(additionalField, BorderLayout.CENTER);
        head.add(additionalLine);

        // Creates the logEntryTable:
        entryTable = new LogEntryTable();

        // Put the logEntryTable into an BorderLayout.North-Field to pack it:
        JPanel entryTableWrapper = new JPanel();
        entryTableWrapper.setLayout(new BorderLayout());
        entryTableWrapper.add(entryTable, BorderLayout.NORTH);

        // ScrollPane around the entryTable:
        JScrollPane scrollPane = new JScrollPane(entryTableWrapper);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED);

        // Add all subPanels to the main panel:
        main.add(head, BorderLayout.NORTH);
        main.add(scrollPane, BorderLayout.CENTER);

        // Adds Status panel to the top:
        JPanel mainWrapper = new JPanel();
        mainWrapper.setLayout(new BorderLayout());

        statusPanel.add(DISCONNECTED_LABEL);
        statusPanel.setBackground(DISCONNECTED_BACKGROUND_COLOR);
        statusPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xA0A0A0)));
        mainWrapper.add(statusPanel, BorderLayout.NORTH);
        mainWrapper.add(main, BorderLayout.CENTER);

        add(mainWrapper);
        setVisible(true);

        // Update some running informations on the gui:
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run() {
               updateTimeAndButtons();
            }
        }, 1000, 1000);

        setupShortCuts();
    }

    private void setupShortCuts(){
        KeyboardFocusManager keyManager;

        keyManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        keyManager.addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                if (e.isControlDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        EventRecorder.history
                                .execute(new EntryCreateAction(new LogEntry("", "", LogType.Manually)));
                        e.consume();
                        return true;

                    } else if(e.getKeyCode() == KeyEvent.VK_Z){
                        // Before undo, the action on focusLost has to be executed:
                        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                        if(focusOwner instanceof TextField)
                            ((TextField)focusOwner).executeChangeAction();
                        if(focusOwner instanceof TimeField)
                            ((TimeField)focusOwner).executeChangeAction();

                        // Undo:
                        EventRecorder.history.undo();
                        e.consume();
                        return true;
                    } else if(e.getKeyCode() == KeyEvent.VK_Y){
                        // Before redo, the action on focusLost has to be executed:
                        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                        if(focusOwner instanceof TextField)
                            ((TextField)focusOwner).executeChangeAction();
                        if(focusOwner instanceof TimeField)
                            ((TimeField)focusOwner).executeChangeAction();

                        // Redo:
                        EventRecorder.history.redo();
                        e.consume();
                        return true;
                    } else if(e.getKeyCode() == KeyEvent.VK_W){
                        // Delete last typed word:
                        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

                        StringBuilder text = null;
                        if(focusOwner instanceof JTextField)
                            text = new StringBuilder(((JTextField) focusOwner).getText());
                        else if(focusOwner instanceof JTextArea)
                            text = new StringBuilder(((JTextArea) focusOwner).getText());

                        String[] textParts = text.toString().split(" ");

                        text = new StringBuilder();
                        for(int i=0;i<textParts.length-1;i++){
                            text.append(textParts[i]).append(" ");
                        }

                        if(focusOwner instanceof JTextField)
                            ((JTextField)focusOwner).setText(text.toString());
                        if(focusOwner instanceof JTextArea)
                            ((JTextArea)focusOwner).setText(text.toString());

                    } else if(e.getKeyCode() == KeyEvent.VK_PLUS){
                        // Increase current time:
                        if(!activeGameController){
                            if(e.isShiftDown()){
                                EventRecorder.model.currentTime += CURRENT_TIME_BIG_STEP;
                            } else {
                                EventRecorder.model.currentTime += CURRENT_TIME_SMALL_STEP;
                            }
                            updateTimeAndButtons();
                        }
                        e.consume();
                        return true;
                    } else if(e.getKeyCode() == KeyEvent.VK_MINUS){
                        // Decrease current time:
                        if(!activeGameController){
                            if(e.isShiftDown()){
                                EventRecorder.model.currentTime -= CURRENT_TIME_BIG_STEP;
                            } else {
                                EventRecorder.model.currentTime -= CURRENT_TIME_SMALL_STEP;
                            }
                            updateTimeAndButtons();
                        }
                        e.consume();
                        return true;
                    } else if(e.getKeyCode() == KeyEvent.VK_SPACE){
                        // Increase current time:
                        if(!activeGameController){
                            EventRecorder.model.isManuallyRunning = !EventRecorder.model.isManuallyRunning;
                        }
                        e.consume();
                        return true;
                    }
                }
            }

            return false;
        });
    }

    private void updateTimeAndButtons(){
        if(EventRecorder.model.isManuallyRunning){
            EventRecorder.model.currentTime--;
        }

        currentTimeLabel.setText((EventRecorder.model.currentTime < 0? "-":"")+TIME_FORMAT.format(Math.abs(EventRecorder.model.currentTime*1000)));
        currentTimeLabel.revalidate();
        currentTimeLabel.repaint();

        // Update the startButton activated state:
        if(EventRecorder.model.isManuallyRunning != startButton.isActivated()){
            startButton.setActivated(EventRecorder.model.isManuallyRunning);
        }

        activeGameController = EventRecorder.model.lastGameControllerInfo + EventRecorder.GAMECONTROLLER_TIMEOUT >= System.currentTimeMillis();

        // Activate or deactivate buttons:
        if(startButton.isEnabled() == activeGameController){
            startButton.setEnabled(!activeGameController);
            resetButton.setEnabled(!activeGameController);

            timeMinusMinusButton.setEnabled(!activeGameController);
            timeMinusButton.setEnabled(!activeGameController);
            timePlusButton.setEnabled(!activeGameController);
            timePlusPlusButton.setEnabled(!activeGameController);

            statusPanel.removeAll();
            statusPanel.add(activeGameController?CONNECTED_LABEL:DISCONNECTED_LABEL);
            statusPanel.setBackground(activeGameController?CONNECTED_BACKGROUND_COLOR:DISCONNECTED_BACKGROUND_COLOR);
            statusPanel.revalidate();
            statusPanel.repaint();
        }
    }

    /**
     * Ask the user whether he wants to save the records and save it if desired.
     *
     * @return Should the program closed?
     */

    private boolean saveBeforeExit() {
        int result = JOptionPane.showConfirmDialog(null, "Do you want to save your changes before exit?", "Save Before Exit Dialog", JOptionPane.YES_NO_CANCEL_OPTION);

        if(result == JOptionPane.YES_OPTION){
            return saveAs();
        } else {
            return result == JOptionPane.NO_OPTION;
        }
    }

    /**
     * Opens a save dialog and saves the recorded events as markdown file.
     *
     * @return  File saved?
     */

    public boolean saveAs(){
        JFileChooser fileChooser = new JFileChooser(prefs.get("DEFAULT_SAVE_DIRECTORY", ""));

        boolean askAgain = true;
        while(askAgain){
            askAgain = false;
            fileChooser.setSelectedFile(null);
            fileChooser.showSaveDialog(null);
            if(fileChooser.getSelectedFile() != null){
                try {
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(fileChooser.getSelectedFile().toPath()), StandardCharsets.UTF_8));
                    out.write(MarkDownExporter.toMarkDown(EventRecorder.model));
                    out.close();
                    prefs.put("DEFAULT_SAVE_DIRECTORY", fileChooser.getSelectedFile().getParent());
                    return true;
                } catch (IOException iOE) {
                    JOptionPane.showMessageDialog(null, iOE.getMessage(), "Error while saving!", JOptionPane.WARNING_MESSAGE);
                    askAgain = true;
                }
            }
        }

        return false;
    }

    /**
     * Sets the default UI Font.
     */

    @SuppressWarnings("rawtypes")
    public static void setUIFont(FontUIResource f) {
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource)
                UIManager.put(key, f);
        }
    }

    public void actionWasExecuted(Action action) {
        undoButton.setEnabled(EventRecorder.history.undoPossible());
        redoButton.setEnabled(EventRecorder.history.redoPossible());

        if(action.getClass().getSimpleName().startsWith("Entry")){
            entryTable.entryActionWasExecuted(action);
        } else if(action instanceof TitleChangeAction){
            if(!titleField.getText().equals(EventRecorder.model.title)){
                titleField.setText(EventRecorder.model.title);
                titleField.requestFocus();
            } else if(!additionalField.getText().equals(EventRecorder.model.additionalInfo)){
                additionalField.setText(EventRecorder.model.additionalInfo);
                additionalField.requestFocus();
            }
        }
    }
}
