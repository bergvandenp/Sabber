package nl.napauleon.downloadmanager.history;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockListFragment;
import nl.napauleon.downloadmanager.R;
import nl.napauleon.downloadmanager.http.AsyncTaskHandler;
import nl.napauleon.downloadmanager.http.HttpGetTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

public class HistoryFragment extends SherlockListFragment implements AsyncTaskHandler {

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
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        String hostname = prefs.getString("hostnamePref", "");
        String port = prefs.getString("portPref", "");
        String apikey = prefs.getString("apikeyPref", "");
		new HttpGetTask(this).execute("http://" + hostname + ":" + port + "/api?mode=history&limit=100&output=json&apikey=" + apikey);
	}

    public void handleResult(String result) throws JSONException{
        JSONArray slots = ((JSONObject) new JSONTokener(result).nextValue()).getJSONObject("history").getJSONArray("slots");
        ArrayList<HistoryInfo> historyItems = new ArrayList<HistoryInfo>(slots.length());
        for(int i=0; i<slots.length(); i++) {
            JSONObject slot = slots.getJSONObject(i);
            String item = slot.getString("nzb_name").replace(".nzb", "");
            Long dateDownloaded = slot.getLong("completed");
            historyItems.add(new HistoryInfo(item, dateDownloaded));
        }
        setListAdapter(new HistoryListAdapter(getActivity(), historyItems));
    }

}
