package data;

import data.hl.HL;
import data.hl.HLAdult;
import data.hl.HLTeen;
import data.spl.SPL;
import data.spl.SPLDropIn;
import org.junit.Rule;

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


}
