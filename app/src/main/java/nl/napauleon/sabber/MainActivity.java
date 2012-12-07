package nl.napauleon.sabber;

import nl.napauleon.sabber.history.HistoryFragment;
import nl.napauleon.sabber.history.NotificationService;
import nl.napauleon.sabber.http.DefaultErrorCallback;
import nl.napauleon.sabber.http.HttpGetTask;
import nl.napauleon.sabber.http.SabNzbConnectionHelper;
import nl.napauleon.sabber.queue.DownloadingFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity {

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
		ContextHelper contextHelper = new ContextHelper(this);
		if (!contextHelper.isSabnzbSettingsPresent()) {
			Intent settingsActivity = new Intent(this, SettingsActivity.class);
			startActivity(settingsActivity);
		}
		if (contextHelper.isNotificationsEnabled()) {
			Intent intent = new Intent(this, NotificationService.class);
			startService(intent);
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
		ActionBar.Tab tab = actionBar.getTabAt(savedInstanceState.getInt(
				SELECTED_TAB_PREF, 0));
		actionBar.selectTab(tab);
	}

	private void initializeTabs() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);
		if (actionBar.getTabCount() == 0) {
			downloadingListener = new TabListener<DownloadingFragment>(this,
					"downloading", DownloadingFragment.class);
			actionBar.addTab(actionBar.newTab().setText(R.string.downloading)
					.setTabListener(downloadingListener).setTag("downloading"));

			historyListener = new TabListener<HistoryFragment>(this, "history",
					HistoryFragment.class);
			actionBar.addTab(actionBar.newTab().setText(R.string.history)
					.setTabListener(historyListener).setTag(TAG_HISTORY_TAB));
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
		if (item.getItemId() == R.id.menu_pause) {
			togglePauseSabnzb();
			return true;
		} else if (item.getItemId() == R.id.menu_settings) {
			Intent settingsActivity = new Intent(getBaseContext(),
					SettingsActivity.class);
			startActivity(settingsActivity);
			return true;
		} else if (item.getItemId() == R.id.menu_refresh) {
			Object tag = getSupportActionBar().getSelectedTab().getTag();
			if (tag.equals(TAG_DOWNLOADING_TAB)) {
				downloadingListener.mFragment.onResume();
			} else if (tag.equals(TAG_HISTORY_TAB)) {
				historyListener.mFragment.onResume();
			}
			return true;
		} else if (item.getItemId() == R.id.menu_search) {
			this.onSearchRequested();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	private void togglePauseSabnzb() {
		SharedPreferences preferences = new ContextHelper(this).checkAndGetSettings();
		if (preferences != null) {
			String message;
			SabNzbConnectionHelper connectionHelper = new SabNzbConnectionHelper(
					preferences);
			if (paused) {
				message = connectionHelper.createResumeConnection();
			} else {
				message = connectionHelper.createPauseConnection();
			}
			new HttpGetTask(new MainCallback()).executeRequest(message);
		}
	}

	private class MainCallback extends DefaultErrorCallback {

		public void handleResponse(String response) {
			paused = !paused;
			invalidateOptionsMenu();
		}

		public void handleTimeout() {
			super.handleTimeout(MainActivity.this);
		}

		public void handleError(String error) {
			super.handleError(MainActivity.this, error);
		}

	}
}