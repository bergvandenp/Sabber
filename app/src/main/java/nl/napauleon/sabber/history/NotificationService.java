package nl.napauleon.sabber.history;

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
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import nl.napauleon.sabber.Constants;
import nl.napauleon.sabber.ContextHelper;
import nl.napauleon.sabber.MainActivity;
import nl.napauleon.sabber.R;
import nl.napauleon.sabber.http.DefaultErrorCallback;
import nl.napauleon.sabber.http.HttpGetTask;
import org.json.JSONException;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {

	public static final int POLLING_INTERVAL = 5 * 1000;
	public static final String TAG = "NotificationService";

	private PendingIntent notificationIntent;
	private final int notificationId = 100;

	boolean notificationsEnabled = false;
	private Timer timer;

	@Override
	public void onCreate() {
		notificationIntent = PendingIntent.getActivity(
				NotificationService.this, 0, new Intent(getBaseContext(),
						MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "Notification Service starting");
		new ContextHelper().updateLastPollingEvent(NotificationService.this);
		timer = new Timer();
		final Handler handler = new Handler();
		final Runnable pollingThread = new Runnable() {
			public void run() {
				if (isNetworkConnected()) {
					new HttpGetTask(new HistoryCallback()).execute(createHistoryConnectionString());
				}
			}
		};

		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				Log.d(TAG, "Polling event occurred");
				handler.post(pollingThread);
			}
		}, 0, POLLING_INTERVAL);
		return START_STICKY;
	}

	boolean isNetworkConnected() {
		ConnectivityManager connectivityService = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityService.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}

	private long getLastPollingEvent() {
		return PreferenceManager.getDefaultSharedPreferences(this).getLong(
				Constants.LAST_POLLING_EVENT_PREF, 0L);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// no binding supported.
		return null;
	}

	@Override
	public void onDestroy() {
		timer.cancel();
		Log.d(TAG, "Notification Service stopped");
	}

	String createHistoryConnectionString() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(NotificationService.this);
		return String.format(
				"http://%s:%s/api?mode=history&limit=5&output=json&apikey=%s",
				preferences.getString(Constants.HOSTNAME_PREF, ""),
				preferences.getString(Constants.PORT_PREF, ""),
				preferences.getString(Constants.APIKEY_PREF, ""));
	}

	boolean shouldNotify(HistoryInfo historyItem) {

		Date itemDateDownloaded = historyItem.getDateDownloaded();
		Date lastPollingEvent = new Date(getLastPollingEvent());
		boolean shouldNotify = historyItem.isProcessingComplete()
				&& lastPollingEvent.before(itemDateDownloaded);
		if (shouldNotify) {
			Log.d(TAG,
					String.format(
							"ShouldNotify about %s. last polling event: %tT. item downloaded at: %tT",
							historyItem.getItem(), lastPollingEvent, itemDateDownloaded));
		}
		return shouldNotify;
	}

	private void sendNotification(HistoryInfo historyItem) {
		Log.i(TAG,
				"Sending notification for item " + historyItem.getItem()
						+ " with downloaddate: "
						+ historyItem.getDateDownloadedAsString());
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				this)
				.setContentIntent(notificationIntent)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(getNotificationTitle(historyItem))
				.setContentText(getNotificationContent(historyItem))
				.setSound(
						RingtoneManager.getActualDefaultRingtoneUri(this,
								RingtoneManager.TYPE_NOTIFICATION));
		notificationManager.notify(notificationId, builder.build());
	}

	private String getNotificationContent(HistoryInfo historyItem) {
		return historyItem.getItem() + (historyItem.getStatus() == Status.Failed 
				? " download failed." : " download complete.");
	}

	private String getNotificationTitle(HistoryInfo historyItem) {
		return historyItem.getStatus() == Status.Failed ? "Nzb download failed"
				: "Nzb download complete";
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
				List<HistoryInfo> historyItems = HistoryInfo
						.createHistoryList(response);
				for (HistoryInfo historyItem : historyItems) {
					if (shouldNotify(historyItem)) {
						sendNotification(historyItem);
						new ContextHelper()
								.updateLastPollingEvent(NotificationService.this);
					}
				}
			} catch (JSONException e) {
				Log.e(TAG, "Error parsing history information", e);
			}
		}
	}
}
