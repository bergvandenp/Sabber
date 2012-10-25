package nl.napauleon.sabber.search;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import nl.napauleon.sabber.ContextHelper;
import nl.napauleon.sabber.R;
import nl.napauleon.sabber.http.DefaultErrorCallback;
import nl.napauleon.sabber.http.HttpGetHandler;
import nl.napauleon.sabber.http.NzbIndexConnectionHelper;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchableActivity extends ListActivity{
    private static final String TAG = "SearchableActivity";

    private ProgressDialog dialog;
    private HttpGetHandler httpHandler;
    private NzbIndexConnectionHelper nzbIndexConnectionHelper;

    //for test purposes
    List<NzbInfo> getResults() {
        return results;
    }

    private List<NzbInfo> results = new ArrayList<NzbInfo>();

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
        getListView().setOnItemClickListener(new SearchClickListener(this, results));
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
            switch (msg.what) {
                case HttpGetHandler.MSG_RESULT:
                    results = parseResults(msg);
                    setListAdapter(new SearchListAdapter(SearchableActivity.this, results));

                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    break;
                default:
                    return false;
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
}