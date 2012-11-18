package nl.napauleon.sabber.queue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import nl.napauleon.sabber.ContextHelper;
import nl.napauleon.sabber.MainActivity;
import nl.napauleon.sabber.R;
import nl.napauleon.sabber.SettingsActivity;
import nl.napauleon.sabber.http.DefaultErrorCallback;
import nl.napauleon.sabber.http.HttpGetTask;
import nl.napauleon.sabber.http.SabNzbConnectionHelper;
import nl.napauleon.sabber.http.SabnzbResultHelper;
import nl.napauleon.sabber.search.GlobalInfo;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;

public class DownloadingFragment extends SherlockListFragment {

    private TextView timeLeftView, speedView, sizeView, etaView;
    private QueueClickListener itemClickListener;
    private List<QueueInfo> queueItems;
    private Handler backgroundHandler = new Handler();
    private Runnable backgroundUpdater = new Runnable() {
        public void run() {
            retrieveQueueData();
            backgroundHandler.postDelayed(this, refreshrate * 1000);
        }
    };
    private int refreshrate;
    private SharedPreferences preferences;
    private HttpGetTask httpGetTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        itemClickListener = new QueueClickListener();
        httpGetTask = new HttpGetTask(new DownloadingCallback());
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
        preferences = new ContextHelper().checkAndGetSettings(getActivity());
        String refreshratePref = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString(SettingsActivity.REFRESHRATE_PREF, "0");
        if (StringUtils.isNotBlank(refreshratePref) && Integer.parseInt(refreshratePref) > 0) {
        		backgroundHandler.postDelayed(backgroundUpdater, Integer.parseInt(refreshratePref) * 1000);
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
        if (preferences != null) {
            getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
            if (httpGetTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                httpGetTask.cancel(true);
            }
            httpGetTask = new HttpGetTask(new DownloadingCallback());
            httpGetTask.execute(new SabNzbConnectionHelper(preferences).createQueueConnectionString());
        }
    }

    public class DownloadingCallback extends DefaultErrorCallback {
        public DownloadingCallback() {
            super();
        }
        
		public void handleError(String error) {
			super.handleError(DownloadingFragment.this.getActivity(), error);
			stopSpinner();
		}

		public void handleTimeout() {
			super.handleTimeout(DownloadingFragment.this.getActivity());
			stopSpinner();
		}

		public void handleResponse(String response) {
			stopSpinner();
			handleResult(response);
			
		}

        private void handleResult(String result) {
            SabnzbResultHelper sabnzbResultHelper = new SabnzbResultHelper(result);
            togglePause(sabnzbResultHelper.isPaused());

            queueItems = sabnzbResultHelper.parseQueueItems();
            setListAdapter(new QueueListAdapter(getActivity(), queueItems));
            itemClickListener.setCategories(sabnzbResultHelper.parseCategories());
            itemClickListener.setQueueItems(queueItems);

            populateGlobalInformation(sabnzbResultHelper.parseGlobalInfo());
        }
    }

    private void togglePause(boolean paused) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setPaused(paused);
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

    private void populateGlobalInformation(GlobalInfo globalInfo) {
        if (timeLeftView != null) {
            timeLeftView.setText(globalInfo.getTimeleft());
        }
        if (sizeView != null) {
            sizeView.setText(globalInfo.getSize());
        }
        if (speedView != null) {
            speedView.setText(globalInfo.getSpeed());
        }
        if (etaView != null) {
            etaView.setText(globalInfo.getEta());
        }
    }

    //for test purposes
    public List<QueueInfo> getQueueItems() {
        return new ArrayList<QueueInfo>(queueItems);
    }
}