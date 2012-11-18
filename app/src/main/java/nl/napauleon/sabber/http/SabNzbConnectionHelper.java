package nl.napauleon.sabber.http;

import android.content.SharedPreferences;
import android.util.Log;
import nl.napauleon.sabber.SettingsActivity;
import nl.napauleon.sabber.search.NzbInfo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SabNzbConnectionHelper {

    private static final String ENCODING = "UTF-8";
    private static final String TAG = "SabNzbConnectionHelper";

    private final String host, port, apikey;

    public SabNzbConnectionHelper(SharedPreferences preferences) {
        host = preferences.getString(SettingsActivity.HOSTNAME_PREF, "");
        port = preferences.getString(SettingsActivity.PORT_PREF, "");
        apikey = preferences.getString(SettingsActivity.APIKEY_PREF, "");
    }

    public String createAddUrlConnectionString(NzbInfo nzbInfo) {
        String link = nzbInfo.getLink();
        String name = nzbInfo.getTitle();
        Pattern pattern = Pattern.compile("\"(.*?)\"");
        Matcher matcher = pattern.matcher(nzbInfo.getTitle());
        if (matcher.find()) {
            name = matcher.group(1);
        }
        try {
            return String.format(createBaseConnectionString() + "&mode=addurl&name=%s&nzbname=%s", URLEncoder.encode(link, ENCODING),
                    URLEncoder.encode(name, ENCODING));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unsupported encoding: " + ENCODING, e);
        }
        return null;
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
