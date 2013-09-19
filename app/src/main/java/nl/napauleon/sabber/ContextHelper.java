package nl.napauleon.sabber;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;

public class ContextHelper {

    private final SharedPreferences prefs;
    private final Context context;

    public ContextHelper(Context context) {
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public SharedPreferences checkAndGetSettings() {
        if (!isSabnzbSettingsPresent()) {
            showConnectionErrorAlert();
            return null;
        } else {
            return PreferenceManager.getDefaultSharedPreferences(context);
        }
    }

    public boolean isSabnzbSettingsPresent() {
        String hostname = prefs.getString(Constants.HOSTNAME_PREF, "");
        String port = prefs.getString(Constants.PORT_PREF, "");
        String apikey = prefs.getString(Constants.APIKEY_PREF, "");

        return hostname != null && !hostname.equals("")
                && port != null && !port.equals("")
                && apikey != null && !apikey.equals("");
    }

    public boolean isNotificationsEnabled() {
        return prefs.getBoolean(Constants.NOTIFICATIONS_PREF, false);
    }

	public boolean isMockEnabled() {
		return BuildConfig.DEBUG && prefs.getString(Constants.PORT_PREF, "").equals("666");
	}

    public long updateLastPollingEvent(long time) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(Constants.LAST_POLLING_EVENT_PREF, time);
        editor.commit();
        return time;
    }

    public void handleJsonException(String originalJsonString, JSONException exception) {
        try {
            showErrorAlert(new JSONObject(originalJsonString).getString("error"));
        } catch (JSONException e1) {
            Log.e(Constants.TAG, "Error parsing json string", exception);
        }
    }

    public void showConnectionErrorAlert() {
        showErrorAlert(Constants.MESSAGE_SETTINGS_NOT_VALID);
    }

    public void showErrorAlert(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void showConnectionTimeoutAlert() {
        showErrorAlert(Constants.MESSAGE_CONNECTION_TIMEOUT);
    }
}
