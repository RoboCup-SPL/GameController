package analyzer;

import common.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.LinkedList;

/**
 * @author: Michel Bartsch
 * 
 */
public class LogInfo
{
    private final static String CHARSET = "UTF-8";
    private final static String NOT_A_REAL_TEAM = "Invisibles";
    private final static int MIN_DURATION = 18*60;
    private final static int NUM_OF_INFO_ENTRIES = 6;
    
    public String version;
    public String league;
    public String[] team = new String[2];
    public Date start;
    public int duration;
    public LinkedList<String> lines = new LinkedList<String>();
    public String parseErrors = "";
    public LogStatistic[] statistic = new LogStatistic[2];
    
    public LogInfo(File log)
    {
        BufferedReader br = null;
        try {
            InputStream inStream = new FileInputStream(log);
            br = new BufferedReader(
                    new InputStreamReader(inStream, CHARSET));
            String currentLine;
            while((currentLine = br.readLine()) != null) {
                lines.add(currentLine);
            }
        } catch(IOException e) {
            Log.error("cannot load "+log);
        }
        finally {
            if(br != null) {
                try {
                    br.close();
                } catch(Exception e) {}
            }
        }
        Parser.info(this);
    }
    
    public boolean realGame()
    {
        return version.equals(controller.Main.version)
                && !team[0].equals(NOT_A_REAL_TEAM)
                && !team[1].equals(NOT_A_REAL_TEAM)
                && duration > MIN_DURATION;
    }
    
    public String getInfo()
    {
        return version + "\n"
                + team[0] + " vs " + team[1] + "\n"
                + start + " starting\n"
                + duration + " secs\n"
                + (lines.size()-NUM_OF_INFO_ENTRIES) + " actions\n"
                + parseErrors;
    }
    
    @Override
    public String toString()
    {
        return team[0] + " vs " + team[1];
    }
}