import data.GameControlData;
import data.GamePreparationData;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class TestGamePreparationData {

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
    public void test_get_prep_team_by_index_works() {
        GamePreparationData gpd = new GamePreparationData();

        assertEquals(gpd.getFirstTeam(), gpd.getPrepTeam(0));
        assertEquals(gpd.getSecondTeam(), gpd.getPrepTeam(1));
    }

}