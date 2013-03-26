package nl.napauleon.sabber;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

@RunWith(CustomTestRunner.class)
public class MainActivityTest {

    private MainActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = new MainActivity();
    }

    @Test
    public void testOnCreate() throws Exception {
        activity.onCreate(null);
        assertNotNull(activity.findViewById(R.id.pager));
    }
}
