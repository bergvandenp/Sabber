package nl.napauleon.downloadmanager;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import nl.napauleon.downloadmanager.http.HttpGetTask;

public class RefreshHandler extends Handler {
    private final Fragment fragment;

    public RefreshHandler(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case HttpGetTask.MSG_RESULT:
                fragment.onResume();
                break;
            default:
                super.handleMessage(msg);
        }
    }
}
