package nl.napauleon.sabber.search;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import nl.napauleon.sabber.ContextHelper;
import nl.napauleon.sabber.R;
import nl.napauleon.sabber.SettingsActivity;
import nl.napauleon.sabber.http.DefaultErrorCallback;
import nl.napauleon.sabber.http.HttpGetTask;
import nl.napauleon.sabber.http.NzbIndexConnectionHelper;
import nl.napauleon.sabber.http.SabNzbConnectionHelper;

public class SearchableActivity extends SherlockListActivity {
    private static final String TAG = "SearchableActivity";

    private ProgressDialog dialog;
    private NzbIndexConnectionHelper nzbIndexConnectionHelper;
    private HttpGetTask httpGetTask;
    private String query;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        httpGetTask = new HttpGetTask(new SearchCallback());
        setContentView(R.layout.itemlist);
        Intent intent = getIntent();
        if (intent != null) {
            handleIntent(intent);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        setupSearchItem(this, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
		if (itemId == R.id.menu_settings) {
			Intent settingsActivity = new Intent(getBaseContext(),
			        SettingsActivity.class);
			startActivity(settingsActivity);
			return true;
		} else if (itemId == R.id.menu_search) {
            onSearchClicked(this, item);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            nzbIndexConnectionHelper = new NzbIndexConnectionHelper(PreferenceManager.getDefaultSharedPreferences(this));
            searchNzbs(query);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    RecentSearchesProvider.AUTHORITY, RecentSearchesProvider.MODE);
            suggestions.saveRecentQuery(query, null);
        }
    }

    private void searchNzbs(String query) {
        this.query = query;
        dialog = ProgressDialog.show(this, "", getString(R.string.title_loading), true);
        String searchString = nzbIndexConnectionHelper.createSearchString(query);
        Log.i(TAG, "searching with url: " + searchString);
        if (httpGetTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            httpGetTask.cancel(true);
        }
        httpGetTask = new HttpGetTask(new SearchCallback());
        httpGetTask.execute(searchString);
    }

    public class SearchCallback extends DefaultErrorCallback {

		public void handleError(String error) {
			showErrorDialog();
		}

		public void handleTimeout() {
			showErrorDialog();
		}
		
		public void handleResponse(String response) {
			List<NzbInfo> results = parseResults(response);
            setListAdapter(new SearchListAdapter(SearchableActivity.this, results, query));
            getListView().setOnItemClickListener(new SearchClickListener(results));
            if (dialog != null) {
                dialog.dismiss();
            }
		}

		private void showErrorDialog() {
			if (dialog != null) {
				dialog.dismiss();
			}
			new AlertDialog.Builder(SearchableActivity.this)
			.setMessage("Cannot connect to search provider")
			.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					SearchableActivity.this.finish();
				}
			})
			.show();
		}
		
		private List<NzbInfo> parseResults(String result) {
            try {
                return nzbIndexConnectionHelper.parseResults(result);
            } catch (IOException e) {
                handleParseException(e);
            } catch (SAXException e) {
                handleParseException(e);
            } catch (ParserConfigurationException e) {
                handleParseException(e);
            }
            return new ArrayList<NzbInfo>();
        }

        private void handleParseException(Exception e) {
            Log.e(TAG, "error parsing results", e);
            new ContextHelper(SearchableActivity.this).showErrorAlert(e.getMessage());
        }
    }

    private class SearchClickListener implements AdapterView.OnItemClickListener {

        private final List<NzbInfo> results;

        private SearchClickListener(List<NzbInfo> results) {
            this.results = results;
        }

        public void onItemClick(final AdapterView<?> parent, View view, final int position, final long rowId) {
            final Context context = parent.getContext();
            new AlertDialog.Builder(context)
                    .setMessage(context.getString(R.string.question_send_nzb))
                    .setPositiveButton(R.string.option_positive,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    SharedPreferences preferences = new ContextHelper(context).checkAndGetSettings();
                                    if (preferences != null) {
                                        String connectionString = new SabNzbConnectionHelper(preferences).createAddUrlConnectionString(results.get(position));
                                        new HttpGetTask(new SearchClickCallback()).execute(connectionString);
                                    }
                                }
                            })
                    .setNegativeButton(R.string.option_negative,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                    .show();
        }

        private class SearchClickCallback extends DefaultErrorCallback {

			public void handleResponse(String response) {
				SearchableActivity.this.finish();
			}

			public void handleTimeout() {
				super.handleTimeout(SearchableActivity.this);
				
			}

			public void handleError(String error) {
				super.handleError(SearchableActivity.this, error);
				
			}
        }
    }

    public static void setupSearchItem(final Activity activity, Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            if (searchView != null) {
                final SearchManager searchManager = (SearchManager) activity.getSystemService(
                        Context.SEARCH_SERVICE);
                searchView.setSearchableInfo(
                        searchManager.getSearchableInfo(activity.getComponentName()));
                searchView.setQueryRefinementEnabled(true);
            }
        }
    }

    public static void onSearchClicked(final Activity activity, MenuItem item) {
        SearchView searchView = (SearchView) item.getActionView();
        if (searchView == null) {
            activity.onSearchRequested();
        }
    }
}
