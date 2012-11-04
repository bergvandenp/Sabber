package nl.napauleon.sabber.history;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;
import nl.napauleon.sabber.ContextHelper;
import nl.napauleon.sabber.MainActivity;
import nl.napauleon.sabber.R;
import nl.napauleon.sabber.Settings;
import nl.napauleon.sabber.http.HttpGetHandler;
import org.json.JSONException;

import java.util.List;

public class NotificationService extends Service {

    public static final int POLLING_INTERVAL = 5000;
    public static final String TAG = "NotificationService";

    private NotificationService.ServiceHandler serviceHandler;
    private Runnable pollingThread;
    private long last_polling_event;
    private HttpGetHandler httpHandler;
    private PendingIntent notificationIntent;
    private final int notificationId = 100;

    @Override
    public void onCreate() {
        HandlerThread notificationThread = new HandlerThread("NotificationThread", Process.THREAD_PRIORITY_BACKGROUND);
        notificationThread.start();
        serviceHandler = new ServiceHandler(notificationThread.getLooper());
        httpHandler = new HttpGetHandler(new HistoryCallback());

        notificationIntent = PendingIntent.getActivity(NotificationService.this, 0, new Intent(getBaseContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Notification Service starting");
        last_polling_event = PreferenceManager.getDefaultSharedPreferences(this).getLong(ContextHelper.LAST_POLLING_EVENT_PREF, System.currentTimeMillis());
        pollingThread = new Runnable() {
            public void run() {
                serviceHandler.sendMessage(serviceHandler.obtainMessage());
                serviceHandler.postDelayed(pollingThread, POLLING_INTERVAL);
            }
        };
        serviceHandler.postDelayed(pollingThread, POLLING_INTERVAL);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //no binding supported.
        return null;
    }

    @Override
    public void onDestroy() {
        serviceHandler.removeCallbacks(pollingThread);
        Log.d(TAG, "Notification Service stopped");
    }

    private boolean notifyDownloadedItem(HistoryInfo historyItem) {

        long itemDateDownloaded = historyItem.getDateDownloaded().getTime();
        boolean shouldNotify = last_polling_event < itemDateDownloaded;
        Log.d(TAG, String.format("ShouldNotify: %s. last polling event: %s. item downloaded %s",
                shouldNotify, last_polling_event, itemDateDownloaded));
        return shouldNotify;
    }

    private void sendNotification(HistoryInfo historyItem) {
        Log.i(TAG, "Sending notification for item " + historyItem.getItem());
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(this)
                .setContentIntent(notificationIntent)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Nzb download complete")
                .setContentText(historyItem.getItem() + " download complete.")
                .getNotification();
        notificationManager.notify(notificationId, notification);
    }

    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "Polling message received.");
            ConnectivityManager connectivityService = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityService.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                httpHandler.executeRequest(createHistoryConnectionString());
            }
        }

        String createHistoryConnectionString() {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(NotificationService.this);
            return String.format("http://%s:%s/api?mode=history&limit=5&output=json&apikey=%s",
                    preferences.getString(Settings.HOSTNAME_PREF, ""),
                    preferences.getString(Settings.PORT_PREF, ""),
                    preferences.getString(Settings.APIKEY_PREF, ""));
        }
    }

    private class HistoryCallback implements Handler.Callback {

        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case HttpGetHandler.MSG_RESULT:
                    handleResult(msg);
                    return true;
            }
            return false;
        }

        private void handleResult(Message msg) {
            try {
                List<HistoryInfo> historyItems = HistoryInfo.createHistoryList((String) msg.obj);
                for (HistoryInfo historyItem : historyItems) {
                    if (notifyDownloadedItem(historyItem)) {
                        sendNotification(historyItem);
                        last_polling_event = new ContextHelper().updateLastPollingEvent(NotificationService.this);
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing history information", e);
            }
        }

    }
}
