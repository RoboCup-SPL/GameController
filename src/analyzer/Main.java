package analyzer;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;

/**
 * @author: Michel Bartsch
 * 
 * The log-analyzer-programm starts in this class.
 * The main components are initialised here.
 */
public class Main
{
    public final static String PATH = "logs";
    public final static String PATH_DROPPED = "logs/dropped";
    
    public static LinkedList<LogInfo> logs;
    public static File stats;
    public static FileWriter writer;
    
    /**
     * The programm starts here.
     * 
     * @param args  This is ignored.
     */
    public static void main(String[] args)
    {
        load();
        GUI gui = new GUI();
    }
    
    public static void load()
    {
        logs = new LinkedList<LogInfo>();
        File dir = new File(PATH);
        File[] files = dir.listFiles();
        for(File file: files) {
            if(file.isFile()) {
                logs.add(new LogInfo(file));
            }
        }
    }
}