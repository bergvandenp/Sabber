package nl.napauleon.downloadmanager.http;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import nl.napauleon.downloadmanager.ContextHelper;

public class HttpHandler extends Handler {

    private Context context;

    public HttpHandler(Context context) {
        this.context = context;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case HttpGetTask.MSG_CONNECTIONERROR:
                new ContextHelper().showConnectionErrorAlert(context);
                break;
            default:
                super.handleMessage(msg);
        }
    }
}
