package analyzer;

/**
 * @author: Michel Bartsch
 * 
 * The log-analyzer-programm starts in this class.
 * The main components are initialised here.
 */
public class Main
{        
    
    /**
     * The programm starts here.
     * 
     * @param args  This is ignored.
     */
    public static void main(String[] args)
    {
        Games games = new Games();
        GUI gui = new GUI(games);
    }
}