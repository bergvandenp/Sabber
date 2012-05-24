package nl.napauleon.downloadmanager;

import android.os.Message;
import android.support.v4.app.Fragment;
import nl.napauleon.downloadmanager.http.HttpGetTask;
import nl.napauleon.downloadmanager.http.HttpHandler;

public class RefreshHandler extends HttpHandler {
    private final Fragment fragment;

    public RefreshHandler(Fragment fragment) {
        super(fragment.getActivity());
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
