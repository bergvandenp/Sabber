package nl.napauleon.sabber.history;

import java.util.ArrayList;
import java.util.List;

import nl.napauleon.sabber.ContextHelper;
import nl.napauleon.sabber.R;
import nl.napauleon.sabber.http.DefaultErrorCallback;
import nl.napauleon.sabber.http.HttpCallback;
import nl.napauleon.sabber.http.HttpGetTask;
import nl.napauleon.sabber.http.SabNzbConnectionHelper;

import org.json.JSONException;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;

public class HistoryFragment extends SherlockListFragment{

    private List<HistoryInfo> historyItems;
    private HttpGetTask httpGetTask;
	private HttpCallback callback = new HistoryFragmentCallback();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		httpGetTask = new HttpGetTask(callback);
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
            if (httpGetTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                httpGetTask.cancel(true);
            }
            httpGetTask = new HttpGetTask(callback);
            httpGetTask.execute(new SabNzbConnectionHelper(preferences).createHistoryConnectionString());
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
                contextHelper.updateLastPollingEvent(getActivity());
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
