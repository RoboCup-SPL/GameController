import data.Rules;
import data.spl.SPL;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestRules {

    @Test
    public void test_rules_are_equal() {
        SPL spl_rules1 = new SPL();
        SPL spl_rules2 = new SPL();

        assertTrue(spl_rules1.equals(spl_rules2));
    }

    @Test
    public void test_rules_are_equal_with_casting() {
        Rules spl_rules1 = new SPL();
        Rules spl_rules2 = new SPL();

        assertTrue(spl_rules1.equals(spl_rules2));
        assertTrue(spl_rules2.equals(spl_rules1));
    }

}
