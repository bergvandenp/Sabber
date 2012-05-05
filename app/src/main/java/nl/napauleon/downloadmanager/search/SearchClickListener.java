package nl.napauleon.downloadmanager.search;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import nl.napauleon.downloadmanager.http.HttpGetTask;
import nl.napauleon.downloadmanager.http.RefreshMainAsyncTaskHandler;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: napauleon
 * Date: 3/4/12
 * Time: 5:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class SearchClickListener implements AdapterView.OnItemClickListener {

    private final String hostname;
    private final String port;
    private final String apikey;
    private final List<NzbInfo> results;
    private static final String ENCODING = "UTF-8";
    private static final String TAG = "SearchClickListener";

    public SearchClickListener(SharedPreferences prefs, List<NzbInfo> results) {
        this.results = results;
        hostname = prefs.getString("hostnamePref", "");
        port = prefs.getString("portPref", "");
        apikey = prefs.getString("apikeyPref", "");
    }

    String createConnectionString(NzbInfo nzbInfo) {
        try {

            String link = nzbInfo.getLink();
            String name = nzbInfo.getTitle();
            Pattern pattern = Pattern.compile("\"(.*?)\"");
            Matcher matcher = pattern.matcher(nzbInfo.getTitle());
            if (matcher.find()) {
                name = matcher.group(1);
            }
            return "http://" + hostname + ":" + port + "/api?mode=addurl&apikey=" + apikey
                    + "&name=" + URLEncoder.encode(link, ENCODING)
                    + "&nzbname=" + URLEncoder.encode(name, ENCODING);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unsupported encoding: " + ENCODING, e);
        }
        return null;
    }

    public void onItemClick(final AdapterView<?> parent, View view, final int position, final long rowId) {
        new AlertDialog.Builder(parent.getContext())
                .setTitle("Send this nzb to downloadserver?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (hostname == null || hostname.isEmpty() || port == null || port.isEmpty()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(parent.getContext());
                            builder.setMessage("Settings are not valid. Check host and port configuration.").setNeutralButton("Ok", null).show();
                        } else {
                            new HttpGetTask(new RefreshMainAsyncTaskHandler(parent.getContext())).execute(createConnectionString(results.get(position)));
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .show();
    }
}
