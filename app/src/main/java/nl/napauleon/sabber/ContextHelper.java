package nl.napauleon.sabber;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class ContextHelper {

    public static final String HOSTNAME_PREF = "hostnamePref";
    public static final String PORT_PREF = "portPref";
    public static final String APIKEY_PREF = "apikeyPref";
    public static final String REFRESHRATE_PREF = "refreshratePref";
    private static final String LAST_POLLING_EVENT_PREF = "lastpollingeventPref";
    private static final String TAG = "Sabber";

    public SharedPreferences checkAndGetSettings(Context context) {
        if (!isSabnzbSettingsPresent(context)) {
            showConnectionErrorAlert(context);
            return null;
        } else {
            return PreferenceManager.getDefaultSharedPreferences(context);
        }
    }

    public boolean isSabnzbSettingsPresent(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String hostname = prefs.getString(HOSTNAME_PREF, "");
        String port = prefs.getString(PORT_PREF, "");
        String apikey = prefs.getString(APIKEY_PREF, "");

        return hostname != null && !hostname.equals("")
                && port != null && !port.equals("")
                && apikey != null && !apikey.equals("");
    }

    public long updateLastPollingEvent(Context context) {
        long last_polling_event = System.currentTimeMillis();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putLong(LAST_POLLING_EVENT_PREF, last_polling_event);
        editor.commit();
        return last_polling_event;
    }

    public void handleJsonException(Context context, String originalJsonString, JSONException exception) {
        try {
            showErrorAlert(context, new JSONObject(originalJsonString).getString("error"));
        } catch (JSONException e1) {
            Log.e(TAG, "Error parsing json string", exception);
        }
    }

    public void showConnectionErrorAlert(Context context) {
        showErrorAlert(context, "Settings are not valid. Check host and port configuration.");
    }

    public void showErrorAlert(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void showConnectionTimeoutAlert(Context context) {
        showErrorAlert(context, "Connection Timeout. Please try again later");
    }
}
