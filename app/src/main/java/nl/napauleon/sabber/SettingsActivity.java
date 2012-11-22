package nl.napauleon.sabber;

import nl.napauleon.sabber.history.NotificationService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

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
        } else if (Constants.NOTIFICATIONS_REFRESHRATE_PREF.equals(key) 
        		&& isNotificationsEnabled(sharedPreferences)) {
        	Intent intent = new Intent(this, NotificationService.class);
        	stopService(intent);
        	startService(intent);
        }
    }

	private boolean isNotificationsEnabled(SharedPreferences sharedPreferences) {
		return sharedPreferences.getBoolean(Constants.NOTIFICATIONS_PREF, false);
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
