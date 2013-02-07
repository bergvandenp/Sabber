package nl.napauleon.sabber;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;

import nl.napauleon.sabber.history.HistoryFragment;
import nl.napauleon.sabber.history.NotificationService;
import nl.napauleon.sabber.http.DefaultErrorCallback;
import nl.napauleon.sabber.http.HttpGetTask;
import nl.napauleon.sabber.http.SabNzbConnectionHelper;
import nl.napauleon.sabber.queue.DownloadingFragment;
import nl.napauleon.sabber.search.SearchableActivity;

public class MainActivity extends SherlockFragmentActivity {

	private static final String TAG_HISTORY_TAB = "history";
	private static final String TAG_DOWNLOADING_TAB = "downloading";
	private boolean paused = false;
    private Menu optionsMenu;
    private boolean deferRefreshing;
    private ViewPager pager;
    private DownloadingFragment downloadingFragment;
    private HistoryFragment historyFragment;

    public void setPaused(boolean paused) {
		this.paused = paused;
	}

    public boolean isPaused() {
        return paused;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		ContextHelper contextHelper = new ContextHelper(this);
		if (!contextHelper.isSabnzbSettingsPresent()) {
			Intent settingsActivity = new Intent(this, SettingsActivity.class);
			startActivity(settingsActivity);
		}
		if (contextHelper.isNotificationsEnabled()) {
			Intent intent = new Intent(this, NotificationService.class);
			startService(intent);
		}

        // set up viewpager
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                getSupportActionBar().setSelectedNavigationItem(position);
            }
        });
        pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                switch (i) {
                    case 0: return (downloadingFragment = new DownloadingFragment());
                    case 1: return (historyFragment = new HistoryFragment());
                }
                return null;
            }

            @Override
            public int getCount() {
                return 2;
            }
        });
        pager.setPageMarginDrawable(R.drawable.list_divider_holo_light);
        pager.setPageMargin((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));

        // set up tabs
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(false);

        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                pager.setCurrentItem(tab.getPosition());
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            }
        };

        actionBar.addTab(actionBar.newTab().setText(R.string.downloading)
                .setTabListener(tabListener).setTag(TAG_DOWNLOADING_TAB));
        actionBar.addTab(actionBar.newTab().setText(R.string.history)
                .setTabListener(tabListener).setTag(TAG_HISTORY_TAB));
    }

    public void setRefreshing(boolean refreshing) {
        if (optionsMenu == null) {
            if (refreshing) {
                deferRefreshing = true;
            }
            return;
        }

        deferRefreshing = false;
        final MenuItem refreshItem = optionsMenu.findItem(R.id.menu_refresh);
        if (refreshItem != null) {
            if (refreshing) {
                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                refreshItem.setActionView(null);
            }
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
        SearchableActivity.setupSearchItem(this, menu);
        optionsMenu = menu;
        if (deferRefreshing) {
            setRefreshing(true);
        }
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
			if (tag.equals(TAG_DOWNLOADING_TAB) && downloadingFragment != null) {
				downloadingFragment.onResume();
			} else if (tag.equals(TAG_HISTORY_TAB) && historyFragment != null) {
				historyFragment.onResume();
			}
			return true;
		} else if (item.getItemId() == R.id.menu_search) {
            SearchableActivity.onSearchClicked(this, item);
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
			new HttpGetTask(new MainCallback()).execute(message);
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
