package nl.napauleon.sabber;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class ContextHelper {

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
        String hostname = prefs.getString(Constants.HOSTNAME_PREF, "");
        String port = prefs.getString(Constants.PORT_PREF, "");
        String apikey = prefs.getString(Constants.APIKEY_PREF, "");

        return hostname != null && !hostname.equals("")
                && port != null && !port.equals("")
                && apikey != null && !apikey.equals("");
    }

    public boolean isNotificationsEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(Constants.NOTIFICATIONS_PREF, false);
    }

    public long updateLastPollingEvent(Context context) {
        long last_polling_event = System.currentTimeMillis();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putLong(Constants.LAST_POLLING_EVENT_PREF, last_polling_event);
        editor.commit();
        return last_polling_event;
    }

    public void handleJsonException(Context context, String originalJsonString, JSONException exception) {
        try {
            showErrorAlert(context, new JSONObject(originalJsonString).getString("error"));
        } catch (JSONException e1) {
            Log.e(Constants.TAG, "Error parsing json string", exception);
        }
    }

    public void showConnectionErrorAlert(Context context) {
        showErrorAlert(context, Constants.MESSAGE_SETTINGS_NOT_VALID);
    }

    public void showErrorAlert(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void showConnectionTimeoutAlert(Context context) {
        showErrorAlert(context, Constants.MESSAGE_CONNECTION_TIMEOUT);
    }
}
