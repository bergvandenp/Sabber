package nl.napauleon.downloadmanager.Queue;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import nl.napauleon.downloadmanager.http.HttpGetTask;
import nl.napauleon.downloadmanager.http.RefreshMainAsyncTaskHandler;

import java.util.Arrays;
import java.util.List;

public class QueueClickListener implements AdapterView.OnItemClickListener{

    private final List<QueueInfo> queueItems;
    private List<String> categories;
    private String hostname;
    private String port;
    private String apikey;
    private Context context;

    public QueueClickListener(SharedPreferences prefs, List<QueueInfo> queueItems, List<String> categories) {
        this.queueItems = queueItems;
        this.categories = categories;
        hostname = prefs.getString("hostnamePref", "");
        port = prefs.getString("portPref", "");
        apikey = prefs.getString("apikeyPref", "");
    }

    public void onItemClick(final AdapterView<?> adapterView, View view, int i, long l) {
        final QueueInfo queueInfo = queueItems.get((int) l);
        context = adapterView.getContext();
        final AlertDialog.Builder categoryAlert = new AlertDialog.Builder(context)
                .setTitle("Pick a category")
                .setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, categories), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialogInterface, int i) {
                        new HttpGetTask(new RefreshMainAsyncTaskHandler(context)).execute(createConnectionChangeCategory(queueInfo.getId(), categories.get(i)));
                    }
                });

        final AlertDialog.Builder deleteAlert = new AlertDialog.Builder(context)
                .setTitle("Delete the selected nzb?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (hostname == null || hostname.isEmpty() || port == null || port.isEmpty()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setMessage("Settings are not valid. Check host and port configuration.").setNeutralButton("Ok", null).show();
                        } else {
                            new HttpGetTask(new RefreshMainAsyncTaskHandler(context)).execute(createConnectionDeleteItem(queueInfo.getId()));

                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        new AlertDialog.Builder(context)
                .setTitle("Choose action")
                .setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, Arrays.asList("Change category", "Delete item")), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int index) {
                        switch (index) {
                            case 0:
                                categoryAlert.show();
                                break;
                            case 1:
                                deleteAlert.show();
                                break;
                        }
                    }
                })
                .show();
    }

    private String createConnectionDeleteItem(String itemId) {
        return String.format("http://%s:%s/api" +
                "?mode=queue" +
                "&name=delete" +
                "&value=%s" +
                "&apikey=%s", hostname, port, itemId, apikey);
    }
    
    private String createConnectionChangeCategory(String itemId, String category) {
        return String.format("http://%s:%s/api" +
                "?mode=change_cat" +
                "&value=%s" +
                "&value2=%s" +
                "&apikey=%s", hostname, port, itemId, category, apikey);
    }
}
