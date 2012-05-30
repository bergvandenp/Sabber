package nl.napauleon.sabber;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

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
        Preference preference = getPreferenceScreen().findPreference(key);
        preference.setSummary(sharedPreferences.getString(key, ""));
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
