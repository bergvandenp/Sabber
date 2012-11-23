package nl.napauleon.sabber.history;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.BuildConfig;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import nl.napauleon.sabber.Constants;
import nl.napauleon.sabber.ContextHelper;
import nl.napauleon.sabber.R;
import nl.napauleon.sabber.http.DefaultErrorCallback;
import nl.napauleon.sabber.http.HttpGetMockTask;
import nl.napauleon.sabber.http.HttpGetTask;
import nl.napauleon.sabber.http.SabNzbConnectionHelper;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends SherlockListFragment{

    private List<HistoryInfo> historyItems;

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
	    	if (BuildConfig.DEBUG && preferences.getString(Constants.PORT_PREF, "").equals("666")) {
	    		new HttpGetMockTask(new HistoryFragmentCallback()).execute("history/historyresult");
	    		return;
	    	}
	        getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
	        new HttpGetTask(new HistoryFragmentCallback()).execute(new SabNzbConnectionHelper(preferences).createHistoryConnectionString());
        }
    }

    public class HistoryFragmentCallback extends DefaultErrorCallback {
        public HistoryFragmentCallback() {
            super();
        }

		public void handleError(String error) {
        	stopSpinner();
			super.handleError(HistoryFragment.this.getActivity(), error);
		}

		public void handleTimeout() {
			stopSpinner();
			super.handleTimeout(HistoryFragment.this.getActivity());
		}
		
		public void handleResponse(String response) {
			stopSpinner();
			handleResult(response);
		}

        private void handleResult(String response) {
            ContextHelper contextHelper = new ContextHelper();
            try {
                historyItems = HistoryInfo.createHistoryList(response);
                contextHelper.updateLastPollingEvent(getActivity(), System.currentTimeMillis());
                setListAdapter(new HistoryListAdapter(getActivity(), historyItems));
            } catch (JSONException e) {
                contextHelper.handleJsonException(getActivity(), response, e);
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
