package nl.napauleon.sabber.http;

import android.os.Handler;

public class HttpGetHandler extends Handler{

    public static final int MSG_RESULT = 1;
    public static final int MSG_CONNECTIONERROR = 2;
    public static final int MSG_CONNECTIONTIMEOUT = 3;

    public HttpGetHandler(Callback callback) {
        super(callback);
    }

    public void executeRequest(String request) {
        new HttpGetTask(this).execute(request);
    }
}
