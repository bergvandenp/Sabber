package nl.napauleon.downloadmanager;

import android.content.Intent;
import android.os.Bundle;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import nl.napauleon.downloadmanager.Queue.DownloadingFragment;
import nl.napauleon.downloadmanager.history.HistoryFragment;

public class MainActivity extends SherlockFragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initializeTabs();
    }

	private void initializeTabs() {
		ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(false);
        
        actionBar.addTab(actionBar.newTab()
                .setText(R.string.downloading)
                .setTabListener(new TabListener<DownloadingFragment>(
                        this, "downloading", DownloadingFragment.class)));
        
        actionBar.addTab(actionBar.newTab()
                .setText(R.string.history)
                .setTabListener(new TabListener<HistoryFragment>(
                        this, "history", HistoryFragment.class)));
        
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent settingsActivity = new Intent(getBaseContext(),
                        Settings.class);
                startActivity(settingsActivity);
                return true;
            case R.id.menu_refresh:
            	Intent i = new Intent(getIntent());
                i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); 
                startActivity(i);
                return true;
            case R.id.menu_search:
                this.onSearchRequested();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}