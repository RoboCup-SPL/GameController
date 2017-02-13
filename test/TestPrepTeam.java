import data.communication.GameControlData;
import data.states.GamePreparationData;
import data.states.PrepTeam;
import data.teams.TeamLoadInfo;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;

/**
 * Created by rkessler on 2017-02-11.
 */
public class TestPrepTeam {

    @BeforeClass
    public static void setUp(){
        System.setProperty("CONFIG_ROOT", "test_resources/");
    }

    @Test
    public void test_prep_team_cycle() {
        GamePreparationData gpd = new GamePreparationData();

        TeamLoadInfo tli = gpd.getAvailableTeams().get(1);
        PrepTeam prepteam = new PrepTeam(tli);

        assertEquals(tli.colors[0], "white");
        assertEquals(tli.colors[1], "orange");
        assertEquals(prepteam.getTeamColor(), "white");

        prepteam.cycleColours();
        assertEquals(prepteam.getTeamColor(), "orange");

        prepteam.cycleColours();
        assertEquals(prepteam.getTeamColor(), "red");


        prepteam.cycleColours();
        assertEquals(prepteam.getTeamColor(), "white");
    }
}