package nl.napauleon.downloadmanager.Queue;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListFragment;
import nl.napauleon.downloadmanager.ContextHelper;
import nl.napauleon.downloadmanager.MainActivity;
import nl.napauleon.downloadmanager.R;
import nl.napauleon.downloadmanager.http.HttpGetTask;
import nl.napauleon.downloadmanager.http.HttpHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

public class DownloadingFragment extends SherlockListFragment {

    public static final String TAG = "DownloadingFragment";
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
        SharedPreferences preferences = new ContextHelper().checkAndGetSettings(getActivity());
        if (preferences != null) {
            getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
            new HttpGetTask(new DownloadingHandler()).execute(
                    String.format("http://%s:%s/api?mode=queue&output=json&apikey=%s",
                            preferences.getString(ContextHelper.HOSTNAME_PREF, ""),
                            preferences.getString(ContextHelper.PORT_PREF, ""),
                            preferences.getString(ContextHelper.APIKEY_PREF, "")
                    )
            );
        }
    }

    class DownloadingHandler extends HttpHandler {

        public DownloadingHandler() {
            super(getActivity());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HttpGetTask.MSG_RESULT:
                    try {
                        JSONObject queue = ((JSONObject) new JSONTokener((String) msg.obj)
                                .nextValue()).getJSONObject("queue");
                        if(getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).setPaused(queue.getBoolean("paused"));
                            if(Build.VERSION.SDK_INT >= 11) {
                                getActivity().invalidateOptionsMenu();
                            }
                        }
                        JSONArray jsonCategories = queue.getJSONArray("categories");
                        List<String> categories = new ArrayList<String>(jsonCategories.length());
                        for (int i = 0; i < jsonCategories.length(); i++) {
                            categories.add(jsonCategories.getString(i));
                        }
                        JSONArray slots = queue.getJSONArray("slots");
                        List<QueueInfo> queueItems = new ArrayList<QueueInfo>(slots.length());
                        for (int i = 0; i < slots.length(); i++) {
                            JSONObject slot = slots.getJSONObject(i);
                            queueItems.add(new QueueInfo(
                                    slot.getString("nzo_id"),
                                    slot.getString("filename"),
                                    slot.getString("timeleft"),
                                    slot.getInt("percentage")));
                        }
                        setListAdapter(new QueueListAdapter(getActivity(), queueItems));
                        if (timeLeftView != null) {
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
                        getListView().setOnItemClickListener(new QueueClickListener(DownloadingFragment.this, queueItems, categories));
                        getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing json string", e);
                    }
                    break;
                default:
                    getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
                    super.handleMessage(msg);
            }
        }
    }
}