package nl.napauleon.sabber.http;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HttpGetTask extends AsyncTask<String, Void, String> {

    public static final int MSG_RESULT = 1;
    public static final int MSG_CONNECTIONERROR = 2;
    public static final int MSG_CONNECTIONTIMEOUT = 3;

    private static final String TAG = "HttpGetTask";

    private Handler handler;

    public HttpGetTask(Handler handler) {
        this.handler = handler;
    }

    @Override
    protected String doInBackground(String... strings) {
        InputStream content = null;
        try {
            HttpParams httpParameters = new BasicHttpParams();
            // Set the timeout in milliseconds until a connection is established.
            int timeoutConnection = 3000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            // Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
            int timeoutSocket = 5000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            HttpResponse response = new DefaultHttpClient(httpParameters).execute(new HttpGet(strings[0]));
            content = response.getEntity().getContent();
        } catch (ConnectTimeoutException e) {
            Log.e(TAG, "Network exception", e);
            handler.sendMessage(handler.obtainMessage(MSG_CONNECTIONTIMEOUT));
        } catch (Exception e) {
            Log.e(TAG, "Network exception", e);
            handler.sendMessage(handler.obtainMessage(MSG_CONNECTIONERROR));
        }
        if(content != null) {
        	return (inputStreamToString(content)).toString();
        } else {
        	return null;
        }
    }
	
	@Override
    public void onPostExecute(String result) {
		try {
            Log.i(TAG, "Http result: " + result);
            if(result != null) {
                handler.sendMessage(handler.obtainMessage(MSG_RESULT, result));
            }
		} catch (ClassCastException e) {
            Log.e(TAG, "No valid response from the downloadserver", e);
		}
    }

	private StringBuilder inputStreamToString(InputStream is) {
        String line;
        StringBuilder total = new StringBuilder();

        // Wrap a BufferedReader around the InputStream
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        // Read response until the end
        try {
            while ((line = rd.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            Log.e("Sabber", "Error reading inputstream", e);
        }

        // Return full string
        return total;
    }
}
