package nl.napauleon.sabber.search;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import nl.napauleon.sabber.ContextHelper;
import nl.napauleon.sabber.R;
import nl.napauleon.sabber.Settings;
import nl.napauleon.sabber.http.DefaultErrorCallback;
import nl.napauleon.sabber.http.HttpGetTask;
import nl.napauleon.sabber.http.NzbIndexConnectionHelper;
import nl.napauleon.sabber.http.SabNzbConnectionHelper;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static nl.napauleon.sabber.Constants.MSG_RESULT;

public class SearchableActivity extends ListActivity {
    private static final String TAG = "SearchableActivity";

    private ProgressDialog dialog;
    private NzbIndexConnectionHelper nzbIndexConnectionHelper;
    private HttpGetTask httpGetTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        httpGetTask = new HttpGetTask(new Handler(new SearchCallback()));
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
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent settingsActivity = new Intent(getBaseContext(),
                        Settings.class);
                startActivity(settingsActivity);
                return true;
            case R.id.menu_search:
                this.onSearchRequested();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            nzbIndexConnectionHelper = new NzbIndexConnectionHelper(PreferenceManager.getDefaultSharedPreferences(this));
            searchNzbs(intent.getStringExtra(SearchManager.QUERY));
        }
    }

    private void searchNzbs(String query) {
        dialog = ProgressDialog.show(this, "", getString(R.string.title_loading), true);
        String searchString = nzbIndexConnectionHelper.createSearchString(query);
        Log.i(TAG, "searching with url: " + searchString);
        if (httpGetTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            httpGetTask.cancel(true);
        }
        httpGetTask.execute(searchString);
    }

    public class SearchCallback extends DefaultErrorCallback {
        protected SearchCallback() {
            super(SearchableActivity.this);
        }

        public boolean handleMessage(Message msg) {
            if (!super.handleMessage(msg)) {
                switch (msg.what) {
                    case MSG_RESULT:
                        List<NzbInfo> results = parseResults(msg);
                        setListAdapter(new SearchListAdapter(SearchableActivity.this, results));
                        getListView().setOnItemClickListener(new SearchClickListener(results));
                        break;
                    default:
                        return false;
                }
            } else {
                new AlertDialog.Builder(SearchableActivity.this)
                        .setTitle("Cannot connect to search provider")
                        .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                SearchableActivity.this.finish();
                            }
                        })
                        .show();
            }
            if (dialog != null) {
                dialog.dismiss();
            }
            return true;
        }

        private List<NzbInfo> parseResults(Message msg) {
            try {
                return nzbIndexConnectionHelper.parseResults((String) msg.obj);
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
            new ContextHelper().showErrorAlert(SearchableActivity.this, e.getMessage());
        }
    }

    private class SearchClickListener implements AdapterView.OnItemClickListener {

        private final List<NzbInfo> results;
        private Handler httpTask;

        private SearchClickListener(List<NzbInfo> results) {
            httpTask = new Handler(new SearchClickCallback());
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
                                    SharedPreferences preferences = new ContextHelper().checkAndGetSettings(context);
                                    if (preferences != null) {
                                        String connectionString = new SabNzbConnectionHelper(preferences).createAddUrlConnectionString(results.get(position));
                                        new HttpGetTask(httpTask).execute(connectionString);
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

        private class SearchClickCallback extends DefaultErrorCallback {

            private SearchClickCallback() {
                super(SearchableActivity.this);
            }

            @Override
            public boolean handleMessage(Message msg) {
                boolean messageHandled = super.handleMessage(msg);
                if (!messageHandled) {
                    switch (msg.what) {
                        case MSG_RESULT:
                            SearchableActivity.this.finish();
                            return true;
                        default:
                            return false;
                    }
                } else {
                    SearchableActivity.this.finish();
                    return true;
                }
            }

        }
    }
}