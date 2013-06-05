package controller.ui;

import controller.EventHandler;
import controller.action.ActionBoard;
import controller.action.GCAction;
import data.GameControlData;
import data.HL;
import data.Rules;
import data.SPL;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;


/**
 * @author: Michel Bartsch
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
        if(e.getID() == KeyEvent.KEY_RELEASED) {
            pressing = 0;
        } else if(e.getID() == KeyEvent.KEY_PRESSED) {
            int key = e.getKeyCode();
        
            if( (key == 0) || (key == pressing) ) {
                return false;
            }
            pressing = key;
            pressed(key);
        }
        
        return true;
    }
    
    /**
     * This is called once every time a key is pressed. It is called once and
     * not as long as the key is pressed.
     * You can easily set the keys for each action here. The actions are
     * to be performed via the actionPerformed method as they are in the
     * GUI.
     * 
     * @param key  The key that has just been pressed.
     */
    private void pressed(int key)
    {
        GCAction event = null;
        
        switch(key) {
            case KeyEvent.VK_ESCAPE: event = ActionBoard.quit; break;
            case KeyEvent.VK_DELETE: event = ActionBoard.testmode; break;
            case KeyEvent.VK_BACK_SPACE: event = ActionBoard.undo[1]; break;
        }
        if(Rules.league instanceof SPL) {
            switch(key) {
                case KeyEvent.VK_B: event = ActionBoard.out[EventHandler.getInstance().data.team[0].teamColor == GameControlData.TEAM_BLUE ? 0 : 1]; break;
                case KeyEvent.VK_R: event = ActionBoard.out[EventHandler.getInstance().data.team[0].teamColor == GameControlData.TEAM_RED ? 0 : 1]; break;
                
                case KeyEvent.VK_P: event = ActionBoard.pushing; break;
                case KeyEvent.VK_L: event = ActionBoard.leaving; break;
                case KeyEvent.VK_F: event = ActionBoard.fallen; break;
                case KeyEvent.VK_I: event = ActionBoard.inactive; break;
                case KeyEvent.VK_D: event = ActionBoard.defender; break;
                case KeyEvent.VK_O: event = ActionBoard.holding; break;
                case KeyEvent.VK_H: event = ActionBoard.hands; break;
                case KeyEvent.VK_U: event = ActionBoard.pickUp; break;
            }
        } else if(Rules.league instanceof HL) {
            switch(key) {
                case KeyEvent.VK_C: event = ActionBoard.out[EventHandler.getInstance().data.team[0].teamColor == GameControlData.TEAM_BLUE ? 0 : 1]; break;
                case KeyEvent.VK_M: event = ActionBoard.out[EventHandler.getInstance().data.team[0].teamColor == GameControlData.TEAM_RED ? 0 : 1]; break;
                
                case KeyEvent.VK_P: event = ActionBoard.pushing; break;
                case KeyEvent.VK_D: event = ActionBoard.defense; break;
                case KeyEvent.VK_B: event = ActionBoard.ballManipulation; break;
                case KeyEvent.VK_R: event = ActionBoard.pickUpHL; break;
                case KeyEvent.VK_A: event = ActionBoard.attack; break;
            }
        }
        
        if(event != null) {
            event.actionPerformed(null);
        }
    }
}