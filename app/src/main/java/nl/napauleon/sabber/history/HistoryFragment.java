package nl.napauleon.sabber.history;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import nl.napauleon.sabber.ContextHelper;
import nl.napauleon.sabber.R;
import nl.napauleon.sabber.http.SabNzbConnectionHelper;
import nl.napauleon.sabber.http.DefaultErrorCallback;
import nl.napauleon.sabber.http.HttpGetHandler;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends SherlockListFragment{

    private HttpGetHandler httpHandler;
    private List<HistoryInfo> historyItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        httpHandler = new HttpGetHandler(new HistoryFragmentCallback());
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
            httpHandler.executeRequest(new SabNzbConnectionHelper(preferences).createHistoryConnectionString());
        }
    }

    public class HistoryFragmentCallback extends DefaultErrorCallback {
        public HistoryFragmentCallback() {
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
            ContextHelper contextHelper = new ContextHelper();
            String messageText = (String) msg.obj;
            try {
                historyItems = HistoryInfo.createHistoryList(messageText);
                contextHelper.updateLastPollingEvent(getActivity());
                setListAdapter(new HistoryListAdapter(getActivity(), historyItems));
            } catch (JSONException e) {
                contextHelper.handleJsonException(getActivity(), messageText, e);
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
