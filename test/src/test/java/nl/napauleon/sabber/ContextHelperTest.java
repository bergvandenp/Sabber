package nl.napauleon.sabber;

import android.content.SharedPreferences;
import com.xtremelabs.robolectric.shadows.ShadowPreferenceManager;
import com.xtremelabs.robolectric.shadows.ShadowToast;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(CustomTestRunner.class)
public class ContextHelperTest {

    private ContextHelper helper;
    private MainActivity context;

    @Before
    public void setUp() throws Exception {
        helper = new ContextHelper();
        context = new MainActivity();
    }

    @Test
    public void testShowConnectionTimeoutAlert() {
        helper.showConnectionTimeoutAlert(context);
        ShadowToast.showedToast(Constants.MESSAGE_CONNECTION_TIMEOUT);
    }

    @Test
    public void testUpdateLastPollingEvent() {
        long beforeTime = System.currentTimeMillis();
        helper.updateLastPollingEvent(context, System.currentTimeMillis());
        Long lastPollingPref = ShadowPreferenceManager.getDefaultSharedPreferences(context).getLong(Constants.LAST_POLLING_EVENT_PREF, 0L);
        long afterTime = System.currentTimeMillis();
        assertTrue(lastPollingPref >= beforeTime);
        assertTrue(lastPollingPref <= afterTime);
    }

    @Test
    public void testIsNotificationsEnabled_NotSet() throws Exception {
        boolean result = helper.isNotificationsEnabled(context);
        assertFalse(result);
    }

    @Test
    public void testIsNotificationsEnabled_False() throws Exception {
        SharedPreferences.Editor editor = ShadowPreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(Constants.NOTIFICATIONS_PREF, false);
        editor.commit();
        boolean result = helper.isNotificationsEnabled(context);
        assertFalse(result);
    }

    @Test
    public void testIsNotificationsEnabled() throws Exception {
        SharedPreferences.Editor editor = ShadowPreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(Constants.NOTIFICATIONS_PREF, true);
        editor.commit();
        boolean result = helper.isNotificationsEnabled(context);
        assertTrue(result);
    }

    @Test
    public void testCheckAndGetSettings_AllEmpty() throws Exception {
        helper.checkAndGetSettings(context);
        assertTrue(ShadowToast.showedToast(Constants.MESSAGE_SETTINGS_NOT_VALID));
    }

    @Test
    public void testCheckAndGetSettings_HostnameEmpty() throws Exception {
        helper.checkAndGetSettings(context);
        SharedPreferences.Editor editor = ShadowPreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(Constants.APIKEY_PREF, "bla");
        editor.putString(Constants.PORT_PREF, "portbla");
        editor.commit();
        assertTrue(ShadowToast.showedToast(Constants.MESSAGE_SETTINGS_NOT_VALID));
    }

    @Test
    public void testCheckAndGetSettings_PortEmpty() throws Exception {
        helper.checkAndGetSettings(context);
        SharedPreferences.Editor editor = ShadowPreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(Constants.APIKEY_PREF, "bla");
        editor.putString(Constants.HOSTNAME_PREF, "hostbla");
        editor.commit();
        assertTrue(ShadowToast.showedToast(Constants.MESSAGE_SETTINGS_NOT_VALID));
    }

    @Test
    public void testCheckAndGetSettings_ApiKeyEmpty() throws Exception {
        helper.checkAndGetSettings(context);
        SharedPreferences.Editor editor = ShadowPreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(Constants.HOSTNAME_PREF, "hostbla");
        editor.putString(Constants.PORT_PREF, "portbla");
        editor.commit();
        assertTrue(ShadowToast.showedToast(Constants.MESSAGE_SETTINGS_NOT_VALID));
    }

    @Test
    public void testCheckAndGetSettings() throws Exception {
        SharedPreferences.Editor editor = ShadowPreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(Constants.APIKEY_PREF, "bla");
        editor.putString(Constants.HOSTNAME_PREF, "hostbla");
        editor.putString(Constants.PORT_PREF, "portbla");
        editor.commit();
        helper.checkAndGetSettings(context);

        assertFalse(ShadowToast.showedToast(Constants.MESSAGE_SETTINGS_NOT_VALID));

    }
}
