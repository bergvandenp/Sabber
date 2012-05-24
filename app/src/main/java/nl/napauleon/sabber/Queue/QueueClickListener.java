package nl.napauleon.sabber.Queue;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import nl.napauleon.sabber.ContextHelper;
import nl.napauleon.sabber.R;
import nl.napauleon.sabber.RefreshHandler;
import nl.napauleon.sabber.http.HttpGetTask;

import java.util.Arrays;
import java.util.List;

public class QueueClickListener implements AdapterView.OnItemClickListener {

    private Fragment fragment;
    private final List<QueueInfo> queueItems;
    private List<String> categories;

    public QueueClickListener(Fragment fragment, List<QueueInfo> queueItems, List<String> categories) {
        this.fragment = fragment;
        this.queueItems = queueItems;
        this.categories = categories;
    }

    public void onItemClick(final AdapterView<?> adapterView, View view, int i, long l) {
        final QueueInfo queueInfo = queueItems.get((int) l);
        Context context = adapterView.getContext();
        final SharedPreferences preferences = new ContextHelper().checkAndGetSettings(context);
        if (preferences != null) {
            final AlertDialog.Builder categoryAlert = new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.title_select_category))
                    .setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, categories),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    new HttpGetTask(new RefreshHandler(fragment))
                                            .execute(createConnectionChangeCategory(preferences, queueInfo.getId(), categories.get(i)));
                                }
                            });

            final AlertDialog.Builder deleteAlert = new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.question_delete_nzb))
                    .setCancelable(false)
                    .setPositiveButton(context.getString(R.string.option_positive),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    new HttpGetTask(new RefreshHandler(fragment))
                                            .execute(createConnectionDeleteItem(preferences, queueInfo.getId()));
                                }
                            })
                    .setNegativeButton(context.getString(R.string.option_negative),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

            new AlertDialog.Builder(context)
                    .setTitle("Choose action")
                    .setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,
                            Arrays.asList(context.getString(R.string.option_change_category),
                                    context.getString(R.string.option_delete_nzb))),
                            new DialogInterface.OnClickListener() {
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

    }

    private String createConnectionDeleteItem(SharedPreferences preferences, String itemId) {
        return String.format("http://%s:%s/api" +
                "?mode=queue" +
                "&name=delete" +
                "&apikey=%s" +
                "&value=%s",
                preferences.getString(ContextHelper.HOSTNAME_PREF, ""),
                preferences.getString(ContextHelper.PORT_PREF, ""),
                preferences.getString(ContextHelper.APIKEY_PREF, ""),
                itemId);
    }

    private String createConnectionChangeCategory(SharedPreferences preferences, String itemId, String category) {
        return String.format("http://%s:%s/api" +
                "?mode=change_cat" +
                "&value=%s" +
                "&value2=%s" +
                "&apikey=%s",
                preferences.getString(ContextHelper.HOSTNAME_PREF, ""),
                preferences.getString(ContextHelper.PORT_PREF, ""),
                itemId, category,
                preferences.getString(ContextHelper.APIKEY_PREF, ""));
    }
}
