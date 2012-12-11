package nl.napauleon.sabber.http;

import android.util.Log;
import nl.napauleon.sabber.queue.QueueInfo;
import nl.napauleon.sabber.search.GlobalInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

public class SabnzbResultHelper {

    private JSONObject jsonObject = null;
    private static final String TAG = "SabnzbResultHelper";

    public SabnzbResultHelper(String jsonMessage) {
        parse(jsonMessage);
    }

    private void parse(String jsonMessage) {
        try {
            jsonObject = ((JSONObject) new JSONTokener(jsonMessage).nextValue()).getJSONObject("queue");
        } catch (JSONException e) {
            handleException(e);
        }
    }

    private void handleException(JSONException e) {
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException("No valid json response string.", e);
        Log.e(TAG, "error parsing sabnzb result.", illegalArgumentException);
        throw illegalArgumentException;
    }

    public boolean isPaused() {
        try {
            return jsonObject.getBoolean("paused");
        } catch (JSONException e) {
            handleException(e);
        }
        return false;
    }

    public GlobalInfo parseGlobalInfo() {
        try {
            return new GlobalInfo(jsonObject.getString("timeleft"), jsonObject.getString("size"),
                    jsonObject.getString("speed"), jsonObject.getString("eta"));
        } catch (JSONException e) {
            handleException(e);
        }
        return null;
    }

    public List<String> parseCategories() {
        try {
            JSONArray jsonCategories = jsonObject.getJSONArray("categories");
            List<String> categories = new ArrayList<String>(jsonCategories.length());
            for (int i = 0; i < jsonCategories.length(); i++) {
                categories.add(jsonCategories.getString(i));
            }
            return categories;
        } catch (JSONException e) {
            handleException(e);
        }
        return new ArrayList<String>();
    }

    public List<QueueInfo> parseQueueItems() {
        try {
            JSONArray slots = jsonObject.getJSONArray("slots");
            List<QueueInfo> queueItems = new ArrayList<QueueInfo>(slots.length());
            for (int i = 0; i < slots.length(); i++) {
                JSONObject slot = slots.getJSONObject(i);
                queueItems.add(new QueueInfo(
                        slot.getString("nzo_id"),
                        slot.getString("filename"),
                        slot.getString("timeleft"),
                        slot.getInt("percentage")));
            }
            return queueItems;
        } catch (JSONException e) {
            handleException(e);
        }
        return new ArrayList<QueueInfo>();
    }

}
