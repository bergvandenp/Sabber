package nl.napauleon.sabber.http;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

public class HttpGetTask extends GetTask {

    public HttpGetTask(HttpCallback callback) {
		super(callback);
	}

    @Override
    protected String doInBackground(String... strings) {
        InputStream content = null;
        HttpResponse response = null;
        String request = strings[0];
        try {
            response = executeRequest(request);
            content = response.getEntity().getContent();
            if (isCancelled()) {
                return null;
            }
            if(response.getStatusLine().getStatusCode() == 200 && content != null) {
                return (inputStreamToString(content)).toString();
            } else {
            	Log.w(TAG, "no response for request " + request);
            }
        } catch (ConnectTimeoutException e) {
            Log.w(TAG, "Connection timed out for uri " + request);
        } catch (ClientProtocolException e) {
            Log.e(TAG, "Http error occured", e);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Connection to ")) {
                Log.w(TAG, "Failed to connect to " + request);
            } else {
                Log.e(TAG, "IO exception occured", e);
            }
        }
        return null;
    }

    private HttpResponse executeRequest(String string) throws IOException {
        HttpResponse response;HttpParams httpParameters = new BasicHttpParams();
        // Set the timeout in milliseconds until a connection is established.
        int timeoutConnection = 3000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        // Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
        int timeoutSocket = 5000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
        response = new DefaultHttpClient(httpParameters).execute(new HttpGet(string));
        return response;
    }
}
