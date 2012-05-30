package nl.napauleon.sabber.history;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockListFragment;
import nl.napauleon.sabber.ContextHelper;
import nl.napauleon.sabber.R;
import nl.napauleon.sabber.http.HttpGetTask;
import nl.napauleon.sabber.http.HttpHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

public class HistoryFragment extends SherlockListFragment {

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

    class HistoryHandler extends HttpHandler {
        public HistoryHandler() {
            super(getActivity());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HttpGetTask.MSG_RESULT:
                    try {
                        JSONArray slots = ((JSONObject) new JSONTokener((String) msg.obj).nextValue())
                                .getJSONObject("history").getJSONArray("slots");
                        ArrayList<HistoryInfo> historyItems = new ArrayList<HistoryInfo>(slots.length());
                        for(int i=0; i<slots.length(); i++) {
                            JSONObject slot = slots.getJSONObject(i);
                            historyItems.add(new HistoryInfo(
                                    slot.getString("nzb_name").replace(".nzb", ""),
                                    slot.getLong("completed")));
                        }
                        setListAdapter(new HistoryListAdapter(getActivity(), historyItems));
                    } catch (JSONException e) {
                        new ContextHelper().handleJsonException(getActivity(), (String) msg.obj, e);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
            getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
        }
    }
}
