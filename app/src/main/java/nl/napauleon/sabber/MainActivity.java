package nl.napauleon.sabber;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import nl.napauleon.sabber.Queue.DownloadingFragment;
import nl.napauleon.sabber.history.HistoryFragment;
import nl.napauleon.sabber.http.HttpGetTask;
import nl.napauleon.sabber.http.HttpHandler;

public class MainActivity extends SherlockFragmentActivity {

    private static final String TAG_HISTORY_TAB = "history";
    private static final String TAG_DOWNLOADING_TAB = "downloading";
    private TabListener<DownloadingFragment> downloadingListener;
    private TabListener<HistoryFragment> historyListener;
    private boolean paused = false;

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(com.actionbarsherlock.view.Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setSupportProgressBarIndeterminateVisibility(false);
        initializeTabs();
    }

	private void initializeTabs() {
		ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(false);

        downloadingListener = new TabListener<DownloadingFragment>(
                this, "downloading", DownloadingFragment.class);
        actionBar.addTab(actionBar.newTab()
                .setText(R.string.downloading)
                .setTabListener(downloadingListener)
                .setTag("downloading"));

        historyListener = new TabListener<HistoryFragment>(
                this, "history", HistoryFragment.class);
        actionBar.addTab(actionBar.newTab()
                .setText(R.string.history)
                .setTabListener(historyListener)
                .setTag(TAG_HISTORY_TAB));
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.menu_pause);
        if (menuItem != null) {
            if(paused) {
                menuItem.setIcon(R.drawable.ic_play);
                menuItem.setTitle(R.string.menu_play);
            } else {
                menuItem.setIcon(R.drawable.ic_pause);
                menuItem.setTitle(R.string.menu_pause);
            }

        }
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_pause:
                togglePauseSabnzb();
                return true;
            case R.id.menu_settings:
                Intent settingsActivity = new Intent(getBaseContext(),
                        Settings.class);
                startActivity(settingsActivity);
                return true;
            case R.id.menu_refresh:
                Object tag = getSupportActionBar().getSelectedTab().getTag();
                if(tag.equals(TAG_DOWNLOADING_TAB)) {
                    downloadingListener.mFragment.onResume();
                } else if(tag.equals(TAG_HISTORY_TAB)) {
                    historyListener.mFragment.onResume();
                }
                return true;
            case R.id.menu_search:
                this.onSearchRequested();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void togglePauseSabnzb() {
        SharedPreferences preferences = new ContextHelper().checkAndGetSettings(this);
        if (preferences != null) {
            if(paused) {
                new HttpGetTask(new PauseToggleHandler()).execute(createResumeConnection(preferences));
            }else {
                new HttpGetTask(new PauseToggleHandler()).execute(createPauseConnection(preferences));
            }
        }
    }

    private String createResumeConnection(SharedPreferences preferences) {
        return String.format("http://%s:%s/api?mode=resume&apikey=%s",
                preferences.getString(ContextHelper.HOSTNAME_PREF, ""),
                preferences.getString(ContextHelper.PORT_PREF, ""),
                preferences.getString(ContextHelper.APIKEY_PREF, ""));
    }

    private String createPauseConnection(SharedPreferences preferences) {
        return String.format("http://%s:%s/api?mode=pause&apikey=%s",
                preferences.getString(ContextHelper.HOSTNAME_PREF, ""),
                preferences.getString(ContextHelper.PORT_PREF, ""),
                preferences.getString(ContextHelper.APIKEY_PREF, ""));
    }

    private class PauseToggleHandler extends HttpHandler {
        public PauseToggleHandler() {
            super(MainActivity.this);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HttpGetTask.MSG_RESULT:
                    paused = !paused;
                    invalidateOptionsMenu();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}