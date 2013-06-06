package nl.napauleon.sabber.http;

import android.content.SharedPreferences;
import android.util.Log;
import nl.napauleon.sabber.search.NzbInfo;
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

public class NzbxConnectionHelper {

    private static final String TAG = "NzbIndexConnectionHelper";
    private static final String ENCODING = "UTF-8";
    private static final String MINSIZE_PREF = "minsizePref";
    private static final String URL = "https://nzbx.co/api/search";
    private String itemMinSize;

    public NzbxConnectionHelper(SharedPreferences preferences) {
        itemMinSize = preferences.getString(MINSIZE_PREF, "0");
    }

    public String createSearchString(String query) {
        try {
            return String.format("%s?q=%s&l=20&sf=%s",
                    URL,
                    URLEncoder.encode(query, ENCODING),
                    itemMinSize);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unsupported encoding: " + ENCODING, e);
            return null;
        }
    }

    public List<NzbInfo> parseResults(String searchResults) throws IOException, SAXException, ParserConfigurationException {
        List<NzbInfo> results = new ArrayList<NzbInfo>();

        return results;
    }
}
