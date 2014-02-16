package nl.napauleon.sabber;

import android.preference.PreferenceActivity;

import java.util.List;

public class SettingsActivity extends PreferenceActivity {

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        getFragmentManager().beginTransaction()
//                .replace(android.R.id.content, new SettingsFragment())
//                .commit();
//    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return SettingsFragment.class.getName().equals(fragmentName);
    }
}
