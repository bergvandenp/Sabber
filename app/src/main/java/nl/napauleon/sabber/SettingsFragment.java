package nl.napauleon.sabber;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import nl.napauleon.sabber.history.NotificationService;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        Preference pref = findPreference("notificationsRefreshratePref");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            pref.getExtras().putString("suffix", "s");
            bindPreferenceSummaryToValue(pref);

            pref = findPreference("screenRefreshratePref");
            pref.getExtras().putString("suffix", "s");
            bindPreferenceSummaryToValue(pref);

            pref = findPreference("minsizePref");
            pref.getExtras().putString("suffix", " MB");
            bindPreferenceSummaryToValue(pref);

        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Activity activity = getActivity();
        if (activity != null) {
            if (Constants.NOTIFICATIONS_PREF.equals(key)) {
                boolean notificationsEnabled = sharedPreferences.getBoolean(key, false);
                Intent intent = new Intent(activity, NotificationService.class);
                if (notificationsEnabled) {
                    activity.startService(intent);
                } else {
                    activity.stopService(intent);
                }
            } else if (Constants.NOTIFICATIONS_REFRESHRATE_PREF.equals(key)
                    && isNotificationsEnabled(sharedPreferences)) {
                Intent intent = new Intent(activity, NotificationService.class);
                activity.stopService(intent);
                activity.startService(intent);
            }
        }
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            String suffix = preference.getExtras().getString("suffix");
            if (suffix == null) suffix = "";

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index] + suffix
                                : null);

            } else {
                preference.setSummary(stringValue + suffix);
            }
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    private boolean isNotificationsEnabled(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean(Constants.NOTIFICATIONS_PREF, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
