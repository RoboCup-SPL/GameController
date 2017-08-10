package eventrecorder;

import java.net.SocketException;
import java.text.SimpleDateFormat;

import javax.swing.JOptionPane;

import data.GameControlData;
import data.PlayerInfo;
import data.TeamInfo;
import eventrecorder.action.EntryCreateAction;
import eventrecorder.gui.MainFrame;
import teamcomm.data.event.GameControlDataEvent;
import teamcomm.data.event.GameControlDataEventListener;
import teamcomm.data.event.GameControlDataTimeoutEvent;
import teamcomm.net.GameControlDataReceiver;

/**
 * This is a little tool to record events while a game takes place.
 * 
 * @author Andre Muehlenbrock
 */

public class EventRecorder {
    public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("dd.MM.YY - HH:mm 'Uhr'");
    public static final SimpleDateFormat SECONDS_FORMAT = new SimpleDateFormat("mm:ss");
    public static final String[] GAME_STATE_NAMES = new String[]{"Initial", "Ready", "Set", "Playing", "Finished"};
    
    public static final int GAMECONTROLLER_TIMEOUT = 3000; /**< Timeout in ms when the manual play/stop/reset buttons should be usable again */
    
    public static DataModel model;
    public static ActionHistory history;
    public static MainFrame gui;
    
    public static GameControlDataReceiver gcDataReceiver;
    
    private static GameControlData lastData = null;
    private static TeamInfo[] lastTeamData = null;
    private static byte lastGameState = -1;
    
    private static boolean[] logPenalty = new boolean[16];
    
    public TeamInfo[] getLastTeamData() {
    	return lastTeamData;
    }
    
    public static void main(String args[]){    	
        model = new DataModel();
        history = new ActionHistory();
        gui = new MainFrame();

        // Initialize listener for GameController messages
        try {
            gcDataReceiver = new GameControlDataReceiver(true);
        } catch (SocketException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error while setting up GameController listener.",
                    "SocketException",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

        // Add this as GameControlDataListener:
        gcDataReceiver.addListener(new GameControlDataEventListener(){

            @Override
            public void gameControlDataChanged(GameControlDataEvent e) {
                EventRecorder.updateGameData(e.data);
            }

            @Override
            public void gameControlDataTimeout(GameControlDataTimeoutEvent e) {
                EventRecorder.updateGameData(null);
            }
            
        });
        
        // Start listening:
        gcDataReceiver.start();
    }

    
    public static void updateGameData(GameControlData data){
        if(data != lastData && data != null){
            // Deactivate manually running:
            model.isManuallyRunning = false;
            
            // Set current time:
            model.currentTime = data.secsRemaining;
            
            // If Gamestate is changed, add a LogEntry:
            if(lastGameState != data.gameState){
                lastGameState = data.gameState;
                
                String gameStateString = GAME_STATE_NAMES[data.gameState];
                if(data.gameState == GameControlData.STATE_INITIAL){
                    gameStateString += " ( "+ (data.firstHalf == GameControlData.C_TRUE? "First Half":"Second Half")+" )";
                }
                

                // Insert before empty logEntries:
            	int insertPlace = EventRecorder.model.logEntries.size();
            	
            	while(insertPlace > 0 && "".equals(EventRecorder.model.logEntries.get(insertPlace-1).text))
            		--insertPlace;
            	
                history.execute(new EntryCreateAction(new LogEntry(gameStateString,SECONDS_FORMAT.format(data.secsRemaining*1000),LogType.GameState), insertPlace, false));
            }

            // Check for changed penalties:
            if(lastTeamData != null) {
            	for(int i=0; i<data.team.length && i<lastTeamData.length; ++i) {
            		for(int p=0; p<data.team[i].player.length && p<lastTeamData[i].player.length; ++p) {
            			if(data.team[i].player[p].penalty != lastTeamData[i].player[p].penalty
            					&& logPenalty[data.team[i].player[p].penalty]) {
            				
            				String penaltyString = data.team[i].player[p].penalty == 0 ? "Back In Game" : 
            					capitalize(PlayerInfo.getPenaltyName(data.team[i].player[p].penalty));
            				
            				String totalString = data.team[i].getTeamColorName().toUpperCase()+" "+(p+1)+": "+
            						penaltyString;
            				
            				// Insert before empty logEntries:
                        	int insertPlace = EventRecorder.model.logEntries.size();
                        	
                        	while(insertPlace > 0 && "".equals(EventRecorder.model.logEntries.get(insertPlace-1).text))
                        		--insertPlace;
                        	
                            history.execute(new EntryCreateAction(new LogEntry(totalString,SECONDS_FORMAT.format(data.secsRemaining*1000),LogType.PlayerState), insertPlace, false));
            				
            			}
            		}
            	}
            }

            lastTeamData = data.team;
            
            // Save current timestamp:
            model.lastGameControllerInfo = System.currentTimeMillis();
        }
        
        lastData = data;
    }
    
    public static String capitalize(String string) {
        StringBuffer result = new StringBuffer();

        String[] array = string.split(" ");
        for (String s : array) {
            char[] charArray = s.trim().toCharArray();
            charArray[0] = Character.toUpperCase(charArray[0]);
            s = new String(charArray);
            result.append(s).append(" ");
        }
        
        return result.toString().trim();
    }
    
    public static void cleanExit(){
        gcDataReceiver.interrupt();
        
        // Try to join receiver threads
        try {
            gcDataReceiver.join(1000);
        } catch (InterruptedException ex) {
            
        }
        
        System.exit(0);
    }

	public static void setLogPenalty(int i, boolean log) {
		logPenalty[i] = log;
	}
}
