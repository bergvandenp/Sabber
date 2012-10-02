package nl.napauleon.sabber.history;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import nl.napauleon.sabber.ContextHelper;
import nl.napauleon.sabber.R;
import nl.napauleon.sabber.http.DefaultErrorCallback;
import nl.napauleon.sabber.http.HttpGetHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

public class HistoryFragment extends SherlockListFragment{

    public static final String TAG = "HistoryFragment";
    private ArrayList<HistoryInfo> historyItems;
    private HttpGetHandler httpHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        httpHandler = new HttpGetHandler(new HistoryCallback());
    }

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
        if (preferences != null) {
            getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
            httpHandler.executeRequest(createHistoryConnectionString(preferences));
        }
    }

    String createHistoryConnectionString(SharedPreferences preferences) {
        return String.format("http://%s:%s/api?mode=history&limit=100&output=json&apikey=%s",
                preferences.getString(ContextHelper.HOSTNAME_PREF, ""),
                preferences.getString(ContextHelper.PORT_PREF, ""),
                preferences.getString(ContextHelper.APIKEY_PREF, ""));
    }

    private class HistoryCallback extends DefaultErrorCallback {
        public HistoryCallback() {
            super(HistoryFragment.this.getActivity());
        }

        public boolean handleMessage(Message msg) {
            boolean messageHandled = super.handleMessage(msg);
            if (messageHandled) {
                stopSpinner();
                return true;
            }
            switch (msg.what) {
                case HttpGetHandler.MSG_RESULT:
                    handleResult(msg);
                    stopSpinner();
                    break;
            }
            return messageHandled;
        }

        private void handleResult(Message msg) {
            try {
                JSONArray slots = ((JSONObject) new JSONTokener((String) msg.obj).nextValue())
                        .getJSONObject("history").getJSONArray("slots");
                historyItems = new ArrayList<HistoryInfo>(slots.length());
                for (int i = 0; i < slots.length(); i++) {
                    JSONObject slot = slots.getJSONObject(i);

                    Status status;
                    try {
                        status = Status.valueOf(slot.getString("status"));
                    } catch (IllegalArgumentException e) {
                        Log.w(TAG, "status " + slot.getString("status") + " is not recognized.");
                        status = Status.Unknown;
                    }
                    historyItems.add(new HistoryInfo(
                            slot.getString("nzb_name").replace(".nzb", ""),
                            slot.getLong("completed"),
                            status == Status.Failed ? slot.getString("fail_message") : slot.getString("action_line")));
                }
                setListAdapter(new HistoryListAdapter(getActivity(), historyItems));
            } catch (JSONException e) {
                new ContextHelper().handleJsonException(getActivity(), (String) msg.obj, e);
            }
        }
    }



    private void stopSpinner() {
        SherlockFragmentActivity sherlockActivity = getSherlockActivity();
        if (sherlockActivity != null) {
            sherlockActivity.setSupportProgressBarIndeterminateVisibility(false);
        }
    }

    //for test purposes
    public ArrayList<HistoryInfo> getHistoryItems() {
        return new ArrayList<HistoryInfo>(historyItems);
    }
}
