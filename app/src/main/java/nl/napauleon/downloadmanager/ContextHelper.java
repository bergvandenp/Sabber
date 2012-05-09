package nl.napauleon.downloadmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ContextHelper {

    public static final String HOSTNAME_PREF = "hostnamePref";
    public static final String PORT_PREF = "portPref";
    public static final String APIKEY_PREF = "apikeyPref";

    public SharedPreferences checkAndGetSettings(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String hostname = prefs.getString(HOSTNAME_PREF, "");
        String port = prefs.getString(PORT_PREF, "");
        String apikey = prefs.getString(APIKEY_PREF, "");

        if (hostname == null ||  hostname.isEmpty()
                || port == null || port.isEmpty()
                || apikey == null || apikey.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Settings are not valid. Check host and port configuration.")
                    .setNeutralButton("Ok", null).show();
            return prefs;
        } else {
            return null;
        }
    }
}
