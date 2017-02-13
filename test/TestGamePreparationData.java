import data.communication.GameControlData;
import data.hl.HL;
import data.states.GamePreparationData;

import data.states.PrepTeam;
import data.teams.TeamLoadInfo;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class TestGamePreparationData {

    @BeforeClass
    public static void setUp(){
        System.setProperty("CONFIG_ROOT", "test_resources/");
    }

    @Test
    public void test_fresh_game_prep_data() {
        GamePreparationData gpd = new GamePreparationData();
        assertEquals(0, gpd.getFirstTeam().getTeamInfo().identifier);
        assertEquals(0, gpd.getSecondTeam().getTeamInfo().identifier);

        assertEquals(GameControlData.TEAM_BLUE, gpd.getFirstTeam().getTeamColorAsByte());
        assertEquals(GameControlData.TEAM_BLUE, gpd.getSecondTeam().getTeamColorAsByte());
    }

    @Test
    public void test_same_team_numbers_cannot_start_game() {
        GamePreparationData gpd = new GamePreparationData();
        gpd.chooseTeam(0, gpd.getAvailableTeams().get(3));
        gpd.chooseTeam(1, gpd.getAvailableTeams().get(3));

        gpd.getFirstTeam().setTeamColor("Red");
        gpd.getSecondTeam().setTeamColor("Blue");

        String problem = gpd.canStart();
        assertEquals("Cannot start with both teams being the same!", problem);
    }

    @Test
    public void test_same_team_colors_cannot_start_game() {
        GamePreparationData gpd = new GamePreparationData();
        gpd.chooseTeam(0, gpd.getAvailableTeams().get(2));
        gpd.chooseTeam(1, gpd.getAvailableTeams().get(3));

        gpd.getFirstTeam().setTeamColor("Red");
        gpd.getSecondTeam().setTeamColor("Red");

        String problem = gpd.canStart();
        assertEquals("Cannot start when TeamColors are the same", problem);
    }

    @Test
    public void test_game_can_start_with_different_team_properties() {
        GamePreparationData gpd = new GamePreparationData();
        gpd.chooseTeam(0, gpd.getAvailableTeams().get(2));
        gpd.chooseTeam(1, gpd.getAvailableTeams().get(3));

        gpd.getFirstTeam().setTeamColor("Red");
        gpd.getSecondTeam().setTeamColor("Blue");

        String problem = gpd.canStart();
        assertEquals(null, problem);
    }

    @Test
    public void test_team_refresh_works() {
        GamePreparationData gpd = new GamePreparationData();
        ArrayList<TeamLoadInfo> al = gpd.getAvailableTeams();
        assertEquals(al.size(), 5);
    }

    @Test
    public void test_switching_leagues_work() {
        GamePreparationData gpd = new GamePreparationData();
        ArrayList<TeamLoadInfo> al = gpd.getAvailableTeams();
        assertEquals(al.size(), 5);

        gpd.switchRules(new HL());

        ArrayList<TeamLoadInfo> al2 = gpd.getAvailableTeams();
        assertEquals(al2.size(), 9);
    }

}