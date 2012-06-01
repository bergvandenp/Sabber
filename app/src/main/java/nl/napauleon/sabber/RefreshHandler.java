package nl.napauleon.sabber;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import nl.napauleon.sabber.http.HttpGetTask;
import nl.napauleon.sabber.http.HttpHandler;

public class RefreshHandler extends HttpHandler {

    public RefreshHandler(Context context) {
        super(context);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case HttpGetTask.MSG_RESULT:
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
                break;
            default:
                super.handleMessage(msg);
        }
    }
}
