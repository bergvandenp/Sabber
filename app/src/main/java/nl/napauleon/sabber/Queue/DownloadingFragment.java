package nl.napauleon.sabber.queue;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import nl.napauleon.sabber.ContextHelper;
import nl.napauleon.sabber.MainActivity;
import nl.napauleon.sabber.R;
import nl.napauleon.sabber.http.HttpGetHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

public class DownloadingFragment extends SherlockListFragment{

    private TextView timeLeftView, speedView, sizeView, etaView;
    private QueueClickListener itemClickListener;
    private List<QueueInfo> queueItems;
    private HttpGetHandler httpHandler;
    private Handler backgroundHandler = new Handler();
    private Runnable backgroundUpdater = new Runnable() {
        public void run() {
            retrieveQueueData();
            backgroundHandler.postDelayed(this, refreshrate * 1000);
        }
    };
    private int refreshrate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        itemClickListener = new QueueClickListener();
        httpHandler = new HttpGetHandler(new DownloadingCallback());
    }

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
    public void onStart() {
        super.onStart();
        getListView().setOnItemClickListener(itemClickListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshrate = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString(ContextHelper.REFRESHRATE_PREF, "0"));
        if (refreshrate > 0) {
            backgroundHandler.postDelayed(backgroundUpdater, refreshrate * 1000);
        } else {
            retrieveQueueData();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        backgroundHandler.removeCallbacks(backgroundUpdater);
    }

    @Override
    public void onStop() {
        super.onStop();
        backgroundHandler.removeCallbacks(backgroundUpdater);
    }

    public void retrieveQueueData() {
        SharedPreferences preferences = new ContextHelper().checkAndGetSettings(getActivity());
        if (preferences != null) {
            getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
            Message message = Message.obtain();
            message.obj = createQueueConnectionString(preferences);
            httpHandler.sendMessage(httpHandler.obtainMessage(
                    HttpGetHandler.MSG_REQUEST,
                    createQueueConnectionString(preferences)
            ));
        }
    }

    String createQueueConnectionString(SharedPreferences preferences) {
        return String.format("http://%s:%s/api?mode=queue&output=json&apikey=%s",
                preferences.getString(ContextHelper.HOSTNAME_PREF, ""),
                preferences.getString(ContextHelper.PORT_PREF, ""),
                preferences.getString(ContextHelper.APIKEY_PREF, "")
        );
    }

    private class DownloadingCallback implements Handler.Callback {

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case HttpGetHandler.MSG_RESULT:
                try {
                    JSONObject queue = ((JSONObject) new JSONTokener((String) msg.obj)
                            .nextValue()).getJSONObject("queue");

                    togglePause(queue);

                    queueItems = extractQueueItems(queue);
                    setListAdapter(new QueueListAdapter(getActivity(), queueItems));
                    itemClickListener.setCategories(retrieveCategories(queue.getJSONArray("categories")));
                    itemClickListener.setQueueItems(queueItems);

                    populateGlobalInformation(queue.getString("timeleft"), queue.getString("size"),
                            queue.getString("speed"), queue.getString("eta"));
                } catch (JSONException e) {
                    new ContextHelper().handleJsonException(getActivity(), (String) msg.obj, e);
                }
                break;
            default:
                // causes further message handling
                return false;
        }
        stopSpinner();
        return true;
    }
    }

    private List<QueueInfo> extractQueueItems(JSONObject queue) throws JSONException {
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
        return queueItems;
    }

    private List<String> retrieveCategories(JSONArray jsonCategories) throws JSONException {
        List<String> categories = new ArrayList<String>(jsonCategories.length());
        for (int i = 0; i < jsonCategories.length(); i++) {
            categories.add(jsonCategories.getString(i));
        }
        return categories;
    }

    private void togglePause(JSONObject queue) throws JSONException {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setPaused(queue.getBoolean("paused"));
            if (Build.VERSION.SDK_INT >= 11) {
                getActivity().invalidateOptionsMenu();
            }
        }
    }

    private void stopSpinner() {
        SherlockFragmentActivity sherlockActivity = getSherlockActivity();
        if (sherlockActivity != null) {
            sherlockActivity.setSupportProgressBarIndeterminateVisibility(false);
        }
    }

    private void populateGlobalInformation(String timeleft, String size, String speed, String eta) throws JSONException {
        if (timeLeftView != null) {
            timeLeftView.setText(timeleft);
        }
        if (sizeView != null) {
            sizeView.setText(size);
        }
        if (speedView != null) {
            speedView.setText(speed);
        }
        if (etaView != null) {
            etaView.setText(eta);
        }
    }

    //for test purposes
    public List<QueueInfo> getQueueItems() {
        return new ArrayList<QueueInfo>(queueItems);
    }
}