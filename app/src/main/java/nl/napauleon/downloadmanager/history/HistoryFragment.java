package nl.napauleon.downloadmanager.history;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockListFragment;
import nl.napauleon.downloadmanager.ContextHelper;
import nl.napauleon.downloadmanager.R;
import nl.napauleon.downloadmanager.http.HttpGetTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

public class HistoryFragment extends SherlockListFragment {

    public static final String TAG = "HistoryFragment";

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.itemlist, container, false);
	}
	
	@Override
    public void onResume() {
        super.onResume();
        retrieveData();
    }
	
	public void retrieveData() {
        SharedPreferences preferences = new ContextHelper().checkAndGetSettings(getActivity());
        if(preferences != null) {
            getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
            new HttpGetTask(new HistoryHandler()).execute(String.format("http://%s:%s/api?mode=history&limit=100&output=json&apikey=%s",
                    preferences.getString(ContextHelper.HOSTNAME_PREF, ""),
                    preferences.getString(ContextHelper.PORT_PREF, ""),
                    preferences.getString(ContextHelper.APIKEY_PREF, "")));
        }
	}

    class HistoryHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HttpGetTask.MSG_RESULT:
                    JSONArray slots;
                    try {
                        slots = ((JSONObject) new JSONTokener((String) msg.obj).nextValue()).getJSONObject("history").getJSONArray("slots");
                        ArrayList<HistoryInfo> historyItems = new ArrayList<HistoryInfo>(slots.length());
                        for(int i=0; i<slots.length(); i++) {
                            JSONObject slot = slots.getJSONObject(i);
                            String item = slot.getString("nzb_name").replace(".nzb", "");
                            Long dateDownloaded = slot.getLong("completed");
                            historyItems.add(new HistoryInfo(item, dateDownloaded));
                        }
                        setListAdapter(new HistoryListAdapter(getActivity(), historyItems));
                        getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing json string", e);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
