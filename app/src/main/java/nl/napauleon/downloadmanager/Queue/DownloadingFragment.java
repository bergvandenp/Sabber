package nl.napauleon.downloadmanager.Queue;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import nl.napauleon.downloadmanager.R;
import nl.napauleon.downloadmanager.http.AsyncTaskHandler;
import nl.napauleon.downloadmanager.http.HttpGetTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class DownloadingFragment extends SherlockListFragment implements AsyncTaskHandler{

	private TextView timeLeftView, speedView, sizeView, etaView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.downloading_fragment, container, false);
        timeLeftView = (TextView) view.findViewById(R.id.timeleft);
        speedView = (TextView) view.findViewById(R.id.speed);
        sizeView = (TextView) view.findViewById(R.id.size);
        etaView = (TextView) view.findViewById(R.id.eta);
        return view;
	}

    @Override
    public void onResume() {
        super.onResume();
        retrieveQueueData();
    }
    
    public void retrieveQueueData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        String hostname = prefs.getString("hostnamePref", "");
        String port = prefs.getString("portPref", "");
        String apikey = prefs.getString("apikeyPref", "");

        if (hostname == null ||  hostname.isEmpty() || port == null || port.isEmpty()) {
        	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage("Settings are not valid. Check host and port configuration.").setNeutralButton("Ok", null).show();
        } else {
            new HttpGetTask(this).execute(createQueueConnectionString(hostname, port, apikey));
        }
    }

    private String createQueueConnectionString(String hostname, String port, String apikey) {
        return "http://" + hostname + ":" + port + "/api?mode=queue&output=json&apikey=" + apikey;
    }

    public void handleResult(String result) throws JSONException, UnsupportedEncodingException {
        JSONObject queue = ((JSONObject) new JSONTokener(result).nextValue()).getJSONObject("queue");
        JSONArray jsonCategories = queue.getJSONArray("categories");
        List<String> categories = new ArrayList<String>(jsonCategories.length());
        for(int i=0; i < jsonCategories.length(); i++) {
            categories.add(jsonCategories.getString(i));
        }
        JSONArray slots = queue.getJSONArray("slots");
        List<QueueInfo> queueItems = new ArrayList<QueueInfo>(slots.length());
        for(int i=0; i<slots.length(); i++) {
            JSONObject slot = slots.getJSONObject(i);
            String item = slot.getString("filename");
            String timeleft = slot.getString("timeleft");
            Integer percentage = slot.getInt("percentage");
            String id = slot.getString("nzo_id");
            queueItems.add(new QueueInfo(id, item, timeleft, percentage));
        }
        for(int i=0; i<1; i++) {
            String item = "test";
            String timeleft = "0:55:10";
            Integer percentage = 30;
            String id = "2";
            queueItems.add(new QueueInfo(id, item, timeleft, percentage));
        }
        setListAdapter(new QueueListAdapter(getActivity(), queueItems));
        if(timeLeftView != null) {
            timeLeftView.setText(queue.getString("timeleft"));
        }
        if (sizeView != null) {
            sizeView.setText(queue.getString("size"));
        }
        if (speedView != null) {
            speedView.setText(queue.getString("speed"));
        }
        if (etaView != null) {
            etaView.setText(queue.getString("eta"));
        }
        getListView().setOnItemClickListener(new QueueClickListener(PreferenceManager.getDefaultSharedPreferences(DownloadingFragment.this.getActivity()), queueItems, categories));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

	
}
