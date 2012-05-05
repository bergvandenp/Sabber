package nl.napauleon.downloadmanager.search;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import nl.napauleon.downloadmanager.R;
import nl.napauleon.downloadmanager.http.AsyncTaskHandler;
import nl.napauleon.downloadmanager.http.HttpGetTask;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class SearchableActivity extends ListActivity implements AsyncTaskHandler {

    private ProgressDialog dialog;
    private String query;
    private static final String ENCODING = "UTF-8";
    private final List<NzbInfo> results = new ArrayList<NzbInfo>();
    private static final String TAG = "SearchableActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.itemlist);
        Intent intent = getIntent();
        if(intent != null) {
        	handleIntent(intent);
        }
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            searchNzbs(query);
        }

        getListView().setOnItemClickListener(new SearchClickListener(PreferenceManager.getDefaultSharedPreferences(this), results));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    private void searchNzbs(String query) {
        dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
        String searchString = createSearchString(query);
        Log.i("DownloadManager", "searching with url: " + searchString);
        new HttpGetTask(this).execute(searchString);
    }

    private String createSearchString(String query) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SearchableActivity.this);
        String minsize = prefs.getString("minsizePref", "");
        try {
            return "http://nzbindex.nl/rss/?q=" + URLEncoder.encode(query, ENCODING)
                    + "&sort=agedesc&max=20"
                    + (!minsize.isEmpty() ? "&minsize=" + minsize : "");
        } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Unsupported encoding: " + ENCODING, e);
            return null;
        }
    }

    public void handleResult(String result) {
        Log.i("DownloadManager", result);
        results.clear();

        Document document = XMLfromString(result);
        NodeList nodeList = document.getElementsByTagName("item");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element item = (Element) nodeList.item(i);
            NodeList childNodes = item.getElementsByTagName("title").item(0).getChildNodes();
            String title = "";
            for(int j=0; j<childNodes.getLength(); j++){
                Node childNode = childNodes.item(j);
                title = title + childNode.getNodeValue();
            }
            Element enclosure = (Element) item.getElementsByTagName("enclosure").item(0);
            String link = enclosure.getAttribute("url");
            Long size = new Long(enclosure.getAttribute("length")) / 1024 / 1024;
            results.add(new NzbInfo(title, link, size + "MB"));
        }
        setListAdapter(new SearchListAdapter(SearchableActivity.this, results));

        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public Document XMLfromString(String xml) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));
            return dbf.newDocumentBuilder().parse(is);

        } catch (ParserConfigurationException e) {
            showErrorAlert(this, e);
        } catch (SAXException e) {
            showErrorAlert(this, e);
        } catch (IOException e) {
            showErrorAlert(this, e);
        }
        return null;
    }

    //todo move to generic class
    private void showErrorAlert(Context context, Exception e) {
        Log.e("DownloadManager", "error reading rss", e);
        AlertDialog.Builder builder = new AlertDialog.Builder(
                context);
        builder.setMessage(e.getMessage()).setNeutralButton("Ok", null)
                .show();
    }
}
