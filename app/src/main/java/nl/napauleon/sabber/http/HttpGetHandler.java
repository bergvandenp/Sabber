package nl.napauleon.sabber.http;

import android.os.Handler;
import android.os.Message;

import java.io.InputStream;

public class HttpGetHandler extends Handler{

    private static final String TAG = "HttpGetHandler";

    public static final int MSG_REQUEST = 10;

    public static final int MSG_RESULT = 1;
    public static final int MSG_CONNECTIONERROR = 2;
    public static final int MSG_CONNECTIONTIMEOUT = 3;

    public HttpGetHandler(Callback callback) {
        super(callback);
    }

    @Override
    public void handleMessage(Message msg) {
        if(msg.what == MSG_REQUEST) {
            InputStream content = null;
//            try {
//                HttpParams httpParameters = new BasicHttpParams();
//                // Set the timeout in milliseconds until a connection is established.
//                int timeoutConnection = 3000;
//                HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
//                // Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
//                int timeoutSocket = 5000;
//                HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
//                Log.e(TAG, "Executing http request " + msg.obj.toString());
//                HttpResponse response = new DefaultHttpClient(httpParameters).execute(new HttpGet(msg.obj.toString()));
//                content = response.getEntity().getContent();
//            } catch (ConnectTimeoutException e) {
//                Log.e(TAG, "Network exception", e);
//                sendMessage(obtainMessage(MSG_CONNECTIONTIMEOUT));
//            } catch (Exception e) {
//                Log.e(TAG, "Network exception", e);
//                sendMessage(obtainMessage(MSG_CONNECTIONERROR));
//            }
            new HttpGetTask(this).execute((String)msg.obj);
        }
        if(msg.what == MSG_RESULT) {
            sendMessage(obtainMessage(MSG_RESULT, msg.obj));
        }
    }


}
