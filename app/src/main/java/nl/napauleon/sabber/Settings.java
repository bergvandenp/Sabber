package nl.napauleon.sabber;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import nl.napauleon.sabber.history.NotificationService;

public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String HOSTNAME_PREF = "hostnamePref";
    public static final String PORT_PREF = "portPref";
    public static final String APIKEY_PREF = "apikeyPref";
    public static final String REFRESHRATE_PREF = "refreshratePref";
    public static final String NOTIFICATIONS_PREF = "notificationsPref";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference hostnamePref = getPreferenceScreen().findPreference("hostnamePref");
        Preference portPref = getPreferenceScreen().findPreference("portPref");
        Preference apikeyPref = getPreferenceScreen().findPreference("apikeyPref");
        Preference minsizePref = getPreferenceScreen().findPreference("minsizePref");

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        hostnamePref.setSummary(sharedPreferences.getString("hostnamePref", ""));
        portPref.setSummary(sharedPreferences.getString("portPref", ""));
        apikeyPref.setSummary(sharedPreferences.getString("apikeyPref", ""));
        minsizePref.setSummary(sharedPreferences.getString("minsizePref", ""));

    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (NOTIFICATIONS_PREF.equals(key)) {
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
