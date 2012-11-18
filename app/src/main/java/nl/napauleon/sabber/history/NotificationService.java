package nl.napauleon.sabber.history;

import java.util.Date;
import java.util.List;

import nl.napauleon.sabber.Constants;
import nl.napauleon.sabber.ContextHelper;
import nl.napauleon.sabber.MainActivity;
import nl.napauleon.sabber.R;
import nl.napauleon.sabber.SettingsActivity;
import nl.napauleon.sabber.http.DefaultErrorCallback;
import nl.napauleon.sabber.http.HttpGetTask;

import org.json.JSONException;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class NotificationService extends Service {

    public static final int POLLING_INTERVAL = 5000;
    public static final String TAG = "NotificationService";

    private Handler serviceHandler;
    private Runnable pollingThread;
    private PendingIntent notificationIntent;
    private final int notificationId = 100;
    
    boolean notificationsEnabled = false;

    @Override
    public void onCreate() {
        HandlerThread notificationThread = new HandlerThread("NotificationThread", Process.THREAD_PRIORITY_BACKGROUND);
        notificationThread.start();
        notificationIntent = PendingIntent.getActivity(NotificationService.this, 0, new Intent(getBaseContext(), MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	serviceHandler = new Handler();
    	Log.d(TAG, "Notification Service starting");
    	notificationsEnabled = true;
    	new ContextHelper().updateLastPollingEvent(NotificationService.this);
    	pollingThread = new Runnable() {
            public void run() {
            		ConnectivityManager connectivityService = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connectivityService.getActiveNetworkInfo();
                    if (notificationsEnabled = true && networkInfo != null && networkInfo.isConnected()) {
                    	new HttpGetTask(new HistoryCallback()).execute(createHistoryConnectionString());
    	                serviceHandler.postDelayed(this, POLLING_INTERVAL);
            	}
            }
        };
        serviceHandler.postDelayed(pollingThread, POLLING_INTERVAL);
        return START_STICKY;
    }

	private long getLastPollingEvent() {
		return PreferenceManager.getDefaultSharedPreferences(this).getLong(Constants.LAST_POLLING_EVENT_PREF, 0L);
	}

    @Override
    public IBinder onBind(Intent intent) {
        //no binding supported.
        return null;
    }

    @Override
    public void onDestroy() {
        serviceHandler.removeCallbacks(pollingThread);
        notificationsEnabled = false;
        Log.d(TAG, "Notification Service stopped");
    }
    
    String createHistoryConnectionString() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(NotificationService.this);
        return String.format("http://%s:%s/api?mode=history&limit=5&output=json&apikey=%s",
                preferences.getString(SettingsActivity.HOSTNAME_PREF, ""),
                preferences.getString(SettingsActivity.PORT_PREF, ""),
                preferences.getString(SettingsActivity.APIKEY_PREF, ""));
    }

    private boolean notifyDownloadedItem(HistoryInfo historyItem) {

        Date itemDateDownloaded = historyItem.getDateDownloaded();
        Date lastPollingEvent = new Date(getLastPollingEvent());
		boolean shouldNotify = lastPollingEvent.before(itemDateDownloaded);
        Log.d(TAG, String.format("ShouldNotify about %s: %s. last polling event: %tT. item downloaded %tT",
                historyItem.getItem(), shouldNotify,  lastPollingEvent, itemDateDownloaded));
        return shouldNotify;
    }

    private void sendNotification(HistoryInfo historyItem) {
        Log.i(TAG, "Sending notification for item " + historyItem.getItem());
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentIntent(notificationIntent)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Nzb download complete")
                .setContentText(historyItem.getItem() + " download complete.")
                .setSound(RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION));
        notificationManager.notify(notificationId, builder.build());
    }
    
    private class HistoryCallback extends DefaultErrorCallback {
		public void handleError(String error) {
			Log.w(TAG, error);
		}

		public void handleTimeout() {
			Log.w(TAG, "connection timeout from notificationservice");
		}

		public void handleResponse(String response) {
			try {
                List<HistoryInfo> historyItems = HistoryInfo.createHistoryList(response);
                for (HistoryInfo historyItem : historyItems) {
                    if (notifyDownloadedItem(historyItem)) {
                        sendNotification(historyItem);
                        new ContextHelper().updateLastPollingEvent(NotificationService.this);
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing history information", e);
            }
		}
    }
}
