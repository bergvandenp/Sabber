package nl.napauleon.sabber;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class ContextHelper {
    protected static final String LAST_POLLING_EVENT_PREF = "lastpollingeventPref";
    private static final String TAG = "Sabber";
    public static final String MESSAGE_CONNECTION_TIMEOUT = "Connection Timeout. Please try again later";
    public static final String MESSAGE_SETTINGS_NOT_VALID = "Settings are not valid. Check host and port configuration.";

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
        String hostname = prefs.getString(Settings.HOSTNAME_PREF, "");
        String port = prefs.getString(Settings.PORT_PREF, "");
        String apikey = prefs.getString(Settings.APIKEY_PREF, "");

        return hostname != null && !hostname.equals("")
                && port != null && !port.equals("")
                && apikey != null && !apikey.equals("");
    }

    public boolean isNotificationsEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Settings.NOTIFICATIONS_PREF, false);
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
        showErrorAlert(context, MESSAGE_SETTINGS_NOT_VALID);
    }

    public void showErrorAlert(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void showConnectionTimeoutAlert(Context context) {
        showErrorAlert(context, MESSAGE_CONNECTION_TIMEOUT);
    }
}
