package nl.napauleon.downloadmanager.http;

import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import java.io.*;

public class HttpGetTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "HttpGetTask";
    private AsyncTaskHandler handler;

    public HttpGetTask(AsyncTaskHandler handler) {
        this.handler = handler;
    }

	@Override
    protected String doInBackground(String... strings) {
        InputStream content = null;
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(new HttpGet(strings[0]));
            content = response.getEntity().getContent();
        } catch (Exception e) {
            Log.e(TAG, "Network exception", e);
        }
        if(content != null) {
        	return (inputStreamToString(content)).toString();
        } else {
        	return null;
        }
    }
	
	@Override
    public void onPostExecute(String result) {
    	if(result == null) {
        	Log.e(TAG, "no result available");
    		return;
        }
		try {
			processResult(result);
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing json string", e);
		} catch (ClassCastException e) {
            Log.e(TAG, "No valid response from the downloadserver", e);
		} catch (UnsupportedEncodingException e) {
            Log.e(TAG, "No valid encoding", e);
        }
    }
	
	private void processResult(String result) throws JSONException, UnsupportedEncodingException {
        if(handler != null) {
            handler.handleResult(result);
        }else {
            new DefaultAsyncTaskHandler().handleResult(result);
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
            Log.e("DownloadManager", "Error reading inputstream", e);
        }

        // Return full string
        return total;
    }
}
