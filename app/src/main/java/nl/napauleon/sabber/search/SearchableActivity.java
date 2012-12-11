package nl.napauleon.sabber.search;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import nl.napauleon.sabber.ContextHelper;
import nl.napauleon.sabber.R;
import nl.napauleon.sabber.SettingsActivity;
import nl.napauleon.sabber.http.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchableActivity extends ListActivity {
    private static final String TAG = "SearchableActivity";

    private NzbIndexConnectionHelper nzbIndexConnectionHelper;
    private HttpGetTask httpGetTask;
    private List<String> categories = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

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
			this.onSearchRequested();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            executeGetCategories();
            nzbIndexConnectionHelper = new NzbIndexConnectionHelper(PreferenceManager.getDefaultSharedPreferences(this));
            searchNzbs(intent.getStringExtra(SearchManager.QUERY));
        }
    }

    private void executeGetCategories() {

        ContextHelper contextHelper = new ContextHelper(this);
        GetCategoriesCallback getCategoriesCallback = new GetCategoriesCallback();
        if (contextHelper.isMockEnabled()) {
            new HttpGetMockTask(getCategoriesCallback).executeRequest("queue/queueresult");
        } else {
            new HttpGetTask(getCategoriesCallback).executeRequest(new SabNzbConnectionHelper(contextHelper.checkAndGetSettings()).createQueueConnectionString());
        }
    }

    private void searchNzbs(String query) {
        setProgressBarIndeterminateVisibility(true);
        ((TextView)findViewById(android.R.id.empty)).setText(R.string.title_loading);
        String searchString = nzbIndexConnectionHelper.createSearchString(query);
        Log.i(TAG, "searching with url: " + searchString);
        if (httpGetTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            httpGetTask.cancel(true);
        }
        httpGetTask = new HttpGetTask(new SearchCallback());
        httpGetTask.executeRequest(searchString);
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
            setListAdapter(new SearchListAdapter(SearchableActivity.this, results));
            getListView().setOnItemClickListener(new SearchClickListener(results));
            setProgressBarIndeterminateVisibility(false);
		}

		private void showErrorDialog() {
            setProgressBarIndeterminateVisibility(false);
			new AlertDialog.Builder(SearchableActivity.this)
			.setTitle("Cannot connect to search provider")
			.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
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
                    .setTitle(context.getString(R.string.question_send_nzb))
                    .setCancelable(false)
                    .setPositiveButton(context.getString(R.string.option_positive),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if (categories != null && !categories.isEmpty()) {
                                        createCategoryAlert(context, position).show();
                                    } else {
                                        executeAddUrlRequest(context, position);
                                    }
                                }
                            })
                    .setNegativeButton(parent.getContext().getString(R.string.option_negative),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                    .show();
        }

        private AlertDialog.Builder createCategoryAlert(final Context context, final int selectedItemPosition) {
            return new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.title_select_category))
                    .setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, categories),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    executeAddUrlRequest(context, selectedItemPosition, categories.get(i));
                                }
                            });
        }

        private void executeAddUrlRequest(Context context, int position, String category) {
            SharedPreferences preferences = new ContextHelper(context).checkAndGetSettings();
            if (preferences != null) {
                String connectionString = new SabNzbConnectionHelper(preferences).createAddUrlConnectionString(results.get(position), category);
                new HttpGetTask(new AddUrlCallback()).executeRequest(connectionString);
            }
        }

        private void executeAddUrlRequest(Context context, int position) {
            SharedPreferences preferences = new ContextHelper(context).checkAndGetSettings();
            if (preferences != null) {
                String connectionString = new SabNzbConnectionHelper(preferences).createAddUrlConnectionString(results.get(position));
                new HttpGetTask(new AddUrlCallback()).executeRequest(connectionString);
            }
        }

        private class AddUrlCallback extends DefaultErrorCallback {

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

    private class GetCategoriesCallback extends DefaultErrorCallback {

        public void handleTimeout() {
            super.handleTimeout(SearchableActivity.this);
        }

        public void handleError(String error) {
            super.handleError(SearchableActivity.this, error);
        }

        public void handleResponse(String response) {
            handleResult(response);

        }

        private void handleResult(String result) {
            SabnzbResultHelper sabnzbResultHelper = new SabnzbResultHelper(result);
            categories = sabnzbResultHelper.parseCategories();
        }
    }
}