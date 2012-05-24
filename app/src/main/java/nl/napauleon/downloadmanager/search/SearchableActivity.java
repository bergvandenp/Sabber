package nl.napauleon.downloadmanager.search;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import nl.napauleon.downloadmanager.ContextHelper;
import nl.napauleon.downloadmanager.R;
import nl.napauleon.downloadmanager.http.HttpGetTask;
import nl.napauleon.downloadmanager.http.HttpHandler;
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

public class SearchableActivity extends ListActivity{

    private static final String MINSIZE_PREF = "minsizePref";
    private static final String TAG = "SearchableActivity";

    private ProgressDialog dialog;
    private static final String ENCODING = "UTF-8";
    private final List<NzbInfo> results = new ArrayList<NzbInfo>();

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
        String searchString = createSearchString(query);
        Log.i(TAG, "searching with url: " + searchString);
        new HttpGetTask(new SearchHandler()).execute(searchString);
    }

    private String createSearchString(String query) {
        String minsize = PreferenceManager.getDefaultSharedPreferences(this).getString(MINSIZE_PREF, "0");
        try {
            return String.format("http://nzbindex.nl/rss/?q=%s&sort=agedesc&max=20&minsize=%s",
                    URLEncoder.encode(query, ENCODING),
                    minsize);
        } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Unsupported encoding: " + ENCODING, e);
            return null;
        }
    }

    class SearchHandler extends HttpHandler {
        public SearchHandler() {
            super(SearchableActivity.this);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HttpGetTask.MSG_RESULT:
                    results.clear();

                    Document document = XMLfromString((String) msg.obj);
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
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

        private Document XMLfromString(String xml) {
            try {
                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(xml));
                return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
            } catch (ParserConfigurationException e) {
                Log.e(TAG, "error reading rss", e);
                new ContextHelper().showErrorAlert(SearchableActivity.this, e.getMessage());
            } catch (SAXException e) {
                Log.e(TAG, "error reading rss", e);
                new ContextHelper().showErrorAlert(SearchableActivity.this, e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "error reading rss", e);
                new ContextHelper().showErrorAlert(SearchableActivity.this, e.getMessage());
            }
            return null;
        }
    }


}
