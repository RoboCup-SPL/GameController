package data;

import data.communication.GameControlData;
import data.hl.HL;
import data.hl.HLAdult;
import data.hl.HLTeen;
import data.spl.SPL;
import data.spl.SPLDropIn;
import org.junit.Rule;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by rkessler on 2017-02-11.
 */
public class Helper {

    public static boolean isValidRule(Rules rules){
        boolean result = rules instanceof SPL;
        result |= rules instanceof SPLDropIn;
        result |= rules instanceof HL;
        result |= rules instanceof HLTeen;
        result |= rules instanceof HLAdult;
        return result;
    }

    private static String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }


    public static Color getColorByString(Rules activeRules, String colorName){
        assert activeRules.teamColor.length == activeRules.teamColorName.length;
        for (int idx=0; idx < activeRules.teamColorName.length; idx++){

            if (activeRules.teamColorName[idx].equals(capitalize(colorName))){
                return activeRules.teamColor[idx];
            }
        }
        return null;
    }
}
