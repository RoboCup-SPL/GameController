package controller.ui;

import controller.EventHandler;
import controller.action.ActionBoard;
import controller.action.GCAction;
import data.GameControlData;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;


/**
 * @author Michel Bartsch
 * 
 * This class listens to the keyboard. It does not depend on the GUI.
 */
public class KeyboardListener implements KeyEventDispatcher
{
    /** The key that is actually pressed, 0 if no key is pressed. */
    private int pressing = 0;
    
    /**
     * Creates a new KeyboardListener and sets himself to listening.
     */
    public KeyboardListener() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
    }
    
    /**
     * This is called every time a key is pressed or released.
     * 
     * @param e     The key that has been pressed or released.
     * 
     * @return If false, the key will be consumed.
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_RELEASED) {
            pressing = 0;
        } else if (e.getID() == KeyEvent.KEY_PRESSED) {
            int key = e.getKeyCode();
        
            if ((key == 0) || (key == pressing)) {
                return false;
            }
            pressing = key;
            return pressed(key);
        }
        
        return false;
    }
    
    /**
     * This is called once every time a key is pressed. It is called once and
     * not as long as the key is pressed.
     * You can easily set the keys for each action here. The actions are
     * to be performed via the actionPerformed method as they are in the
     * GUI.
     * 
     * @param key  The key that has just been pressed.
     * 
     * @return If false, the key was used and should be consumed.
     */
    private boolean pressed(int key)
    {
        GCAction event = null;
        
        switch (key) {
            case KeyEvent.VK_ESCAPE: event = ActionBoard.quit; break;
            case KeyEvent.VK_DELETE: event = ActionBoard.testmode; break;
            case KeyEvent.VK_BACK_SPACE: event = ActionBoard.undo[1]; break;
            default:
                switch (key) {
                    // TODO: This Key-Mappings do not work properly with 10 Colors
                    case KeyEvent.VK_B:
                        for (int i = 0; i < ActionBoard.kickIn.length; ++i) {
                            if (EventHandler.getInstance().data.team[i].teamColor == GameControlData.TEAM_BLUE) {
                                event = ActionBoard.kickIn[i];
                            }
                        }
                        break;
                    case KeyEvent.VK_R:
                        for (int i = 0; i < ActionBoard.kickIn.length; ++i) {
                            if (EventHandler.getInstance().data.team[i].teamColor == GameControlData.TEAM_RED) {
                                event = ActionBoard.kickIn[i];
                            }
                        }
                        break;
                    case KeyEvent.VK_Y:
                        for (int i = 0; i < ActionBoard.kickIn.length; ++i) {
                            if (EventHandler.getInstance().data.team[i].teamColor == GameControlData.TEAM_YELLOW) {
                                event = ActionBoard.kickIn[i];
                            }
                        }
                        break;
                    case KeyEvent.VK_K:
                        for (int i = 0; i < ActionBoard.kickIn.length; ++i) {
                            if (EventHandler.getInstance().data.team[i].teamColor == GameControlData.TEAM_BLACK) {
                                event = ActionBoard.kickIn[i];
                            }
                        }
                        break;

                    case KeyEvent.VK_P: event = ActionBoard.pushing; break;
                    case KeyEvent.VK_L: event = ActionBoard.leaving; break;
                    case KeyEvent.VK_I: event = ActionBoard.inactive; break;
                    case KeyEvent.VK_D: event = ActionBoard.defender; break;
                    case KeyEvent.VK_G: event = ActionBoard.kickOffGoal; break;
                    case KeyEvent.VK_O: event = ActionBoard.ballContact; break;
                    case KeyEvent.VK_U: event = ActionBoard.pickUp; break;
                    case KeyEvent.VK_F: event = ActionBoard.foul; break;
                }
        }
        
        if (event != null) {
            event.actionPerformed(null);
            return true;
        }
        return false;
    }
}
