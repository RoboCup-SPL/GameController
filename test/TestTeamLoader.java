import data.hl.HL;
import data.hl.HLTeen;
import data.spl.SPL;
import data.teams.TeamLoadInfo;
import data.teams.TeamsLoader;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by rkessler on 2017-02-11.
 */
public class TestTeamLoader {

    @BeforeClass
    public static void setUp(){
        System.setProperty("CONFIG_ROOT", "test_resources/");
    }


    @Test
    public void test_spl_loads_correct_size() {
        TeamsLoader tl = TeamsLoader.getInstance();

        SPL spl_rules = new SPL();

        ArrayList<String> names = tl.getNames(spl_rules.leagueName, true);

        assertEquals(5, names.size());
    }

    @Test
    public void test_hl_kid_loads_correct_size() {
        TeamsLoader tl = TeamsLoader.getInstance();

        HL hl_rules = new HL();

        ArrayList<String> names = tl.getNames(hl_rules.leagueName, true);

        assertEquals(9, names.size());
    }

    @Test
    public void test_hl_teen_image_is_loaded() {
        TeamsLoader tl = TeamsLoader.getInstance();

        HLTeen hl_rules = new HLTeen();

        ArrayList<String> names = tl.getNames(hl_rules.leagueName, true);
        assertEquals(1, names.size());

        ArrayList<TeamLoadInfo> tli_array = tl.getTeamLoadInfoList(hl_rules.leagueName);

        TeamLoadInfo tli = tli_array.get(0);

        assertEquals(tli.name, "NimbRo TeenSize");
        assertEquals(tli.identifier, 33);
        assertEquals(tli.colors.length, 2);
        assertEquals(tli.colors[0], "yellow");
        assertEquals(tli.colors[1], "black");
        assertTrue(tli.icon != null);
    }
}
