package tmc.CrafterTracker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CrafterTrackerPluginTest {

    private CrafterTrackerPlugin crafterTrackerPlugin;

    @Before
    public void setUp() throws Exception {
        crafterTrackerPlugin = new CrafterTrackerPlugin();
        crafterTrackerPlugin.onEnable();
    }

    @After
    public void tearDown() throws Exception {
        crafterTrackerPlugin.onDisable();
    }

    @Test
    public void doSomething() {
        assert true;
    }
}
