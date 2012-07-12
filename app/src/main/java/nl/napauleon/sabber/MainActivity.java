package nl.napauleon.sabber;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import nl.napauleon.sabber.history.HistoryFragment;
import nl.napauleon.sabber.http.HttpGetHandler;
import nl.napauleon.sabber.queue.DownloadingFragment;

public class MainActivity extends SherlockFragmentActivity implements Handler.Callback {

    private static final String TAG_HISTORY_TAB = "history";
    private static final String TAG_DOWNLOADING_TAB = "downloading";
    private static final String SELECTED_TAB_PREF = "selectedTab";
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
        if (!new ContextHelper().isSabnzbSettingsPresent(this)) {
            Intent settingsActivity = new Intent(getBaseContext(), Settings.class);
            startActivity(settingsActivity);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeTabs();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        initializeTabs();
        ActionBar actionBar = getSupportActionBar();
        ActionBar.Tab tab = actionBar.getTabAt(savedInstanceState.getInt(SELECTED_TAB_PREF, 0));
        actionBar.selectTab(tab);
    }

    private void initializeTabs() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(false);
        if (actionBar.getTabCount() == 0) {
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
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        ActionBar.Tab selectedTab = getSupportActionBar().getSelectedTab();
        if (selectedTab != null) {
            outState.putInt(SELECTED_TAB_PREF, selectedTab.getPosition());
        }
        getSupportActionBar().removeAllTabs();
        super.onSaveInstanceState(outState);
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
            if (paused) {
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
                if (tag.equals(TAG_DOWNLOADING_TAB)) {
                    downloadingListener.mFragment.onResume();
                } else if (tag.equals(TAG_HISTORY_TAB)) {
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
            Message message = Message.obtain();
            if (paused) {
                message.obj = createResumeConnection(preferences);
            } else {
                message.obj = createPauseConnection(preferences);
            }
            new HttpGetHandler(this).sendMessage(message);
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

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case HttpGetHandler.MSG_RESULT:
                paused = !paused;
                invalidateOptionsMenu();
                break;
            case HttpGetHandler.MSG_CONNECTIONTIMEOUT:
                new ContextHelper().showConnectionTimeoutAlert(this);
                break;
            case HttpGetHandler.MSG_CONNECTIONERROR:
                new ContextHelper().showConnectionErrorAlert(this);
                break;
            default:
                return false;
        }
        return true;
    }
}