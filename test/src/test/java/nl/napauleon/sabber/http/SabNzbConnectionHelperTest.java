package nl.napauleon.sabber.http;

import static org.junit.Assert.assertEquals;
import nl.napauleon.sabber.Constants;
import nl.napauleon.sabber.CustomTestRunner;
import nl.napauleon.sabber.MainActivity;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.SharedPreferences;

import com.xtremelabs.robolectric.shadows.ShadowPreferenceManager;

@RunWith(CustomTestRunner.class)
public class SabNzbConnectionHelperTest {

    public static final String HOSTNAME = "localhost";
    public static final String PORT = "8080";
    public static final String APIKEY = "abc";
    public static final String EXPECTED_DEFAULT_CONNECTION_STRING = String.format("http://%s:%s/api?output=json&apikey=%s", HOSTNAME, PORT, APIKEY);
    SabNzbConnectionHelper helper;

    @Before
    public void setUp() throws Exception {
        SharedPreferences preferences = ShadowPreferenceManager.getDefaultSharedPreferences(new MainActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.HOSTNAME_PREF, HOSTNAME);
        editor.putString(Constants.PORT_PREF, PORT);
        editor.putString(Constants.APIKEY_PREF, APIKEY);
        editor.commit();
        helper = new SabNzbConnectionHelper(preferences);
    }

    @Test
    public void testCreateHistoryConnectionString() throws Exception {
        String expected = EXPECTED_DEFAULT_CONNECTION_STRING + "&mode=history&limit=100";

        String result = helper.createHistoryConnectionString();

        assertEquals(expected, result);
    }

    @Test
    public void testCreateQueueConnectionString() throws Exception {
        String expected = EXPECTED_DEFAULT_CONNECTION_STRING + "&mode=queue";

        String result = helper.createQueueConnectionString();

        assertEquals(expected, result);
    }

    @Test
    public void testCreateDeleteItemConnectionString() throws Exception {
        String itemId = RandomStringUtils.random(10);
        String expected = EXPECTED_DEFAULT_CONNECTION_STRING + "&mode=queue&name=delete&value=" + itemId;

        String result = helper.createDeleteItemConnectionString(itemId);

        assertEquals(expected, result);
    }

    @Test
    public void testCreateChangeCategoryConnectionString() throws Exception {
        String itemId = RandomStringUtils.random(10);
        String category = RandomStringUtils.random(10);
        String expected = EXPECTED_DEFAULT_CONNECTION_STRING + "&mode=change_cat&value=" + itemId + "&value2=" + category;

        String result = helper.createChangeCategoryConnectionString(itemId, category);

        assertEquals(expected, result);
    }

    @Test
    public void testCreateResumeConnection() throws Exception {
        String expected = EXPECTED_DEFAULT_CONNECTION_STRING + "&mode=resume";

        String result = helper.createResumeConnection();

        assertEquals(expected, result);
    }

    @Test
    public void testCreatePauseConnection() throws Exception {
        String expected = EXPECTED_DEFAULT_CONNECTION_STRING + "&mode=pause";

        String result = helper.createPauseConnection();

        assertEquals(expected, result);
    }
}
