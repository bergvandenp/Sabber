package nl.napauleon.sabber.http;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import nl.napauleon.sabber.Constants;
import nl.napauleon.sabber.search.NzbInfo;
import org.apache.commons.lang3.StringUtils;
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

public class NzbSearchConnectionHelper {

    private static final String TAG = "NzbSearchConnectionHelper";
    private static final String ENCODING = "UTF-8";
    private static final String MINSIZE_PREF = "minsizePref";
    private final String apiKeySearch;
    private final String searchProvider;

    public NzbSearchConnectionHelper(SharedPreferences preferences) {
        apiKeySearch = preferences.getString(Constants.APIKEY_SEARCH_PREF, "");
        searchProvider = preferences.getString(Constants.SEARCH_PROVIDER_PREF, "");
        // itemMinSize = preferences.getString(MINSIZE_PREF, "0");
    }

    public String createSearchString(String query) {
        if (StringUtils.isNotBlank(apiKeySearch) && StringUtils.isNotBlank(searchProvider)) {
            try {
                return String.format("%s/api?t=search&apikey=%s&q=%s",
                        searchProvider,
                        apiKeySearch,
                        URLEncoder.encode(query, ENCODING));
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Unsupported encoding: " + ENCODING, e);
                return null;
            }
        }
        return null;
    }

    public List<NzbInfo> parseResults(String searchResults) throws IOException, SAXException, ParserConfigurationException {
        List<NzbInfo> results = new ArrayList<NzbInfo>();
        Document document = XMLfromString(searchResults);
        NodeList nodeList = document.getElementsByTagName("item");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element item = (Element) nodeList.item(i);
            NodeList childNodes = item.getElementsByTagName("title").item(0).getChildNodes();
            String title = "";
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node childNode = childNodes.item(j);
                title = title + childNode.getNodeValue();
            }
            Element enclosure = (Element) item.getElementsByTagName("enclosure").item(0);
            String link = enclosure.getAttribute("url");
            Long size = Long.valueOf(enclosure.getAttribute("length")) / 1024 / 1024;
            results.add(new NzbInfo(title, link, size + "MB"));
        }
        return results;
    }

    private Document XMLfromString(String xml) throws ParserConfigurationException, SAXException, IOException {
        InputSource is = new InputSource();
        StringReader stringReader = null;
        try {
            stringReader = new StringReader(xml);
            is.setCharacterStream(stringReader);
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
        } catch (ParserConfigurationException e) {
            Log.e(TAG, "error reading rss", e);
            throw(e);
        } catch (SAXException e) {
            Log.e(TAG, "error reading rss", e);
            throw(e);
        } catch (IOException e) {
            Log.e(TAG, "error reading rss", e);
            throw(e);
        } finally {
            if (stringReader != null) {
                stringReader.close();
            }
        }
    }
}
