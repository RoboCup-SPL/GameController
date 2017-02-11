import data.communication.GameControlData;
import data.hl.HL;
import data.states.GamePreparationData;

import data.states.PrepTeam;
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
        assertEquals(0, gpd.getFirstTeam().getTeamNumber());
        assertEquals(0, gpd.getSecondTeam().getTeamNumber());

        assertEquals(GameControlData.TEAM_WHITE, gpd.getFirstTeam().getTeamColor());
        assertEquals(GameControlData.TEAM_WHITE, gpd.getSecondTeam().getTeamColor());
    }

    @Test
    public void test_same_team_numbers_cannot_start_game() {
        GamePreparationData gpd = new GamePreparationData();
        gpd.getFirstTeam().setTeamNumber(14);
        gpd.getSecondTeam().setTeamNumber(14);

        gpd.getFirstTeam().setTeamColor(GameControlData.TEAM_RED);
        gpd.getSecondTeam().setTeamColor(GameControlData.TEAM_BROWN);

        assertFalse(gpd.canStart());
    }

    @Test
    public void test_same_team_colors_cannot_start_game() {
        GamePreparationData gpd = new GamePreparationData();
        gpd.getFirstTeam().setTeamNumber(15);
        gpd.getSecondTeam().setTeamNumber(14);

        gpd.getFirstTeam().setTeamColor(GameControlData.TEAM_RED);
        gpd.getSecondTeam().setTeamColor(GameControlData.TEAM_RED);

        assertFalse(gpd.canStart());
    }

    @Test
    public void test_game_can_start_with_different_team_properties() {
        GamePreparationData gpd = new GamePreparationData();
        gpd.getFirstTeam().setTeamNumber(15);
        gpd.getSecondTeam().setTeamNumber(14);

        gpd.getFirstTeam().setTeamColor(GameControlData.TEAM_RED);
        gpd.getSecondTeam().setTeamColor(GameControlData.TEAM_BLUE);

        assertTrue(gpd.canStart());
    }

    @Test
    public void test_team_refresh_works() {
        GamePreparationData gpd = new GamePreparationData();
        ArrayList<PrepTeam> al = gpd.getPreparedTeams();
        assertEquals(al.size(), 5);
    }

    @Test
    public void test_switching_leagues_work() {
        GamePreparationData gpd = new GamePreparationData();
        ArrayList<PrepTeam> al = gpd.getPreparedTeams();
        assertEquals(al.size(), 5);

        gpd.switchRules(new HL());

        ArrayList<PrepTeam> al2 = gpd.getPreparedTeams();
        assertEquals(al2.size(), 9);
    }

}