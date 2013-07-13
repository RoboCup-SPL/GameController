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
    
    public File file;
    public String version;
    public String league;
    public String[] team = new String[2];
    public Date start;
    public int duration;
    public LinkedList<String> lines = new LinkedList<String>();
    public String parseErrors = "";
    
    public LogInfo(File log)
    {
        file = log;
        BufferedReader br = null;
        try {
            InputStream inStream = new FileInputStream(log);
            br = new BufferedReader(new InputStreamReader(inStream, CHARSET));
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
    
    public boolean isRealLog()
    {
        return isRealVersion()
                && isRealTeamOne()
                && isRealTeamTwo()
                && isRealDuration();
    }
    
    private boolean isRealVersion()
    {
        return version == null ? false : version.equals(controller.Main.version);
    }
    
    private boolean isRealTeamOne()
    {
        return team[0] == null ? false : !team[0].equals(NOT_A_REAL_TEAM);
    }
    
    private boolean isRealTeamTwo()
    {
        return team[1] == null ? false : !team[1].equals(NOT_A_REAL_TEAM);
    }
    
    private boolean isRealDuration()
    {
        return duration > MIN_DURATION;
    }
    
    public String getInfo()
    {
        return GUI.HTML
                + (isRealVersion() ? version : GUI.HTML_RED + version + GUI.HTML_END) + GUI.HTML_LF
                + (isRealTeamOne() ? team[0] : GUI.HTML_RED + team[0] + GUI.HTML_END)
                + " vs "
                + (isRealTeamTwo() ? team[1] : GUI.HTML_RED + team[1] + GUI.HTML_END) + GUI.HTML_LF
                + (start != null ? start : GUI.HTML_RED + start + GUI.HTML_END) + " starting" + GUI.HTML_LF
                + (isRealDuration() ? duration : GUI.HTML_RED + duration + GUI.HTML_END) + " seconds" + GUI.HTML_LF
                + (lines.size()-NUM_OF_INFO_ENTRIES) + " actions" + GUI.HTML_LF
                + GUI.HTML_RED + parseErrors;
    }
    
    @Override
    public String toString()
    {
        return GUI.HTML + (isRealLog() ? team[0] + " vs " + team[1] : GUI.HTML_RED + team[0] + " vs " + team[1] + GUI.HTML_END);
    }
}