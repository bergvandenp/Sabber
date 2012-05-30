package nl.napauleon.sabber;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

public class ContextHelper {

    public static final String HOSTNAME_PREF = "hostnamePref";
    public static final String PORT_PREF = "portPref";
    public static final String APIKEY_PREF = "apikeyPref";
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
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setNeutralButton("Ok", null)
                .show();
    }

    public void showConnectionTimeoutAlert(Context context) {
        showErrorAlert(context, "Connection Timeout. Please try again later");
    }
}
