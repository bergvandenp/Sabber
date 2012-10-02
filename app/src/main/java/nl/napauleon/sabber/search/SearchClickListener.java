package nl.napauleon.sabber.search;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import nl.napauleon.sabber.ContextHelper;
import nl.napauleon.sabber.R;
import nl.napauleon.sabber.http.DefaultErrorCallback;
import nl.napauleon.sabber.http.HttpGetHandler;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchClickListener implements AdapterView.OnItemClickListener{

    private static final String ENCODING = "UTF-8";
    private static final String TAG = "SearchClickListener";

    private final List<NzbInfo> results;
    private final Activity activity;
    private HttpGetHandler httpTask;

    public SearchClickListener(Activity activity, List<NzbInfo> results) {
        httpTask = new HttpGetHandler(new SearchClickCallback());
        this.activity = activity;
        this.results = results;
    }

    String createConnectionString(SharedPreferences preferences, NzbInfo nzbInfo) {
        try {

            String link = nzbInfo.getLink();
            String name = nzbInfo.getTitle();
            Pattern pattern = Pattern.compile("\"(.*?)\"");
            Matcher matcher = pattern.matcher(nzbInfo.getTitle());
            if (matcher.find()) {
                name = matcher.group(1);
            }
            return String.format("http://%s:%s/api?mode=addurl&apikey=%s&name=%s&nzbname=%s",
                    preferences.getString(ContextHelper.HOSTNAME_PREF, ""),
                    preferences.getString(ContextHelper.PORT_PREF, ""),
                    preferences.getString(ContextHelper.APIKEY_PREF, ""),
                    URLEncoder.encode(link, ENCODING),
                    URLEncoder.encode(name, ENCODING));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unsupported encoding: " + ENCODING, e);
        }
        return null;
    }

    public void onItemClick(final AdapterView<?> parent, View view, final int position, final long rowId) {
        new AlertDialog.Builder(parent.getContext())
                .setTitle(parent.getContext().getString(R.string.question_send_nzb))
                .setCancelable(false)
                .setPositiveButton(parent.getContext().getString(R.string.option_positive),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SharedPreferences preferences = new ContextHelper().checkAndGetSettings(parent.getContext());
                                if (preferences != null) {

                                    String connectionString = createConnectionString(preferences, results.get(position));
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
            super(SearchClickListener.this.activity);
        }

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case HttpGetHandler.MSG_RESULT:
                    activity.finish();
                    break;
                default:
                    return false;
            }
            return true;
        }

    }
}
