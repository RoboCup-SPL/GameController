package eventrecorder.action;

import eventrecorder.EventRecorder;

/**
 * Saves all executed actions for undo- and redo-functionality.
 *
 * @author Andre Muehlenbrock
 */

public class ActionHistory {
    public final CircularLiFoBuffer<Action> actions  = new CircularLiFoBuffer<>(1000);

    public boolean execute(Action action){
        if(action.executeAction()){

            if(action.shouldBeAddedToHistory())
                actions.push(action);

            notifyGUI(action);
            System.out.println("Execute: "+action.getClass().getSimpleName());
            return true;
        }

        return false;
    }

    /**
     * Undoes the last action.
     *
     * @return true if it worked.
     */

    public boolean undo(){
        if(actions.isEmpty())
            return false;

        Action action = actions.pop();

        System.out.println("Undo: "+action.getClass().getSimpleName());
        if(action.undoAction()){
            notifyGUI(action);
            return true;
        }

        return false;
    }

    /**
     * Redo the last undone action.
     *
     * @return true if it worked.
     */

    public boolean redo(){
        if(!actions.hasNext())
            return false;


        Action action = actions.popForward();
        System.out.println("Redo: "+action.getClass().getSimpleName());
        if(action.executeAction()){
            notifyGUI(action);
            return true;
        }

        return false;
    }


    /**
     * Notify the GUI that a LogEntry is changed.
     *
     * @param a
     */

    private void notifyGUI(Action a){
        EventRecorder.gui.actionWasExecuted(a);
    }

    public boolean undoPossible(){
        return !actions.isEmpty();
    }

    public boolean redoPossible(){
        return actions.hasNext();
    }
}
