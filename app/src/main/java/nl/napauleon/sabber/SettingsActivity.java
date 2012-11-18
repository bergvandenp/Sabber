package nl.napauleon.sabber;

import nl.napauleon.sabber.history.NotificationService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String HOSTNAME_PREF = "hostnamePref";
    public static final String PORT_PREF = "portPref";
    public static final String APIKEY_PREF = "apikeyPref";
    public static final String REFRESHRATE_PREF = "refreshratePref";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Constants.NOTIFICATIONS_PREF.equals(key)) {
            boolean notificationsEnabled = sharedPreferences.getBoolean(key, false);
            Intent intent = new Intent(this, NotificationService.class);
            if (notificationsEnabled) {
                startService(intent);
            } else {
                stopService(intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
