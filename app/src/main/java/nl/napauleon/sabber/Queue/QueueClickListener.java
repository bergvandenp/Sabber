package nl.napauleon.sabber.queue;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import nl.napauleon.sabber.ContextHelper;
import nl.napauleon.sabber.MainActivity;
import nl.napauleon.sabber.R;
import nl.napauleon.sabber.http.DefaultErrorCallback;
import nl.napauleon.sabber.http.HttpGetTask;
import nl.napauleon.sabber.http.SabNzbConnectionHelper;

import java.util.Arrays;
import java.util.List;

public class QueueClickListener implements AdapterView.OnItemClickListener{

    private List<QueueInfo> queueItems;
    private List<String> categories;
    private Context context;
	private QueueClickCallback queueClickCallback;
    

    public void onItemClick(final AdapterView<?> adapterView, View view, int i, long l) {
        queueClickCallback = new QueueClickCallback();
        final QueueInfo queueInfo = queueItems.get((int) l);
        context = adapterView.getContext();
        final SharedPreferences preferences = new ContextHelper(context).checkAndGetSettings();

        if (preferences != null) {

            new AlertDialog.Builder(context)
                    .setTitle("Choose action")
                    .setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1,
                            Arrays.asList(context.getString(R.string.option_change_category),
                                    context.getString(R.string.option_delete_nzb))),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int index) {
                                    switch (index) {
                                        case 0:
                                            createCategoryAlert(queueInfo, preferences).show();
                                            break;
                                        case 1:
                                            createDeleteAlert(queueInfo, preferences).show();
                                            break;
                                    }
                                }
                            })
                    .show();
        }

    }

    private AlertDialog.Builder createDeleteAlert(QueueInfo queueInfo, SharedPreferences preferences) {
        DialogInterface.OnClickListener deleteClickListener = createDeleteClickListener(queueInfo, preferences);
        DialogInterface.OnClickListener cancelClickListener = createCancelClickListener();

        return new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.question_delete_nzb))
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.option_positive),
                        deleteClickListener)
                .setNegativeButton(context.getString(R.string.option_negative),
                        cancelClickListener);
    }

    private AlertDialog.Builder createCategoryAlert(final QueueInfo queueInfo, final SharedPreferences preferences) {
        return new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.title_select_category))
                .setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, categories),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new HttpGetTask(queueClickCallback).execute(new SabNzbConnectionHelper(preferences).createChangeCategoryConnectionString(queueInfo.getId(), categories.get(i)));
                            }
                        });
    }

    private DialogInterface.OnClickListener createDeleteClickListener(final QueueInfo queueInfo, final SharedPreferences preferences) {
        return new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new HttpGetTask(queueClickCallback).execute(new SabNzbConnectionHelper(preferences).createDeleteItemConnectionString(queueInfo.getId()));
                    }
                };
    }

    private DialogInterface.OnClickListener createCancelClickListener() {
        return new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                };
    }



    public void setQueueItems(List<QueueInfo> queueItems) {
        this.queueItems = queueItems;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    private class QueueClickCallback extends DefaultErrorCallback {

		public void handleResponse(String response) {
			Intent intent = new Intent(context, MainActivity.class);
            context.startActivity(intent);
		}

		public void handleTimeout() {
			super.handleTimeout(context);
		}

		public void handleError(String error) {
			super.handleError(context, error);
			
		}
    }
}
