package controller;

import controller.action.ActionBoard;


/**
 *
 * @author Michel Bartsch
 *
 * This class is no thread, it continous the main-thread and fires the action ClockTick.
 */
public class Clock
{
    /**
     * The time in millis to sleep before next ClockTick. This does not mean
     * it fires after this time, it will allways take some more millis depending
     * on the performance.
     */
    public static final int HEARTBEAT = 500; // 2Hz
    
    
    /**
     * Lets the Clock start to run.
     */
    public void start()
    {   
        while (true)
        {
            ActionBoard.clock.actionPerformed(null);
            
            try {
                Thread.sleep(HEARTBEAT);
            } catch (InterruptedException e) {}
        }
    }
}