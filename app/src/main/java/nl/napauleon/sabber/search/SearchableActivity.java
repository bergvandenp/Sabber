package nl.napauleon.sabber.search;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import nl.napauleon.sabber.ContextHelper;
import nl.napauleon.sabber.R;
import nl.napauleon.sabber.http.DefaultErrorCallback;
import nl.napauleon.sabber.http.HttpGetHandler;
import nl.napauleon.sabber.http.NzbIndexConnectionHelper;
import nl.napauleon.sabber.http.SabNzbConnectionHelper;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchableActivity extends ListActivity {
    private static final String TAG = "SearchableActivity";

    private ProgressDialog dialog;
    private HttpGetHandler httpHandler;
    private NzbIndexConnectionHelper nzbIndexConnectionHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        httpHandler = new HttpGetHandler(new SearchCallback());
        setContentView(R.layout.itemlist);
        Intent intent = getIntent();
        if (intent != null) {
            handleIntent(intent);
        }
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            nzbIndexConnectionHelper = new NzbIndexConnectionHelper(PreferenceManager.getDefaultSharedPreferences(this));
            searchNzbs(intent.getStringExtra(SearchManager.QUERY));
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void searchNzbs(String query) {
        dialog = ProgressDialog.show(this, "", getString(R.string.title_loading), true);
        String searchString = nzbIndexConnectionHelper.createSearchString(query);
        Log.i(TAG, "searching with url: " + searchString);
        httpHandler.executeRequest(searchString);
    }

    public class SearchCallback extends DefaultErrorCallback {
        protected SearchCallback() {
            super(SearchableActivity.this);
        }

        public boolean handleMessage(Message msg) {
            if (!super.handleMessage(msg)) {
                switch (msg.what) {
                    case HttpGetHandler.MSG_RESULT:
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
        private HttpGetHandler httpTask;

        private SearchClickListener(List<NzbInfo> results) {
            httpTask = new HttpGetHandler(new SearchClickCallback());
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
                                        httpTask.executeRequest(connectionString);
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
                        case HttpGetHandler.MSG_RESULT:
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