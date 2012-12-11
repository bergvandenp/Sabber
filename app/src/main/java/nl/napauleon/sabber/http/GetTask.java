package nl.napauleon.sabber.http;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import nl.napauleon.sabber.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class GetTask extends AsyncTask<String, Void, String> {
	protected static final String TAG = Constants.TAG + ".HttpGetTask";

	private HttpCallback callback;

    public GetTask(HttpCallback callback) {
        this.callback = callback;
    }
    
    @SuppressLint("NewApi")
	public void executeRequest(String... params) {
    	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
    		super.execute(params);
    	} else {
    		super.executeOnExecutor(THREAD_POOL_EXECUTOR, params);
    	}
    }

    private void notifyFrontendAboutError() {
        if (!isCancelled()) {
            callback.handleError("Error connecting with server");
        }
    }

    @Override
    public void onPostExecute(String result) {
		try {
            Log.i(TAG, "Http result: " + result);
            if(result != null) {
            	callback.handleResponse(result);
            } else {
            	notifyFrontendAboutError();
            }
		} catch (ClassCastException e) {
            Log.e(TAG, "No valid response from the server", e);
		}
    }

	protected StringBuilder inputStreamToString(InputStream is) {
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
