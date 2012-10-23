package nl.napauleon.sabber.http;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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

    private static final String TAG = "HttpGetTask";

    private Handler handler;

    public HttpGetTask(Handler handler) {
        this.handler = handler;
    }

    @Override
    protected String doInBackground(String... strings) {
        InputStream content = null;
        HttpResponse response = null;
        try {
            HttpParams httpParameters = new BasicHttpParams();
            // Set the timeout in milliseconds until a connection is established.
            int timeoutConnection = 3000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            // Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
            int timeoutSocket = 5000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            response = new DefaultHttpClient(httpParameters).execute(new HttpGet(strings[0]));
            content = response.getEntity().getContent();
            if(response.getStatusLine().getStatusCode() == 200 && content != null) {
                return (inputStreamToString(content)).toString();
            } else {
                handler.sendMessage(handler.obtainMessage(HttpGetHandler.MSG_CONNECTIONERROR));
            }

        } catch (ConnectTimeoutException e) {
            Log.e(TAG, "Connection timed out for uri " + strings[0]);
            handler.sendMessage(handler.obtainMessage(HttpGetHandler.MSG_CONNECTIONTIMEOUT));
        } catch (ClientProtocolException e) {
            Log.e(TAG, "Http error occured", e);
            handler.sendMessage(handler.obtainMessage(HttpGetHandler.MSG_CONNECTIONTIMEOUT));
        } catch (IOException e) {
            if (e.getMessage().startsWith("Connection to ")) {
                Log.w(TAG, "Failed to connect to " + strings[0]);
            } else {
                Log.e(TAG, "IO exception occured", e);
            }
            handler.sendMessage(handler.obtainMessage(HttpGetHandler.MSG_CONNECTIONTIMEOUT));
        }
        return null;
    }

    @Override
    public void onPostExecute(String result) {
		try {
            Log.i(TAG, "Http result: " + result);
            if(result != null) {
                handler.sendMessage(handler.obtainMessage(HttpGetHandler.MSG_RESULT, result));
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
