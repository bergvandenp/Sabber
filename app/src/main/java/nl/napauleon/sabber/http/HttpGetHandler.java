package nl.napauleon.sabber.http;

import android.os.Handler;
import android.os.Message;

public class HttpGetHandler extends Handler{

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
            new HttpGetTask(this).execute((String)msg.obj);
        }
        if(msg.what == MSG_RESULT) {
            sendMessage(obtainMessage(MSG_RESULT, msg.obj));
        }
    }


}
