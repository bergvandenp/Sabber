package nl.napauleon.sabber.http;

import android.content.SharedPreferences;
import nl.napauleon.sabber.Settings;

public class SabNzbConnectionHelper {

    private final String host, port, apikey;

    public SabNzbConnectionHelper(SharedPreferences preferences) {
        host = preferences.getString(Settings.HOSTNAME_PREF, "");
        port = preferences.getString(Settings.PORT_PREF, "");
        apikey = preferences.getString(Settings.APIKEY_PREF, "");
    }

    public String createHistoryConnectionString() {
        return createBaseConnectionString() + "&mode=history&limit=100";
    }

    public String createQueueConnectionString() {
        return createBaseConnectionString() + "&mode=queue";
    }

    public String createDeleteItemConnectionString(String itemId) {
        return String.format(createBaseConnectionString() + "&mode=queue&name=delete&value=%s", itemId);
    }

    public String createChangeCategoryConnectionString(String itemId, String category) {
        return String.format(createBaseConnectionString() + "&mode=change_cat&value=%s&value2=%s", itemId, category);
    }

    public String createResumeConnection() {
        return createBaseConnectionString() + "&mode=resume";
    }

    public String createPauseConnection() {
        return createBaseConnectionString() + "&mode=pause";
    }

    private String createBaseConnectionString() {
        return String.format("http://%s:%s/api?output=json&apikey=%s",
                host, port, apikey);
    }
}
