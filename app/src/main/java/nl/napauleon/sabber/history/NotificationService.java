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
import nl.napauleon.sabber.http.HttpGetMockTask;
import nl.napauleon.sabber.http.HttpGetTask;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import java.util.*;

public class NotificationService extends Service {

	public static final long DEFAULT_POLLING_INTERVAL = 60 * 1000;
	public static final String TAG = "NotificationService";

	private PendingIntent notificationIntent;
	private final int notificationId = 100;

	private Timer timer;

	final Handler handler = new Handler();
    private ContextHelper contextHelper;
    final Runnable pollingThread = new Runnable() {
		public void run() {
            if (contextHelper.isMockEnabled()) {
                new HttpGetMockTask(new HistoryCallback()).execute("history/historyresult");
                return;
            }
			String connectionString = createHistoryConnectionString();
			if (isNetworkConnected() && StringUtils.isNotBlank(connectionString)) {
				new HttpGetTask(new HistoryCallback()).execute(connectionString);
			}
		}
	};

    @Override
	public void onCreate() {
        contextHelper = new ContextHelper(NotificationService.this);
		notificationIntent = PendingIntent.getActivity(
				NotificationService.this, 0, new Intent(getBaseContext(),
						MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
		Log.d(TAG, "Notification Service starting");
        updateLastPollingEvent();
        timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				Log.d(TAG, String.format("Polling event occurred at time: %tT", new Date()));
				handler.post(pollingThread);
			}
		}, 0, getPollingInterval());
	}

    private void updateLastPollingEvent() {
        Calendar initialTime = Calendar.getInstance();
        if (contextHelper.isMockEnabled()) {
            initialTime.add(Calendar.YEAR, -1);
        }
        contextHelper.updateLastPollingEvent(initialTime.getTimeInMillis());
    }


    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
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

	private long getPollingInterval() {
		String userPollingInterval = PreferenceManager.getDefaultSharedPreferences(this).getString(
				Constants.NOTIFICATIONS_REFRESHRATE_PREF, Long.toString(DEFAULT_POLLING_INTERVAL / 1000));
		try {
			return Long.parseLong(userPollingInterval) * 1000;
		} catch (NumberFormatException e) {
			Log.w(TAG, String.format("user entered invalid interval value: %s. Falling back to default.", userPollingInterval));
			return DEFAULT_POLLING_INTERVAL;
		}
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

	String createHistoryConnectionString() {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(NotificationService.this);
		String hostname = preferences.getString(Constants.HOSTNAME_PREF, "");
		String port = preferences.getString(Constants.PORT_PREF, "");
		String apikey = preferences.getString(Constants.APIKEY_PREF, "");
		if (StringUtils.isNotBlank(hostname) && StringUtils.isNotBlank(port)) {
			return String.format(
					"http://%s:%s/api?mode=history&limit=5&output=json&apikey=%s",
					hostname,
					port,
					apikey);
		} else {
			return null;
		}
		
	}

	boolean shouldNotify(HistoryInfo historyItem) {

		Date itemDateDownloaded = historyItem.getDateDownloaded();
		Date lastPollingEvent = new Date(getLastPollingEvent());
		boolean shouldNotify = historyItem.isProcessingComplete()
				&& lastPollingEvent.before(itemDateDownloaded)
                && new Date().after(itemDateDownloaded);
		if (shouldNotify) {
			Log.d(TAG, String.format( "ShouldNotify about %s. last polling event: %tT. item downloaded at: %tT",
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

	String getNotificationContent(HistoryInfo historyItem) {
		return historyItem.getStatus() == Status.Failed
                ? getString(R.string.notification_content_failed, historyItem.getItem())
                : getString(R.string.notification_content_completed, historyItem.getItem());
	}

	String getNotificationTitle(HistoryInfo historyItem) {
		return historyItem.getStatus() == Status.Failed ? getString(R.string.notification_title_failed)
				: getString(R.string.notification_title_completed);
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
						contextHelper.updateLastPollingEvent(System.currentTimeMillis());
					}
				}
			} catch (JSONException e) {
				Log.e(TAG, "Error parsing history information", e);
			}
		}
	}
}
