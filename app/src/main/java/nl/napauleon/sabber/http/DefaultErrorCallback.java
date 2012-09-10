package nl.napauleon.sabber.http;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import nl.napauleon.sabber.ContextHelper;

public class DefaultErrorCallback implements Handler.Callback{

    private Context context;

    public DefaultErrorCallback(Context context) {
        this.context = context;
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case HttpGetHandler.MSG_CONNECTIONTIMEOUT:
                new ContextHelper().showConnectionTimeoutAlert(context);
                return true;
            case HttpGetHandler.MSG_CONNECTIONERROR:
                new ContextHelper().showConnectionErrorAlert(context);
                return true;
        }
        return false;
    }
}
