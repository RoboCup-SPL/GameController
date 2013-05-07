package controller.ui;

import controller.action.ActionBoard;
import controller.action.GCAction;
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
            
            case KeyEvent.VK_1: event = ActionBoard.robot[0][0]; break;
            case KeyEvent.VK_2: event = ActionBoard.robot[0][1]; break;
            case KeyEvent.VK_3: event = ActionBoard.robot[0][2]; break;
            case KeyEvent.VK_4: event = ActionBoard.robot[0][3]; break;
            case KeyEvent.VK_5: event = ActionBoard.robot[0][4]; break;
            case KeyEvent.VK_6: event = ActionBoard.robot[1][0]; break;
            case KeyEvent.VK_7: event = ActionBoard.robot[1][1]; break;
            case KeyEvent.VK_8: event = ActionBoard.robot[1][2]; break;
            case KeyEvent.VK_9: event = ActionBoard.robot[1][3]; break;
            case KeyEvent.VK_0: event = ActionBoard.robot[0][4]; break;
            
            case KeyEvent.VK_Q: event = ActionBoard.goalInc[0]; break;
            case KeyEvent.VK_I: event = ActionBoard.goalInc[1]; break;
            case KeyEvent.VK_A: event = ActionBoard.out[0]; break;
            case KeyEvent.VK_K: event = ActionBoard.out[1]; break;
            case KeyEvent.VK_Y: event = ActionBoard.timeOut[0]; break;
            case KeyEvent.VK_COMMA: event = ActionBoard.timeOut[1]; break;
            
            case KeyEvent.VK_E: event = ActionBoard.initial; break;
            case KeyEvent.VK_R: event = ActionBoard.ready; break;
            case KeyEvent.VK_T: event = ActionBoard.set; break;
            case KeyEvent.VK_Z: event = ActionBoard.play; break;
            
            case KeyEvent.VK_D: event = ActionBoard.pushing; break;
            case KeyEvent.VK_F: event = ActionBoard.leaving; break;
            case KeyEvent.VK_G: event = ActionBoard.fallen; break;
            case KeyEvent.VK_H: event = ActionBoard.inactive; break;
            case KeyEvent.VK_C: event = ActionBoard.defender; break;
            case KeyEvent.VK_V: event = ActionBoard.holding; break;
            case KeyEvent.VK_B: event = ActionBoard.hands; break;
            case KeyEvent.VK_N: event = ActionBoard.pickUp; break;
        }
        
        if(event != null) {
            event.actionPerformed(null);
        }
    }
}